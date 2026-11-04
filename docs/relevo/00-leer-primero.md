# Relevo del proyecto — leer primero

*Actualizado el 4 de noviembre de 2026, al cierre del Hito 1. Este documento es la puerta de entrada: te dice dónde estamos, cómo arrancar y qué reglas seguir. Los demás documentos de esta carpeta tienen el detalle.*

## En 30 segundos

**Sorbo** es un diario personal de vinos para Android (Kotlin, Jetpack Compose, MVVM + Repository). Se saca una foto de la etiqueta, se marca si gustó y con qué se tomó, y después se consulta sin conexión en la vinoteca ("¿ya lo probé?"). Con cuenta, se respalda en Supabase y se puede pedir un resumen de gustos con IA.

**Hito 1 (cerrado):** la app funciona completa en el teléfono, **con los datos en memoria** (se pierden al cerrar la app). Falta persistir, la cuenta, el respaldo y la IA. De eso se trata esta etapa.

| Ya funciona | Todavía es provisorio |
|---|---|
| Diario, búsqueda, detalle, editar, eliminar con deshacer, "Lo volví a probar" | **Los datos viven en memoria** (`VinoRepositoryEnMemoria`, con vinos de ejemplo) |
| Registrar con foto (cámara o galería), fecha, regla "¿Es el mismo vino?" | Ajustes, cuenta, iniciar sesión, privacidad: pantallas "en construcción" |
| Lectura de la etiqueta con ML Kit (sin conexión) | Bienvenida y búsquedas recientes (no existen aún) |
| Mis gustos: estadísticas calculadas en el teléfono | Resumen con IA, respaldo en la nube, restaurar en otro teléfono |
| 233 tests unitarios en verde | Accesibilidad con TalkBack real, medición con más etiquetas |

## Orden de lectura

1. **Este documento** (reglas y arranque).
2. [`01-estado-y-mapa-del-codigo.md`](01-estado-y-mapa-del-codigo.md): cómo está armado el código y los patrones a copiar. **Leelo antes de escribir una línea.**
3. [`02-plan-de-trabajo.md`](02-plan-de-trabajo.md): qué le toca a cada uno, en qué orden, y cómo no pisarse.
4. El de tu área:
   - Datos, sincronización y Supabase del lado de la app → [`03-diseno-datos-y-sincronizacion.md`](03-diseno-datos-y-sincronizacion.md)
   - Pantallas, estados, cuenta y accesibilidad → [`04-diseno-cuenta-y-pantallas.md`](04-diseno-cuenta-y-pantallas.md)
   - Resumen con IA y lectura de etiquetas en la nube → [`05-diseno-ia.md`](05-diseno-ia.md)
5. [`06-como-probar.md`](06-como-probar.md): tests, pruebas en el celular y definición de "terminado".

Otros documentos del repositorio que conviene tener a mano: [`docs/preentrega.md`](../preentrega.md) (fuente de verdad de requisitos y diseño; las secciones 9, 10 y 14 son las más usadas), [`docs/evaluacion-lectura-etiquetas.md`](../evaluacion-lectura-etiquetas.md) y los [mockups](../mockups/index.html).

## Cómo ponerlo a andar

1. Clonar: `git clone https://github.com/alejogarciabosio/sorbo-app.git` y quedarse en la rama `develop`.
2. Abrir la carpeta en **Android Studio** y esperar la sincronización de Gradle.
3. **JDK:** el proyecto compila con Java 17 o 21. Si Gradle falla con un mensaje que solo dice un número de versión (por ejemplo `25.0.4`), el IDE está usando un JDK demasiado nuevo: en *Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK*, elegí uno de 17 o 21.
4. Copiar `local.properties.example` como `local.properties` (no se sube a Git) y completar `SUPABASE_ANON_KEY` (se la pedís a Alejo por privado). Sin la clave la app compila igual; solo van a fallar la cuenta, el respaldo y la IA cuando existan.
5. Correr la app en un teléfono con depuración USB o en un emulador (Android 8.0 o superior). **La app está bloqueada en vertical.**
6. Verificar que todo esté sano antes de tocar nada: `./gradlew assembleDebug testDebugUnitTest` tiene que terminar en `BUILD SUCCESSFUL`.

## Reglas del juego

