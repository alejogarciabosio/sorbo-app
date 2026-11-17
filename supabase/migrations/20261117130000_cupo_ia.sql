-- Límite de uso de la IA por usuario (tarea A3): un contador por usuario y por día.
-- Solo lo toca la Edge Function con la clave de servicio; la app no puede leerlo ni escribirlo.
create table public.uso_ia (
  usuario_id uuid not null references auth.users (id) on delete cascade,
  dia        date not null,
  cantidad   integer not null default 0 check (cantidad >= 0),
  primary key (usuario_id, dia)
);

-- RLS activado y sin políticas: ningún rol de la API (anon ni authenticated) accede.
alter table public.uso_ia enable row level security;

-- Suma un uso del día si todavía hay cupo. Devuelve true si lo consumió, false si ya se alcanzó el límite.
create function public.consumir_cupo_ia(p_usuario uuid, p_limite integer)
returns boolean
language plpgsql
security definer
set search_path = ''
as $$
declare
  usados integer;
begin
  insert into public.uso_ia (usuario_id, dia, cantidad)
  values (p_usuario, (now() at time zone 'America/Argentina/Buenos_Aires')::date, 1)
  on conflict (usuario_id, dia)
  do update set cantidad = public.uso_ia.cantidad + 1
  where public.uso_ia.cantidad < p_limite
  returning cantidad into usados;
  return usados is not null;
end;
$$;

revoke execute on function public.consumir_cupo_ia(uuid, integer) from public, anon, authenticated;
grant execute on function public.consumir_cupo_ia(uuid, integer) to service_role;
