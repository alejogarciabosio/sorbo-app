-- Base de datos en la nube de Sorbo (tarea A1): tablas, seguridad por fila (RLS) y bucket privado de fotos.
-- Los id los genera la app (UUID) para poder crear registros sin conexión.
-- Los nombres van en snake_case; los DTO de data/remote los mapean con @SerializedName:
--   usuarioId -> usuario_id, creadoEn -> creado_en, actualizadoEn -> actualizado_en, fotoUrl -> foto_url,
--   vinoId -> vino_id, meGusto -> me_gusto. Los enums viajan por nombre (DULCE, CARNE...), igual que en Room.
-- fotoPath y estadoSync son solo del teléfono y no existen en la nube.

-- ---------------------------------------------------------------- perfiles
create table public.perfiles (
  id        uuid primary key references auth.users (id) on delete cascade,
  creado_en timestamptz not null default now()
);

-- ---------------------------------------------------------------- vinos
create table public.vinos (
  id             uuid primary key,
  usuario_id     uuid not null default auth.uid() references auth.users (id) on delete cascade,
  nombre         text not null check (char_length(btrim(nombre)) > 0),
  bodega         text,
  cepa           text,
  anio           smallint check (anio between 1800 and 2200),
  foto_url       text,
  creado_en      timestamptz not null default now(),
  actualizado_en timestamptz not null default now(),
  eliminado      boolean not null default false,
  -- Permite que degustaciones apunte a (vino, dueño) y así nadie cuelgue una degustación de un vino ajeno.
  unique (id, usuario_id)
);

-- ---------------------------------------------------------------- degustaciones
create table public.degustaciones (
  id             uuid primary key,
  vino_id        uuid not null,
  usuario_id     uuid not null default auth.uid() references auth.users (id) on delete cascade,
  fecha          date not null,
  me_gusto       boolean not null,
  dulzor         text check (dulzor in ('DULCE', 'SECO')),
  comida         text check (comida in ('CARNE', 'PASTA', 'PESCADO', 'PIZZA', 'QUESOS', 'PICADA', 'POSTRE', 'SIN_COMIDA', 'OTRA')),
  compania       text check (compania in ('SOLO', 'PAREJA', 'AMIGOS', 'FAMILIA', 'TRABAJO', 'OTRA')),
  ocasion        text check (ocasion in ('CENA', 'ASADO', 'PREVIA', 'ALMUERZO', 'CELEBRACION', 'OTRA')),
  nota           text,
  actualizado_en timestamptz not null default now(),
  eliminado      boolean not null default false,
  foreign key (vino_id, usuario_id) references public.vinos (id, usuario_id) on delete cascade
);

-- Índices: el dueño (lo usa RLS en cada consulta), lo cambiado desde la última sincronización y el vino de cada degustación.
create index vinos_usuario_actualizado_idx on public.vinos (usuario_id, actualizado_en);
create index degustaciones_usuario_actualizado_idx on public.degustaciones (usuario_id, actualizado_en);
create index degustaciones_vino_idx on public.degustaciones (vino_id, usuario_id);

-- ---------------------------------------------------------------- seguridad por fila
alter table public.perfiles      enable row level security;
alter table public.vinos         enable row level security;
alter table public.degustaciones enable row level security;

-- Cada tabla: el usuario con sesión solo ve y modifica lo suyo. (select auth.uid()) se evalúa una vez por consulta.
create policy perfiles_propios on public.perfiles
  for all to authenticated
  using (id = (select auth.uid())) with check (id = (select auth.uid()));

create policy vinos_propios on public.vinos
  for all to authenticated
  using (usuario_id = (select auth.uid())) with check (usuario_id = (select auth.uid()));

create policy degustaciones_propias on public.degustaciones
  for all to authenticated
  using (usuario_id = (select auth.uid())) with check (usuario_id = (select auth.uid()));

-- Sin sesión (rol anon) no hay ninguna política: no ve ni escribe nada.
revoke all on public.perfiles, public.vinos, public.degustaciones from anon;

-- ---------------------------------------------------------------- perfil automático al crear la cuenta
create function public.crear_perfil_al_registrarse()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
  insert into public.perfiles (id) values (new.id);
  return new;
end;
$$;

-- Solo la dispara el trigger; nadie la puede llamar desde la API.
revoke execute on function public.crear_perfil_al_registrarse() from public, anon, authenticated;

create trigger al_crear_usuario
  after insert on auth.users
  for each row execute function public.crear_perfil_al_registrarse();

-- ---------------------------------------------------------------- fotos de etiquetas
-- Bucket privado. La app comprime a 500 KB; se deja 1 MB de margen. Ruta: {usuario_id}/{vino_id}.jpg
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('etiquetas', 'etiquetas', false, 1048576, array['image/jpeg'])
on conflict (id) do nothing;

create policy etiquetas_propias on storage.objects
  for all to authenticated
  using (bucket_id = 'etiquetas' and (storage.foldername(name))[1] = (select auth.uid())::text)
  with check (bucket_id = 'etiquetas' and (storage.foldername(name))[1] = (select auth.uid())::text);
