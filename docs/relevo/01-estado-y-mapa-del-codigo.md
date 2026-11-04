# Estado y mapa del código

*Cómo está armada la app, dónde está cada cosa, los patrones a copiar y las trampas que ya encontramos. Todo lo que se dice acá se verificó en el código y en un teléfono real.*

## Arquitectura en una imagen

```
 Pantalla (Compose)  ──eventos──▶  ViewModel  ──llama──▶  Caso de uso  ──usa──▶  Repository (interfaz)
        ▲                              │                                              │
        └────── StateFlow<Estado> ─────┘                                  implementación: hoy en memoria,
                                                                          mañana Room + Supabase
```

- **MVVM + Repository con flujo unidireccional.** La pantalla solo dibuja el estado que expone el ViewModel y le avisa de los eventos. El ViewModel nunca conoce a la pantalla.
- **Una sola `Activity`** (`MainActivity`) con **Navigation Compose**. Está bloqueada en vertical.
- **Inyección de dependencias manual** en `AppContainer.kt`: crea todo una sola vez y se lo pasa a los ViewModels. No hay Hilt (a propósito, para que se explique fácil).
- **Los casos de uso (`domain/usecase`) son una decisión propia** y no figuran en lo visto en clase: concentran una regla de negocio para probarla sin Android. Están justificados en el README.
- **Offline-first:** la interfaz siempre lee de la base local (hoy, la memoria; mañana, Room). La red es un respaldo que ocurre en segundo plano.

## Mapa de carpetas

Todo está bajo `app/src/main/java/com/sorbo/app/`. Los tests repiten la misma estructura en `app/src/test/java/com/sorbo/app/`.

| Carpeta | Qué hay | Ejemplos |
|---|---|---|
| raíz | Punto de entrada y dependencias | `MainActivity`, `SorboApplication`, **`AppContainer`** |
| `ui/theme` | Colores, tipografía, formas, espaciado (Material 3) | `Theme.kt`, `Espaciado.kt` |
| `ui/navegacion` | Rutas y mapa de pantallas | **`Rutas.kt`**, **`SorboNavHost.kt`**, `AppSorbo.kt` (barra inferior, botón flotante y avisos) |
| `ui/screens` | Una pantalla por archivo (`Pantalla*.kt`) | `PantallaDiario`, `PantallaRegistro`, `PantallaDetalle`, `PantallaGustos` |
| `ui/viewmodels` | Un ViewModel por pantalla, con su `sealed interface` de estado | `DiarioViewModel`, `RegistroViewModel`, `EditarViewModel` |
| `ui/components` | Piezas reutilizables **sin estado** (reciben datos y eventos por parámetro) | `ItemVino`, `Banner`, `EstadoVacio`, `HojaDeAviso`, `ItemAjustes`, `FilaEstadistica` |
| `domain/model` | Datos puros del negocio, sin Android ni Room | `Vino`, `Degustacion`, `Usuario`, `AnalisisGustos` |
| `domain/repository` | **Contratos** (interfaces) que la capa de datos implementa | `VinoRepository`, `SesionRepository`, `PreferenciasRepository`, `ConectividadObserver`, `AnalisisGustosRepository`, `AlmacenFotos`, `LectorEtiqueta` |
| `domain/usecase` | Reglas de negocio | `RegistrarDegustacion`, `BuscarVinoParecido`, `BuscarEnDiario`, `CalcularEstadisticasGustos`, `InterpretarTextoEtiqueta` |
| `domain/util` | Funciones puras | `TextoNormalizado`, `Credenciales`, `BusquedasRecientes`, `Foto` |
| `data/memoria` | Implementaciones **simuladas en memoria** de los contratos | `VinoRepositoryEnMemoria`, `SesionRepositoryEnMemoria`, `PreferenciasRepositoryEnMemoria`, `ConectividadSimulada`, `AnalisisGustosRepositoryEnMemoria`, `DatosDeEjemplo` |
| `data/foto` | Fotos en el teléfono | `AlmacenFotosLocal` (achica a ≤ 500 KB y endereza), `ArchivoParaCamara` |
| `data/ml` | Lectura de la etiqueta con ML Kit | `LectorEtiquetaMlKit` |

**Lo que todavía no existe y hay que crear:** `data/local` (Room), `data/remote` (Retrofit + OkHttp contra Supabase), `data/sync` (WorkManager), `data/mapper`, y las implementaciones reales de `SesionRepository`, `PreferenciasRepository`, `ConectividadObserver` y `AnalisisGustosRepository`. Cada una **reemplaza a su versión simulada en `AppContainer` sin tocar las pantallas**: ese es el truco de la arquitectura.

