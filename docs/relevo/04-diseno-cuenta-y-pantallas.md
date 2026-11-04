# Diseño de pantallas, cuenta y accesibilidad

*Para Nino (tareas N1–N6). Los códigos de pantalla (O2, F1, G3…) son los de los mockups: `docs/mockups/index.html` y las imágenes en `docs/mockups/png/<código>.png`. Abrí el índice para ver cada pantalla con su descripción.*

## Cómo se trabaja sin esperar a nadie

Los contratos ya existen y tienen versiones simuladas funcionando en `AppContainer`:

| Necesitás… | Usá | Versión simulada que ya anda |
|---|---|---|
| Iniciar sesión, crear cuenta, etc. | `SesionRepository` | `SesionRepositoryEnMemoria` |
| Bienvenida vista, aviso de IA, búsquedas recientes | `PreferenciasRepository` | `PreferenciasRepositoryEnMemoria` |
| Saber si hay internet | `ConectividadObserver` | `ConectividadSimulada` |
| Generar el resumen con IA | `AnalisisGustosRepository` | `AnalisisGustosRepositoryEnMemoria` |

Las simuladas permiten **provocar cada caso** desde los tests (`fallaConMotivo`, `hayConexion`, `cambiar(false)`). Cuando Iñaki y Alejo terminen las reales, se cambia una línea en `AppContainer` y tus pantallas siguen funcionando igual. **Si tu pantalla necesita algo que el contrato no ofrece, no lo inventes en el ViewModel: hablalo** (ver regla de cambios de contrato en `02`).

Todo lo que se muestra al usuario sale de `strings.xml`. Los errores se muestran en lenguaje simple y siempre con una acción posible.

---

## N1 — Bienvenida (O1, O2, O3)

- **O1 Splash:** la pantalla de arranque del sistema (tema del splash) con el color de marca. No hace falta una pantalla propia en Compose.
- **O2 Bienvenida:** *única pantalla de onboarding, un solo botón.* Se muestra mientras `bienvenidaVista` sea `false`; al continuar, `marcarBienvenidaVista()`. Debe poder iniciar sesión desde ahí ("Ya tengo cuenta").
- **O3 Diario vacío:** ya está hecha (estado vacío del Diario).
- Lo difícil: decidir la pantalla inicial **sin parpadeo**. La preferencia se lee de forma asíncrona; mientras carga, mostrar la pantalla de arranque en vez de mostrar el Diario y saltar a la bienvenida.
- **Tests:** el ViewModel muestra la bienvenida si no se vio y no la muestra si ya se vio.

## N2 — Estados del Diario y de Buscar (A3, A4, B1)

- **A3 Sin conexión:** un banner discreto, la app sigue funcionando. Usar `ConectividadObserver.hayConexion` y el componente `Banner` que ya existe. Con cuenta y pendientes, "N vinos sin respaldar" (el número llega con la tarea I5; mientras tanto, solo el aviso de que no hay conexión).
- **A4 Error:** mensaje en lenguaje simple con "Reintentar". Pensá en qué falla de verdad al leer el diario local (poco); que el estado exista y se pueda probar con un repositorio que lance un error en un test.
- **B1 Buscador abierto:** al abrir el buscador sin texto, mostrar las **búsquedas recientes** (`PreferenciasRepository.busquedasRecientes`) tocables y con "Borrar". Se agrega una búsqueda reciente cuando el usuario **confirma** una búsqueda (no en cada letra). Sumar el aviso de que busca sin conexión.
- Ya hecho y que no hay que romper: la búsqueda sin resultados dice **"No lo encontramos en tu diario"** y ofrece registrar con el nombre cargado.

## N3 — Cuenta (F1–F8)

