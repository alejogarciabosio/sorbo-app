-- uso_ia solo la toca la Edge Function con la clave de servicio. Se deja explícito que la API no accede.
create policy sin_acceso_desde_la_api on public.uso_ia for all to anon, authenticated using (false) with check (false);