## Un recorrido completo: registrar un vino

1. `PantallaRegistro` muestra el formulario con el estado de `RegistroViewModel` y le manda eventos (`alCambiarNombre`, `alElegirMeGusto`, `alGuardar`...).
2. Con una foto elegida, el ViewModel le pide el texto a `LectorEtiqueta` (ML Kit) y completa los campos vacíos como "sugeridos", sin bloquear nada.
3. Al guardar, valida, y llama a `BuscarVinoParecido`: si hay un vino igual o parecido, muestra la hoja "¿Es el mismo vino?".
4. `RegistrarDegustacion` arma el vino o la degustación y llama a `VinoRepository.guardarVinoNuevo` / `agregarDegustacion`.
5. El repositorio guarda y **emite el diario actualizado por un `Flow`**: el Diario, la búsqueda, el detalle y Mis gustos se actualizan solos, sin que nadie les avise.
6. La navegación va al detalle del vino y `AppSorbo` muestra el aviso "Vino guardado" (o "Sumamos esta vez a… / Deshacer").

## Patrones a copiar (con un archivo de ejemplo de cada uno)

| Necesito… | Copiá el patrón de |
|---|---|
| Un ViewModel simple que observa datos | `DiarioViewModel` (el estado sale de un `Flow` con `stateIn(WhileSubscribed(5_000))`) |
| Un formulario que **sobrevive a que Android cierre la app** | `RegistroViewModel` y `EditarViewModel` (guardan los campos en `SavedStateHandle`; la clase de campos es `Serializable`) |
| Una pantalla que muestra estados `Cargando / Contenido / NoEncontrado` | `PantallaDetalle` + `DetalleViewModel` |
| Un componente reutilizable sin estado | `ItemVino`, `Banner`, `ChipsDeOpciones` |
| Una hoja inferior o un diálogo | `HojaDeAviso` (base), `HojaMismoVino`, el diálogo de eliminar de `PantallaDetalle` |
| Un aviso con "Deshacer" que sigue visible después de navegar | `SorboNavHost` (el `SnackbarHostState` vive en `AppSorbo` y se lo pasa al mapa de pantallas) |
| Cámara, galería y permiso | `rememberAccionesDeFoto` en `ui/components/AccionesDeFoto.kt` |
| Una regla de negocio probada | `RegistrarDegustacion`, `BuscarVinoParecido` (clase con `operator fun invoke`) |
| Un test de ViewModel | `DetalleViewModelTest`, `RegistroViewModelTest` (despachador de prueba, repositorio en memoria, `first { ... }`) |
| Un test de caso de uso | `RegistrarDegustacionTest`, `InterpretarEtiquetasRealesTest` |

### Receta: agregar una pantalla nueva

1. En `ui/viewmodels`: una `sealed interface Estado...` y un `ViewModel` que recibe sus dependencias por constructor (contratos, no implementaciones). `MutableStateFlow` privado, `StateFlow` público.
2. **Primero los tests del ViewModel** (con las versiones simuladas), después el código.
3. En `ui/screens`: la pantalla `Pantalla...`, que recibe el ViewModel y callbacks de navegación. Usá componentes de `ui/components`.
4. Textos en `strings.xml` (dentro de la sección de tu pantalla).
5. La ruta en `Rutas.kt` y el `composable(...)` en `SorboNavHost.kt`, donde se arma el ViewModel con `viewModel { ... }` tomando las dependencias de `contenedor`.
6. Probala en el teléfono con la lista de `06-como-probar.md`.

## Reglas de negocio que no hay que romper

La regla completa está en la sección 9 de la preentrega. Lo esencial:

- **Cada añada es un vino distinto.** Nombres iguales y misma añada: es el mismo vino. Nombres parecidos: se **pregunta** ("¿Es el mismo vino?"). Si solo cambia la añada, la ficha muestra "Otras añadas que probaste".
- **Una búsqueda sin resultados dice "No lo encontramos en tu diario"**, nunca "no lo probaste".
- **Nada sugerido por la lectura de la etiqueta se guarda sin que el usuario toque "Guardar".** Siempre es editable.
- **Foto de hasta 500 KB.** Solo dos campos obligatorios: nombre y si gustó.
- **Borrado lógico:** eliminar marca `eliminado = true` (también en las degustaciones) y se puede deshacer. Es lo que después se propaga a la nube.
- **El resumen con IA envía solo estadísticas, nunca fotos ni notas**, y exige al menos 5 degustaciones.

