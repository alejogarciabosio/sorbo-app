# Diseño de datos, red y sincronización

*Para Iñaki (tareas I1–I6) y para Alejo (A1, A2, A5). Es una propuesta concreta, no una obligación: si algo no cierra al implementarlo, se cambia y se anota acá.*

## Principios

1. **Room es la fuente de verdad.** La interfaz solo lee de Room. Nunca espera a la red.
2. **Toda escritura local queda `PENDIENTE`** y con `actualizadoEn = ahora`. El respaldo es un proceso aparte.
3. **Sin cuenta, la app funciona completa** y no sube nada. Con cuenta, se respalda en segundo plano.
4. **Borrado lógico:** `eliminado = true`, también en la nube. Así una eliminación hecha sin conexión se propaga.
5. **Gana la última escritura** (`actualizadoEn` más reciente). Es una decisión simple y explicable; los conflictos reales son raros en un diario personal.
6. **Nunca exponer entidades de Room a la UI.** Entidad ↔ modelo de dominio con mappers.

## I1 — Room

### Dependencias nuevas
Room (runtime, ktx y compilador) con **KSP**. Agregarlas en `gradle/libs.versions.toml` y `app/build.gradle.kts` en un commit de `build:` aparte, y avisar, porque son archivos compartidos.

### Entidades (propuesta)

| Entidad (tabla) | Campos | Notas |
|---|---|---|
| `VinoEntidad` (`vinos`) | `id` (PK, texto), `usuarioId?`, `nombre`, `bodega?`, `cepa?`, `anio?`, `fotoPath?`, `fotoUrl?`, `textoBusqueda`, `creadoEn`, `actualizadoEn`, `estadoSync`, `eliminado` | Guardar `textoBusqueda` (ya viene calculado en el modelo) permite buscar con una consulta parametrizada |
| `DegustacionEntidad` (`degustaciones`) | `id` (PK), `vinoId` (FK a `vinos`, índice), `fecha`, `meGusto`, `dulzor?`, `comida?`, `compania?`, `ocasion?`, `nota?`, `actualizadoEn`, `estadoSync`, `eliminado` | Clave foránea con índice en `vinoId` |
| `AnalisisGustosEntidad` (`analisis_gustos`) | una sola fila: `resumen`, `sugerencias` (texto JSON), `generadoEn`, `cantidadDegustaciones` | Cache del último resumen con IA (`AnalisisGustosRepository.observarUltimo`) |

- **Conversores:** `Instant` ↔ `Long` (milisegundos), `LocalDate` ↔ texto ISO, enumeraciones ↔ nombre.
- **Consulta del diario:** vinos no eliminados con sus degustaciones no eliminadas (`@Relation`), ordenado por la fecha de la última degustación. Hay que **replicar el orden exacto** de `VinoRepositoryEnMemoria.observarDiario`.
- **Búsqueda:** `buscar(consultaNormalizada)` debe devolver los vinos cuyo texto contiene **todas** las palabras de la consulta. Dos caminos válidos: (a) una consulta por palabra con `LIKE '%' || :palabra || '%'` sobre `textoBusqueda` (siempre con parámetros, nunca concatenando texto); o (b) traer los vinos y filtrar en Kotlin como hace hoy la versión en memoria. Elegir el más simple de explicar.
- **Mappers** en `data/mapper`: `VinoEntidad.aDominio()` y `Vino.aEntidad()`, ídem degustación.

### Prueba de contrato (lo más importante de I1)

Ya existen tests de `VinoRepositoryEnMemoria`. Convertirlos en una **batería común** que se ejecuta contra las dos implementaciones (la de memoria con JUnit común; la de Room como test instrumentado en `androidTest` con una base en memoria). Si ambas pasan los mismos tests, cambiar una por la otra en `AppContainer` es seguro.

Casos que no pueden faltar: guardar un vino nuevo y verlo en el diario; agregar una degustación; el orden del diario; eliminar un vino y sus degustaciones; **restaurar**; eliminar una sola degustación; `observarVino` de un vino eliminado devuelve `null`; la búsqueda ignora tildes y mayúsculas; cada escritura deja `estadoSync = PENDIENTE` y actualiza `actualizadoEn`.

