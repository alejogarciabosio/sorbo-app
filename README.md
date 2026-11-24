# Sorbo

Diario personal de vinos para Android. Registrás cada vino que probás con una foto de la etiqueta y una opinión simple, y después, en la vinoteca, sabés al instante si ya lo probaste y qué te pareció, aunque no tengas señal.

TPO de Desarrollo de Aplicaciones I — UADE.

## Problema

Las personas que toman vino de forma ocasional no recuerdan qué vinos probaron ni si les gustaron. Al comprar eligen al azar y repiten malas experiencias. Las apps existentes se centran en la opinión de la comunidad, usan vocabulario técnico y dependen de conexión.

## Propuesta

- Registro rápido con foto y opinión simple (me gustó / no, dulce / seco, comida, compañía, ocasión), sin conexión.
- Búsqueda en tu historial: "¿ya lo probé?", sin conexión.
- Autocompletado de los datos del vino leyendo la etiqueta en el teléfono, sin conexión ni cuenta.
- Con conexión: resumen de tus gustos con IA, a partir de tus estadísticas.
- Respaldo en la nube para no perder el historial.

## Stack

Kotlin · Jetpack Compose (Material 3) · MVVM + Clean Architecture · Navigation Compose · Coroutines y Flow · Room · DataStore · WorkManager · Retrofit + OkHttp · Coil · ML Kit (lectura de etiquetas en el teléfono) · Supabase (Auth, Postgres con seguridad por fila, Storage y Edge Functions) · Groq (modelos de texto y de visión, solo desde las Edge Functions).

## Cómo ejecutar

1. Abrir la carpeta del repositorio en Android Studio con un JDK 17 o 21 (en *Settings → Build Tools → Gradle → Gradle JDK*) y esperar la sincronización de Gradle.
2. Copiar `local.properties.example` como `local.properties` y completar `SUPABASE_ANON_KEY` con la clave publicable del proyecto de Supabase (pedírsela al equipo). Sin la clave la app compila igual; solo no funcionan la cuenta, el respaldo ni la IA.
3. Ejecutar la configuración `app` en un emulador o un teléfono con Android 8.0 (API 26) o superior. La app está bloqueada en vertical.

Desde la terminal: `./gradlew assembleDebug` compila, `./gradlew testDebugUnitTest` corre los tests unitarios y `./gradlew connectedDebugAndroidTest` corre los de Room en un emulador o teléfono conectado.

**Importante antes de una demo:** el proyecto gratuito de Supabase se hiberna por inactividad. Usarlo una hora antes (abrir la app o el panel) para que responda sin demora.

## Qué hace la app

- **Registrar (RF01):** foto de la etiqueta con la cámara o la galería (permiso explicado, alternativas si se rechaza, foto de hasta 500 KB), nombre y si te gustó como únicos datos obligatorios, y opciones de un toque para dulzor, comida, compañía y ocasión. Si el vino ya está en el diario, pregunta si es el mismo y suma una degustación en vez de duplicarlo. Todo funciona sin conexión.
- **Consultar (RF02):** el buscador responde "¿ya lo probé?" en el diario del teléfono, sin tildes ni mayúsculas y con texto parcial, aunque no haya señal. Si no está, dice "No lo encontramos en tu diario" y ofrece registrarlo.
- **Leer la etiqueta (RF03):** ML Kit completa nombre, bodega, cepa y año en el teléfono, sin conexión. Con cuenta, el aviso aceptado y conexión, un modelo de visión en la nube mejora lo sugerido. Todo queda marcado como sugerido y editable, y nada se guarda sin tocar "Guardar".
- **Mis gustos (RF04):** estadísticas calculadas en el teléfono (por ejemplo, "secos: 8 de 9") y, con cuenta, un resumen con IA que usa solo esas estadísticas.
- **Cuenta y respaldo:** iniciar sesión, crear cuenta, recuperar contraseña desde el mail, cambiar contraseña, cerrar sesión y eliminar la cuenta. Con cuenta, el diario se respalda en segundo plano al volver la conexión y se puede restaurar en otro teléfono.
- **Estados de pantalla:** carga, vacío, error con "Reintentar", sin conexión, permiso rechazado y recuperación ante errores.

## Requisitos alcanzados

| Requisito | Estado |
|---|---|
| RF01 Registrar un vino | Cumplido |
| RF02 Consultar si ya lo probé | Cumplido |
| RF03 Completar datos leyendo la etiqueta | Cumplido (ML Kit sin conexión; mejora opcional con IA en la nube) |
| RF04 Análisis de mis gustos | Cumplido (estadísticas locales y resumen con IA) |
| Cuenta, respaldo y restauración | Cumplido; falta la prueba en un celular real del recorrido completo |
| RNF03 Accesibilidad | Parcial: descripciones, tamaños y contraste revisados y con tests; falta recorrerla con TalkBack real |

## Arquitectura

MVVM con patrón Repository y flujo unidireccional de datos, en tres capas: `ui` (screens, viewmodels, components, theme, navegacion), `domain` (model, repository, usecase, util) y `data` (local, remote, sync, red, ml, foto, mapper, memoria). Las pantallas solo dibujan el estado que expone el ViewModel; nunca acceden a datos. La inyección de dependencias es manual (`AppContainer`), a propósito, para poder explicarla sin librerías extra.