## Decisiones tomadas (y por qué)

| Decisión | Motivo |
|---|---|
| Contratos del dominio + versiones simuladas en `data/memoria` | Permite que cada uno avance en paralelo sin esperar a los demás; los tests y las pantallas no dependen de Room ni de la red |
| Estado de los formularios en `SavedStateHandle` | Android puede cerrar la app mientras se usa la cámara; sin esto se perdía lo escrito |
| Las fotos solo se borran cuando el usuario **cierra el formulario** (`alCerrar`), nunca en `onCleared` | `onCleared` también se ejecuta cuando el sistema destruye la pantalla y después la restaura: borrar ahí perdería la foto |
| Los avisos con "Deshacer" viven en `AppSorbo` | Si vivieran en cada pantalla, desaparecerían al navegar al detalle |
| ML Kit lee la foto derecha **y girada 90° a cada lado** | Muchas botellas se sacan acostadas; las lecturas giradas solo completan lo que falta, nunca pisan |
| APK de entrega solo `arm64-v8a`; el de desarrollo conserva todas las arquitecturas | El de entrega baja de 50 a 21 MB; los emuladores x86 siguen funcionando en desarrollo |
| ML Kit como lector base; IA en la nube solo como mejora opcional | Ver [`docs/evaluacion-lectura-etiquetas.md`](../evaluacion-lectura-etiquetas.md) |

## Trampas que ya encontramos (para que no te las comas)

1. **Los datos viven en memoria**: al cerrar la app o que Android la mate, vuelven los vinos de ejemplo. No es un bug; es lo que Room viene a resolver.
2. **`./gradlew` con un JDK demasiado nuevo** falla con un mensaje que solo dice un número de versión. Usá JDK 17 o 21.
3. **El permiso de internet no está declarado** en `AndroidManifest.xml` (hoy no hace falta). Apenas se use la red hay que agregar `INTERNET` y `ACCESS_NETWORK_STATE`; sin eso las llamadas fallan sin explicación.
4. **Un `BACK` con el teclado cerrado cierra la pantalla.** Al probar con comandos, verificá antes si el teclado está abierto.
5. **El selector de fecha de Material mide 360 dp fijos**: en pantallas más angostas se recortaba. Ya está resuelto en `FilaFecha.kt` (usa entrada por teclado en pantallas chicas); no volver al `DatePickerDialog` común.
6. **La lectura de la etiqueta es unas 8 veces más lenta con la pantalla apagada o en reposo.** Leer siempre con la pantalla encendida (es el caso normal de uso).
7. **Con el teclado abierto, el botón "Guardar" tiene que subir** (`imePadding()`); ya está en los formularios, mantenelo en los nuevos.
8. **El selector de fotos del sistema** solo da acceso a la foto elegida; la app copia la foto a su carpeta privada, no guarda la dirección original.
9. **`PantallaEnConstruccion` y `GaleriaComponentes` son temporales.** La primera se reemplaza pantalla por pantalla; la segunda (accesible desde Ajustes) se borra cuando exista Ajustes real.
10. **Los tests de Room no pueden ser tests unitarios comunes**: necesitan un teléfono o emulador (`androidTest`). Ver el documento `03`.

## Estado de las pantallas

| Pantalla | Ruta | Estado |
|---|---|---|
| Diario (con contenido, vacío, cargando) | `diario` | ✅ Falta: "Sin conexión" y "Error" (A3, A4), y la bienvenida (O1, O2) |
| Buscar | `buscar` | ✅ Falta: búsquedas recientes (B1) |
| Registrar (formulario, foto, fecha, vino parecido, lectura de etiqueta) | `registrar?nombre=` | ✅ |
| Detalle, Editar, Lo volví a probar, eliminar con deshacer | `detalle/{vinoId}`, `editar/…`, `volver_a_probar/…` | ✅ |
| Mis gustos | `gustos` | ✅ solo estadísticas locales. Falta todo lo de IA (E2–E7) |
| Ajustes | `ajustes` | ⏳ provisoria (G1, G2) |
| Cuenta, iniciar sesión, crear cuenta, recuperar y cambiar contraseña, privacidad | `cuenta`, `iniciar_sesion`, … | ⏳ provisorias (F1–F8, G3–G7) |