### Cambios en `AppContainer`
Reemplazar la creación de `VinoRepositoryEnMemoria` por `VinoRepositoryRoom` y crear la base de datos una sola vez. **Los vinos de ejemplo no deben cargarse en producción** (decisión abierta #1): si se quieren para desarrollo, que sea con una bandera de depuración.

### Fotos huérfanas
Si el usuario saca una foto y la app se cierra antes de guardar, la foto queda en el almacenamiento sin vino. Propuesta: agregar `AlmacenFotos.listar()` (contrato + versión local + fake) y, al iniciar, borrar las fotos que ningún `fotoPath` referencia. Hacerlo con cuidado: nunca borrar una foto de un formulario abierto.

## I2 — Preferencias (DataStore)

`PreferenciasRepositoryDataStore` implementa `PreferenciasRepository`. Cuatro claves: `bienvenida_vista` (Boolean), `aviso_ia_aceptado` (Boolean), `ia_activada` (Boolean, por defecto `true`), `busquedas_recientes` (un solo texto con las búsquedas unidas por un separador que no pueda aparecer en una búsqueda, por ejemplo salto de línea). **Usar `agregarBusquedaReciente(...)` de `domain/util`** para no duplicar la regla de "sin repetidos, las más nuevas primero, máximo 5". Copiar los tests de `PreferenciasRepositoryEnMemoriaTest`.

## I3 — Conectividad real

`ConectividadObserverAndroid` con `ConnectivityManager.registerDefaultNetworkCallback` dentro de un `callbackFlow`. Emitir `true` solo cuando la red tiene `NET_CAPABILITY_INTERNET` **y** `NET_CAPABILITY_VALIDATED` (tener Wi-Fi no es tener internet). Emitir el valor inicial al suscribirse y `distinctUntilChanged()`. Requiere los permisos `INTERNET` y `ACCESS_NETWORK_STATE` en el manifiesto. Prueba: modo avión en el teléfono.

## I4 — Cliente de Supabase

Supabase se usa por su **API REST estándar**, sin su SDK, con Retrofit + OkHttp (así se explica con lo visto en clase).

| Para qué | Método y ruta |
|---|---|
| Crear cuenta | `POST /auth/v1/signup` |
| Iniciar sesión | `POST /auth/v1/token?grant_type=password` |
| Renovar sesión | `POST /auth/v1/token?grant_type=refresh_token` |
| Recuperar contraseña | `POST /auth/v1/recover` |
| Cambiar contraseña / datos | `PUT /auth/v1/user` |
| Cerrar sesión | `POST /auth/v1/logout` |
| Leer / escribir tablas | `GET/POST/PATCH /rest/v1/vinos`, `/rest/v1/degustaciones` (`Prefer: resolution=merge-duplicates` para *upsert*) |
| Subir / bajar fotos | `POST/GET /storage/v1/object/etiquetas/{usuario_id}/{vino_id}.jpg` |
| Resumen con IA | `POST /functions/v1/analizar-gustos` |

Todas llevan el encabezado `apikey: <anon key>`. Las que son del usuario llevan además `Authorization: Bearer <access_token>`.

- **La URL y la clave anónima salen de `local.properties`** (`SUPABASE_URL`, `SUPABASE_ANON_KEY`) hacia `BuildConfig`. Nunca en el repo. Hay un `local.properties.example` con los nombres.
- **Guardar la sesión** (tokens) en almacenamiento privado de la app. Si un pedido devuelve 401, renovar con el *refresh token* y reintentar una vez; si también falla, `EstadoSesion.SinSesion`.
- **Traducir errores** de la API a `MotivoCuenta` (ver `SesionRepository`): credenciales incorrectas, email ya registrado, clave débil, sin conexión (excepciones de red), etc. La UI de Nino solo conoce `MotivoCuenta`.
- **Tiempos de espera** explícitos en OkHttp (por ejemplo 10 s) y nada de bloquear el hilo principal.
- **No registrar tokens ni contraseñas en logs.** Si se usa un interceptor de logs, solo en depuración y sin cuerpos.

## I5 — Sincronización

### Qué se sincroniza
Los `vinos` y `degustaciones` con `estadoSync = PENDIENTE` (incluidos los eliminados) y las fotos de los vinos pendientes. Solo si hay sesión.

### Caso de uso `SincronizarDiario` (en `domain/usecase`)
Depende de **un puerto del dominio** (una interfaz, por ejemplo `RespaldoRemoto`) y no de Retrofit, para poder probarlo con una versión simulada. Pasos:

1. Leer lo pendiente (hace falta un método nuevo en el repositorio local, por ejemplo `listarPendientes()`; es un cambio de contrato: avisar, ver regla en `02`).
2. Subir la foto (si hay `fotoPath` y no `fotoUrl`) y guardar la `fotoUrl`.
3. *Upsert* del vino y sus degustaciones.
4. Si salió bien, marcar `SINCRONIZADO` **solo si el registro no cambió mientras tanto** (comparar `actualizadoEn`). Si cambió, queda `PENDIENTE` y se sube en la próxima vuelta.
5. Devolver un resultado (cuántos subió, si falló algo y si vale la pena reintentar).

### `SyncWorker` (WorkManager)
- Trabajo **único** (`enqueueUniqueWork`) con restricción `NetworkType.CONNECTED`, reintento con *backoff* exponencial.
- Se encola cada vez que se guarda, edita o elimina algo (si hay sesión) y al iniciar la app.
- Si `SincronizarDiario` dice que hay que reintentar → `Result.retry()`; si es un error que no se arregla reintentando (por ejemplo, un 4xx) → `Result.failure()` y dejarlo `PENDIENTE`.
- El `Worker` crea sus dependencias desde `AppContainer` (a través de `SorboApplication`).

### Indicador para la UI (opcional pero útil)
Un flujo `observarCantidadPendientes()` permite mostrar "Respaldo al día" / "N pendientes" en Ajustes. Sumarlo al contrato junto con `listarPendientes()` en la misma rama.

### Pruebas
Test unitario de `SincronizarDiario` con repositorio y remoto simulados: sube lo pendiente; no marca `SINCRONIZADO` si cambió durante la subida; ante un error deja `PENDIENTE`; los eliminados se propagan; sin sesión no hace nada.

## I6 — Restauración (F8)

Al iniciar sesión por primera vez en un teléfono: pedir `vinos` y `degustaciones` del usuario, bajar las fotos, e insertarlas **sin duplicar** (misma `id` → gana el `actualizadoEn` más reciente) y ya como `SINCRONIZADO`. Mostrar progreso (la pantalla F8 de los mockups). Si se corta, que se pueda reanudar.

---

## A1 — Base en Supabase (Alejo)

Proyecto: `yxybmbdzltfaqbsahash`. Las migraciones van en `supabase/migrations` (nombre con fecha, SQL puro, versionadas).

| Tabla | Columnas | Seguridad (RLS) |
|---|---|---|
| `perfiles` | `id` (= `auth.users.id`), `creado_en` | solo el dueño: `id = auth.uid()` |
| `vinos` | `id uuid`, `usuario_id uuid` (por defecto `auth.uid()`), `nombre`, `bodega`, `cepa`, `anio`, `foto_url`, `creado_en`, `actualizado_en`, `eliminado` | `usuario_id = auth.uid()` en select/insert/update/delete |
| `degustaciones` | `id uuid`, `vino_id`, `usuario_id`, `fecha`, `me_gusto`, `dulzor`, `comida`, `compania`, `ocasion`, `nota`, `actualizado_en`, `eliminado` | ídem |

- Los `id` los genera la app (UUID) para poder crear registros sin conexión.
- Índices en `usuario_id` y `actualizado_en`.
- Bucket **privado** `etiquetas`; política: cada usuario solo accede a la carpeta `{su usuario_id}/…`.
- **Verificar con dos cuentas** que una no ve lo de la otra, y pasar el asesor de seguridad de Supabase sin advertencias.
- Los nombres de columnas de la nube van en `snake_case`; los DTO de `data/remote` los mapean con `@SerializedName` / `@Json`. Documentar el mapa en el propio SQL.

## A2 — Autenticación

Email + contraseña. Configurar: largo mínimo (decisión abierta #2), confirmación de email (#3), *redirect URL* para la recuperación (deep link, #4). La plantilla del mail de recuperación en español.

## A5 — Eliminar cuenta

El usuario común **no puede** borrarse a sí mismo desde la API: hace falta una Edge Function (`eliminar-cuenta`) que, verificando el token del usuario, use la clave de servicio **del lado servidor** para borrar: fotos del bucket, filas y finalmente el usuario. Esa clave nunca sale del servidor ni va al repo.