**Offline First:** la interfaz siempre lee de Room, que es la fuente de verdad. Toda escritura queda `PENDIENTE` con su `actualizadoEn`; con una cuenta abierta, un `SyncWorker` de WorkManager (con restricción de red y espera creciente) la sube a Supabase, y recién entonces la marca `SINCRONIZADO` si no cambió durante la subida. Gana la última escritura, los borrados son lógicos (`eliminado = true`) y los ids son UUID, así que subir dos veces no duplica. Sin cuenta no se sube nada.

**Casos de uso (`domain/usecase`):** son una decisión propia, no figuran en lo visto en clase. Cada uno concentra una regla de negocio (detectar si un vino ya está en el diario, calcular estadísticas, interpretar el texto de una etiqueta, sincronizar, restaurar) para que sea pura, testeable sin Android y reutilizable. El ViewModel los llama y ellos usan el Repository. Se prueban con tests unitarios.

**Contratos y versiones en memoria:** cada fuente de datos tiene una interfaz en `domain/repository` y una versión en memoria en `data/memoria`. Los mismos tests de contrato corren contra las dos (y contra Room, en un emulador), así que cambiar una por otra en `AppContainer` es seguro.

**Nube (`supabase/`):** tablas con seguridad por fila (cada usuario solo ve lo suyo), un bucket privado de fotos y tres Edge Functions: `analizar-gustos`, `leer-etiqueta` y `eliminar-cuenta`. Las claves de la IA viven solo como secretos de las funciones: nunca van en la app ni en el repositorio. Cada función verifica la sesión y limita el uso por usuario y por día, y las de IA prueban modelos de respaldo si Groq retira uno.

## Decisiones relevantes

- **ML Kit como base y la IA en la nube como mejora opcional:** la lectura en el teléfono funciona sin conexión, sin cuenta y sin que la foto salga del teléfono. La nube solo se usa con cuenta, aviso aceptado, IA activada y conexión, y cada dato que devuelve se acepta solo si figura en los renglones que el propio modelo dice haber leído. Detalle y mediciones en `docs/evaluacion-lectura-etiquetas.md`.
- **Retrofit + OkHttp contra la API REST de Supabase**, sin su SDK: es lo visto en clase y se puede explicar línea por línea.
- **La IA recibe solo estadísticas** para el resumen, nunca notas ni nombres de vinos. La foto de una etiqueta solo se envía con consentimiento y no se guarda.
- **Privacidad por diseño:** se puede usar todo sin cuenta; la cuenta se puede eliminar desde Ajustes junto con su respaldo.

## Limitaciones conocidas

- El respaldo no resuelve conflictos entre dos teléfonos editando a la vez: gana la última escritura (el enunciado excluye el uso simultáneo en varios dispositivos).
- La lectura de etiquetas con ML Kit falla con tipografías muy decorativas y puede tomar un año de otra botella que salga en la foto; por eso todo es una sugerencia editable.
- La IA depende de un plan gratuito con cupos por minuto: ante el límite la app avisa y sigue funcionando sin ella.
- Los mails de recuperar contraseña salen por un Gmail y tienen un tope de 30 por hora.
- El proyecto gratuito de Supabase se hiberna por inactividad (ver arriba).
- Solo Android, solo en español y bloqueada en vertical.

## Pruebas

Más de 480 tests unitarios (casos de uso, ViewModels y repositorios, con versiones en memoria y un servidor HTTP falso para los clientes de Supabase) y una batería de contrato de 16 casos que corre contra Room en un emulador. Las pantallas del diario, el registro con cámara y lectura de etiquetas, la búsqueda, el detalle y Mis gustos se probaron en un celular real (cámara, permisos, persistencia, modo avión). Las de cuenta, Ajustes, resumen con IA, bienvenida y restauración se construyeron contra versiones simuladas y tests de ViewModel, y falta recorrerlas en un celular, junto con TalkBack.

## Documentación

- [Preentrega de análisis y diseño](docs/preentrega.md): incluye la validación de supuestos, la comparación con alternativas y la estimación de costos
- [Diagramas (draw.io)](docs/diagramas/sorbo.drawio): flujo de pantallas, arquitectura, topología y modelo de datos
- [Guía de relevo](docs/relevo/00-leer-primero.md): por dónde empezar, cómo está armado el código, diseño de datos, sincronización, cuenta e IA, y cómo probar
- [Evaluación de la lectura de etiquetas](docs/evaluacion-lectura-etiquetas.md): qué se probó para leer la etiqueta (ML Kit, PaddleOCR, Tesseract, modelos de visión), los resultados con 10 botellas y la decisión
- [Mockups de pantallas](docs/mockups/index.html): todas las pantallas y estados, base del diseño en [Figma](https://www.figma.com/design/NM0utJz4cc9JBflTfu1ep9/Sorbo-App)

## Equipo

| Integrante | Rol |
|---|---|
| Alejo Garcia Bosio | Arquitectura, interfaz, Supabase e inteligencia artificial, integración y documentación |
| Iñaki Courtinade | Datos, offline first, sincronización y pruebas en dispositivo |

Nino Bizzotto formó parte del equipo en la etapa inicial (configuración del cliente de Supabase) y dejó el proyecto antes de la implementación: sus tareas de pantallas pasaron a Alejo.