| Código | Pantalla | Detalle |
|---|---|---|
| F1 | Iniciar sesión | Email y contraseña, enlaces a "Olvidé mi contraseña" y "Crear cuenta" |
| F2 | Error al iniciar sesión | **Mensaje único** que no dice cuál de los dos datos falló (`CREDENCIALES_INCORRECTAS`) |
| F3 | Crear cuenta | Nombre, email, contraseña **con el requisito visible** y aceptación de términos |
| F4 | Recuperar contraseña | Solo el email |
| F5 | Revisá tu email | Botón que abre la app de correo del teléfono |
| F6 | Nueva contraseña | Se abre desde el enlace del email (deep link, decisión #4 de `02`) |
| F7 | Vinos del teléfono | Al crear la cuenta, los vinos cargados antes se respaldan; mostrar cuántos |
| F8 | Restaurar diario | Al iniciar sesión en un teléfono nuevo; progreso. Su lógica la hace I6; vos hacés la pantalla |

- **Validación antes de llamar:** `esEmailValido` y `esClaveValida` de `domain/util/Credenciales.kt` (ya testeadas). No repetir las reglas en el ViewModel.
- **Cada `MotivoCuenta` tiene un mensaje propio**: credenciales incorrectas, email ya registrado, email inválido, clave débil, sin conexión, sin sesión, error desconocido. Un `when` exhaustivo para que el compilador avise si se agrega un motivo nuevo.
- Mientras se espera la respuesta: botón deshabilitado y un indicador; evitar el doble toque.
- Los datos escritos no se pierden al rotar ni si Android cierra la app: mismo patrón de `SavedStateHandle` que `RegistroViewModel` (**pero nunca guardar contraseñas en él**: la contraseña vive solo en memoria).
- **Tests de ViewModel** para cada motivo de error, para validaciones y para el camino feliz.

## N4 — Ajustes (G1–G7)

| Código | Pantalla | Detalle |
|---|---|---|
| G1 | Ajustes con sesión | Cuenta, estado del respaldo y control de la IA (`iaActivada`) |
| G2 | Ajustes sin sesión | Invitación a crear cuenta; la IA aparece deshabilitada |
| G3 | Mi cuenta | Perfil y acciones |
| G4 | Cambiar contraseña | Contraseña actual y nueva |
| G5 | Cerrar sesión con pendientes | Avisa si hay vinos sin respaldar y deja elegir |
| G6 | Eliminar cuenta | Confirmación con la consecuencia explícita (se borra todo, también de la nube) |
| G7 | Cómo usamos tus datos | Privacidad en lenguaje simple: qué se guarda, qué sale del teléfono y qué no |

- Usá el componente `ItemAjustes(titulo, icono, subtitulo, final, onClick)` ya existente.
- Al terminar: **borrar `GaleriaComponentes` y su ruta `GALERIA`**, y `PantallaEnConstruccion` cuando ya no la use nadie. Son andamios temporales.
- G7 tiene que decir la verdad de lo que hace la app: las fotos no salen del teléfono salvo respaldo con cuenta; la lectura de etiquetas es local; el resumen con IA envía solo estadísticas (cuántos te gustaron, por tipo, por cepa…), nunca fotos ni notas. Si se hace la lectura en la nube (A4 opcional), este texto cambia: coordinar con Alejo.
- El número de pendientes de G1 y G5 llega con I5. Hasta entonces, simulado.

## N5 — Mis gustos con IA (E2–E7)

La pantalla de estadísticas locales ya está hecha (E1). Agregar el bloque del resumen con IA:

| Código | Estado | Detalle |
|---|---|---|
| E7 | Aviso de IA (primer resumen) | Consentimiento antes de enviar estadísticas la primera vez: `avisoIaAceptado` / `aceptarAvisoIa()` |
| E2 | Generando | Carga con un mensaje de qué está pasando |
| E3 | Análisis | Resumen y sugerencias (`AnalisisGustos`) con la fecha en que se generó |
| E4 | Sin conexión | Los números siguen; el resumen anterior se muestra con su fecha |
| E5 | Requiere cuenta | `MotivoAnalisis.SIN_SESION` |
| E6 | Error | `LIMITE_ALCANZADO`, `SERVICIO_NO_DISPONIBLE`, `ERROR_DESCONOCIDO`: mensajes distintos y reintento sin perder lo que ya se ve |

- **Mínimo 5 degustaciones** para ofrecer el botón (si hay menos, explicar cuántas faltan).
- El resumen anterior se lee con `AnalisisGustosRepository.observarUltimo()`; generar uno nuevo es `generar(estadisticas)`, con las `EstadisticasGustos` que ya calcula `CalcularEstadisticasGustos`. Esa clase **no** incluye fotos ni notas: no agregarlas nunca.
- Si `iaActivada` es `false` (Ajustes), no mostrar el botón de generar.
- Con la versión simulada se puede recorrer todo: `fallaConMotivo = MotivoAnalisis.X` provoca cada error.

## N6 — Accesibilidad y pulido

Cada pantalla, nueva y existente, tiene que cumplir:

- **Objetivos táctiles de al menos 48 dp.**
- **Descripción de contenido** en todo icono y botón que no tenga texto; el texto que lee TalkBack tiene sentido solo (no "botón 1").
- **Estado que no dependa solo del color** (el "Me gustó / No me gustó" ya lleva icono y texto).
- **Fuente al 130 %** (y al máximo del sistema): nada recortado, todo se puede desplazar.
- **Pantalla de 320 dp de ancho**: sin cortes ni scroll horizontal (el selector de fecha ya se resolvió, ver `01`).
- **Tema oscuro**: contraste legible.
- **Teclado:** con el teclado abierto, el botón principal sigue visible (`imePadding()`).
- **Orden de foco** lógico y títulos de pantalla marcados como encabezado.
- **TalkBack real**: hoy solo se auditó el árbol de accesibilidad con herramientas; falta recorrer las pantallas con TalkBack encendido. Es parte de N6.

Cómo probar cada punto en el teléfono: `06-como-probar.md`.

## Tests de ViewModel: lo que se espera

Cada ViewModel nuevo tiene un test que cubre: el estado inicial, cada transición de estado, **cada rama de error** y cada acción del usuario. Seguí el estilo de `DetalleViewModelTest` y `RegistroViewModelTest`: despachador de prueba, repositorio simulado, esperar el estado con `first { ... }`, y comprobar el resultado, no la implementación. Para estar seguro de que un test sirve: romper a propósito la lógica y ver que falla (y deshacerlo).