**Ramas**
- `main`: versión estable. Solo se actualiza en hitos, con fast-forward desde `develop`. **Nadie pushea a `main`.**
- `develop`: integración. **No se pushean cambios directamente**: se integra desde una rama de trabajo, después de verificar build y tests.
- Una rama por tarea, creada desde `develop` actualizado y con nombre `feature/<tema>` (por ejemplo `feature/room`, `feature/cuenta-pantallas`). Cada uno commitea **con su propia cuenta** en sus propias ramas.
- Al terminar una tarea: subí tu rama y avisá a Alejo; él (o quien él indique) la integra a `develop` con `git merge --no-ff` y pushea. Sin Pull Requests.
- **Antes de empezar cada tarea y al menos una vez por día:** `git checkout develop && git pull`, y después `git merge develop` dentro de tu rama. Los conflictos chicos y frecuentes son fáciles; los grandes y tardíos no.

**Commits**
- Chicos y frecuentes (el profesor lo pidió: muchos commits, no pocos y enormes), en español, con el formato *Conventional Commits*: `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`, `build:`.
- Cada commit tiene que compilar y pasar los tests.
- Sin trailers de coautoría y sin mencionar herramientas de asistencia en mensajes, código ni documentación.

**Código**
- Todo lo que sube lo tiene que poder explicar quien lo sube, línea por línea. **No sumar librerías ni patrones que no se hayan visto en clase** (por ejemplo Hilt, MVI, módulos de Gradle extra, capas de más). Si una dependencia es imprescindible (Room, DataStore, WorkManager, Retrofit, OkHttp ya están previstas en la preentrega), documentar por qué.
- Cada archivo nuevo empieza con un comentario de una línea con su capa y su rol (mirá cualquier archivo existente).
- **Cero textos hardcodeados** en la interfaz: todo en `app/src/main/res/values/strings.xml`.
- Estados de pantalla con `sealed interface`. `MutableStateFlow` siempre privado. Nada de lógica de negocio dentro de un composable.
- Nunca exponer a la interfaz una entidad de Room: se convierte con un mapper al modelo de dominio.

**Secretos (el repositorio es público)**
- Nada de claves, tokens ni contraseñas en el código, en `strings.xml`, en documentos ni en mensajes de commit. Van en `local.properties` (que Git ignora) o como secretos de Supabase.
- La clave "anon" de Supabase está pensada para ir en el cliente, pero igual la mantenemos fuera del repo. Las claves de IA (Groq) **jamás** van en la app: solo en una Edge Function.

## Quién hace qué

| Persona | Área | Documento |
|---|---|---|
| **Iñaki** | Datos: Room, DataStore, conectividad, sincronización, restauración | `03` |
| **Nino** | Pantallas: bienvenida, ajustes, cuenta, estados del diario, Mis gustos con IA, tests y accesibilidad | `04` |
| **Alejo** | Supabase (tablas, seguridad, Auth, almacenamiento), Edge Functions de IA, integración, README | `03` y `05` |

El detalle, tarea por tarea y con el orden recomendado, está en [`02-plan-de-trabajo.md`](02-plan-de-trabajo.md). Los roles no son exclusivos: todos tienen que entender la app completa.

## Qué no tocar sin avisar

- **Los contratos del dominio** (`app/src/main/java/com/sorbo/app/domain/repository/*`): son las interfaces que separan las pantallas de los datos. Si necesitás cambiar una firma, avisá primero: la implementación simulada, las pantallas y los tests dependen de ella.
- **Los casos de uso y sus tests** (`domain/usecase`): contienen las reglas de negocio ya validadas (vino parecido, búsqueda, estadísticas, lectura de etiquetas).
- **Archivos que tocan varios a la vez** (más fáciles de pisar): `AppContainer.kt`, `SorboNavHost.kt`, `strings.xml`, `libs.versions.toml`, `app/build.gradle.kts`. Ver las reglas en el documento `02`.
- Los 233 tests actuales tienen que seguir en verde. Si uno falla por un cambio tuyo, o el cambio rompe una regla, o el test hay que actualizarlo y explicar por qué.

## "Terminado" significa

Ver la lista completa en [`06-como-probar.md`](06-como-probar.md). En corto: compila, los tests pasan (con tests nuevos para lo que agregaste), lo probaste en un teléfono real incluyendo los casos difíciles (modo avión, fuente grande, tema oscuro, la app cerrada por el sistema), cumple el requisito de la preentrega que corresponde, y dejaste el documento de diseño de tu área actualizado si cambiaste algo.
