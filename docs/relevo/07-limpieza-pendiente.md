# Limpieza pendiente

*Lista viva de cosas que ya no aportan y se pueden borrar o corregir. Tachar al hacerlo.*

## Ramas ya integradas (borrar del remoto)

Todas están contenidas en `main` o en `develop`; no tienen commits propios sin integrar. Se borran con `git push origin --delete <rama>`.

- [ ] `feature/proyecto-base`
- [ ] `feature/tema-y-componentes`
- [ ] `feature/navegacion`
- [ ] `feature/dominio`
- [ ] `feature/lectura-local-etiquetas`
- [ ] `feature/ajustes-devolucion`
- [ ] `feature/registrar`
- [ ] `feature/detalle`
- [ ] `feature/contratos-relevo` (integrada en `develop`)
- [ ] `feature/room`, `feature/preferencias`, `feature/conectividad`, `feature/supabase-cliente` (una vez que `develop` las tenga)

## Código y documentación

- [ ] `DatosDeEjemplo.kt`, `VinoRepositoryEnMemoria`, `SesionRepositoryEnMemoria`, `PreferenciasRepositoryEnMemoria`, `ConectividadSimulada`: siguen siendo la versión de prueba de los contratos y de los tests; borrar solo lo que los tests ya no usen.
- [ ] `PantallaEnConstruccion` y `GaleriaComponentes`: se borran cuando existan las pantallas reales de Ajustes y Cuenta (tareas de Nino).
- [ ] La copia de la preentrega que está en Drive ("TPO Sorbo - Preentrega corregida") todavía nombra `supabase-kt` en tres lugares (diagrama de topología, persistencia y tabla de tecnologías). La versión del repositorio (`docs/preentrega.md`) y el código ya dicen Retrofit + OkHttp, que es lo que pide el enunciado del TPO. Actualizar el texto de Drive antes de la entrega final.
- [x] `allowBackup="true"`: la sesión de Supabase queda excluida del respaldo de Google (`res/xml/reglas_de_respaldo.xml` y `reglas_de_extraccion.xml`).
