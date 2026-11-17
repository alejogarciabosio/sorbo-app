-- El cupo pasa a contarse por tipo de uso (resumen de gustos y lectura de etiquetas), cada uno con su límite diario.
alter table public.uso_ia add column tipo text not null default 'gustos';
alter table public.uso_ia drop constraint uso_ia_pkey;
alter table public.uso_ia add primary key (usuario_id, dia, tipo);

create or replace function public.consumir_cupo_ia(p_usuario uuid, p_limite integer, p_tipo text default 'gustos')
returns boolean
language plpgsql
security definer
set search_path = ''
as $$
declare
  usados integer;
begin
  insert into public.uso_ia (usuario_id, dia, tipo, cantidad)
  values (p_usuario, (now() at time zone 'America/Argentina/Buenos_Aires')::date, p_tipo, 1)
  on conflict (usuario_id, dia, tipo)
  do update set cantidad = public.uso_ia.cantidad + 1
  where public.uso_ia.cantidad < p_limite
  returning cantidad into usados;
  return usados is not null;
end;
$$;

drop function public.consumir_cupo_ia(uuid, integer);
revoke execute on function public.consumir_cupo_ia(uuid, integer, text) from public, anon, authenticated;
grant execute on function public.consumir_cupo_ia(uuid, integer, text) to service_role;
