# Sorbo — Preentrega de análisis y diseño

*Desarrollo de Aplicaciones I — UADE. Los campos marcados como `[COMPLETAR]` quedan pendientes de definir por el equipo.*

---

## 1. Nombre provisorio

**Sorbo**

---

## 2. Descripción del problema

**¿Qué problema existe?**
Las personas que toman vino de forma ocasional no recuerdan qué vinos probaron ni si les gustaron. Al momento de comprar eligen por precio, por la etiqueta o al azar, y repiten malas experiencias o no logran volver a encontrar un vino que sí disfrutaron.

**¿Quién lo experimenta?**
Consumidores no expertos, que compran vino algunas veces por mes para cenas, asados, reuniones o regalos, y que no manejan vocabulario técnico de cata.

**¿En qué contexto ocurre?**
En dos momentos:
1. Cuando toman el vino (bar, restaurante, casa de amigos, cena), que es cuando se forma la opinión.
2. Cuando están frente a la góndola de una vinoteca o supermercado, que es cuando necesitan recordar esa opinión.

**¿Cómo se resuelve actualmente?**
Con la memoria, sacando fotos sueltas que quedan perdidas en la galería, anotando en apps de notas, o usando apps como Vivino.

**¿Qué dificultades presenta la solución actual?**
- La memoria falla.
- Las fotos en la galería no tienen contexto (¿me gustó? ¿con qué lo tomé?) ni se pueden buscar.
- Las apps existentes están orientadas a la opinión de la comunidad y al catálogo global, usan vocabulario de sommelier, piden muchos datos y dependen de conexión, algo que en sótanos de vinotecas o supermercados suele fallar.

**¿Por qué una app móvil podría mejorarlo?**
Porque ambos momentos ocurren lejos de una computadora, con el celular en la mano: la cámara permite registrar la etiqueta en segundos, el almacenamiento local permite consultar el historial sin señal en el punto de venta, y la app acompaña la situación sin obligar a cargar datos complejos.

---

## 3. Usuario principal

| Aspecto | Descripción |
|---|---|
| Perfil | Persona de 25 a 45 años, consumidora ocasional de vino, no experta. Usa el celular con soltura. |
| Necesidad principal | Saber rápidamente si ya probó un vino y qué le pareció, y entender qué tipo de vinos le gustan según su propia experiencia. |
| Contexto de uso | Registro en bares, restaurantes o reuniones; consulta parada frente a una góndola. |
| Frecuencia estimada | 2 a 6 registros por mes; consultas en cada compra (1 a 4 veces por mes). |
| Restricciones | Poco tiempo (menos de 30 segundos en la góndola), una sola mano libre (la otra con la botella o el carrito), situaciones sociales donde no quiere pasar mucho tiempo con el celular. |
| Conectividad | Intermitente o nula en sótanos, supermercados grandes y algunos bares. |
| Condiciones ambientales | Poca luz en bares y restaurantes, ruido, distracción, posible consumo de alcohol (se requiere una interfaz simple y tolerante a errores). |
| Dificultades actuales | No recuerda, no encuentra las fotos, no entiende la terminología de las apps especializadas. |

---

## 4. Contexto de uso

**Escenario A — Registro.** Lucía está en una cena y le gusta mucho el vino. Saca el celular, toma una foto de la etiqueta, marca "me gustó", "seco", "carne" y "con amigos", y guarda. Todo en menos de un minuto, aun sin señal en el lugar.

**Escenario B — Consulta.** Semanas después, en la vinoteca, duda entre dos botellas. Escribe parte del nombre y la app le muestra que uno ya lo probó dos veces y le gustó con carne. Funciona aunque el local no tenga señal.

**Mirada de computación ubicua (Weiser).** La app busca integrarse a la actividad sin interrumpirla:
- La foto reemplaza el tipeo.
- Las opiniones se cargan con opciones simples ("me gustó / no", "dulce / seco") en lugar de texto libre o puntajes técnicos.
- La consulta en góndola se resuelve en pocos toques.

---

## 5. Propuesta de solución

- **Nombre provisorio:** Sorbo.
- **Descripción breve:** diario personal de vinos. El usuario registra cada vino que prueba con una foto de la etiqueta y una opinión simple (me gustó / no, dulce / seco, comida, compañía, ocasión). Puede buscar en su historial para saber si ya lo probó y, con conexión, usar IA para autocompletar los datos del vino desde la foto y obtener un análisis de sus propios gustos.
- **Usuario principal:** consumidor ocasional, no experto.
- **Propuesta de valor:** en lugar de decir qué opina el mundo de un vino, la app arma un espejo de los gustos propios del usuario, con vocabulario simple.
- **Escenario principal de uso:** consultar en la vinoteca si un vino ya fue probado y qué opinión se tuvo.
- **Beneficio esperado:** menos compras fallidas, posibilidad de repetir los vinos que gustaron y mayor autoconocimiento de las preferencias sin necesitar conocimientos técnicos.

---

## 6. ¿Por qué tiene que ser una app móvil?

- **Cámara:** la foto de la etiqueta es la forma natural de registrar un vino en el momento; en un sitio web o de escritorio el registro ocurriría tarde o nunca.
- **Funcionamiento offline:** la consulta ocurre en lugares con mala señal; un sitio web no funcionaría ahí.
- **Almacenamiento local:** el historial vive en el dispositivo y está disponible al instante.
- **Contexto de uso:** los dos momentos clave (tomar y comprar) ocurren en movimiento, fuera de casa, con el celular como único dispositivo disponible.
- **Funciona con un solo usuario:** no depende de una comunidad para aportar valor desde el primer registro.

---

## 7. Caso de negocio y propuesta de valor

**Problema → Usuario → Solución → Valor generado**

> No recuerdo qué vinos probé ni si me gustaron → consumidor ocasional no experto → diario personal con foto, opinión simple, búsqueda offline y análisis de gustos con IA → elijo mejor, repito lo que me gustó y entiendo mis preferencias sin ser sommelier.

- **Valor de experiencia:** compras con más seguridad, menos frustración.
- **Valor económico para el usuario:** menos dinero gastado en vinos que no le gustan.
- **Valor de accesibilidad:** vocabulario simple, apto para personas sin conocimientos de cata.
- **Diferencial frente a apps existentes:** no compite en catálogo ni comunidad; se enfoca en el historial y los gustos propios, funciona offline y es útil desde el primer día con un solo usuario.
- **Posible evolución comercial (fuera de alcance):** modelo freemium (análisis de IA ilimitado como función paga) o alianzas con vinotecas.

---

## 8. Alcance y fuera de alcance

**Incluido en la versión 1:**
- Registro de vinos probados con foto y opinión simple (offline).
- Historial, detalle, edición y eliminación de registros (offline).
- Búsqueda en el historial por texto (offline).
- Autocompletado de datos del vino desde la foto mediante IA (requiere conexión; se encola si no hay).
- Análisis de gustos personales mediante IA (requiere conexión; si no hay, se muestra el último análisis guardado).
- Respaldo de los datos en la nube (Supabase) para no perder el historial al cambiar de dispositivo.

**Fuera de alcance:**
- Catálogo global de vinos, puntajes o reseñas de otros usuarios.
- Funciones sociales (seguir usuarios, compartir, comentarios).
- Precios, compras o integración con tiendas.
- Uso simultáneo en varios dispositivos con edición concurrente.
- Notas de cata técnicas (aromas, taninos, etc.).
- Versión iOS o web.

---

## 9. Requisitos funcionales

### RF01 — Registrar un vino probado
- **Usuario:** consumidor ocasional.
- **Descripción:** el usuario podrá registrar un vino tomando una foto de la etiqueta (o eligiéndola de la galería) e ingresando nombre, opinión (me gustó / no me gustó), percepción (dulce / seco), comida con la que lo acompañó, con quién lo tomó, ocasión y una nota opcional.
- **Criterio de aceptación:** al confirmar, el registro queda guardado en el dispositivo y aparece inmediatamente en el historial, aun con el dispositivo en modo avión. Los únicos campos obligatorios son el nombre y la opinión.

### RF02 — Consultar si ya probé un vino
- **Usuario:** consumidor ocasional, en el punto de venta.
- **Descripción:** el usuario podrá buscar por nombre o bodega en su historial y ver si ya probó el vino, cuántas veces y qué opinó en cada ocasión.
- **Criterio de aceptación:** la búsqueda funciona sin conexión, tolera coincidencias parciales, no distingue mayúsculas ni tildes, y muestra resultados en menos de 1 segundo para un historial de 500 registros. Si no hay resultados, se muestra un estado vacío con la opción de registrar el vino.

### RF03 — Autocompletar datos del vino con IA a partir de la foto
- **Usuario:** consumidor ocasional, al registrar.
- **Descripción:** a partir de la foto de la etiqueta, la app enviará la imagen a un modelo de IA con visión que devolverá una sugerencia de nombre, bodega, cepa y año. El usuario podrá aceptar, editar o descartar la sugerencia.
- **Criterio de aceptación:**
  - Con conexión, la sugerencia se muestra en el formulario y nunca se guarda sin confirmación del usuario.
  - Sin conexión, el registro se guarda igual con los datos cargados a mano y la foto queda marcada como "pendiente de análisis"; al recuperar conexión se procesa automáticamente y se notifica al usuario para que confirme la sugerencia.
  - Si la IA falla, se informa el error y el registro manual sigue disponible.

### RF04 — Ver análisis de mis gustos
- **Usuario:** consumidor ocasional.
- **Descripción:** la app generará, mediante IA, un análisis en lenguaje simple de los gustos del usuario a partir de su historial (por ejemplo: "te gustan más los vinos secos", "tus mejores experiencias fueron con carne"), junto con recomendaciones del tipo de vino a probar.
- **Criterio de aceptación:**
  - El análisis se habilita a partir de 5 registros; con menos se muestra un estado vacío que explica cuántos faltan.
  - Con conexión se genera o actualiza a pedido.
  - Sin conexión se muestra el último análisis guardado con su fecha de generación y un aviso de que se necesita conexión para actualizarlo.

---

## 10. Requisitos no funcionales

| ID | Tipo | Requisito |
|---|---|---|
| RNF01 | Comportamiento offline | RF01, RF02 y la edición o eliminación de registros deben funcionar con el dispositivo en modo avión. |
| RNF02 | Usabilidad | Un registro debe poder completarse con un máximo de 5 toques además de la foto. La búsqueda debe ser accesible desde la pantalla principal con un toque. |
| RNF03 | Accesibilidad | Elementos táctiles de al menos 48dp, contraste mínimo WCAG AA, soporte de TalkBack con descripciones en todos los íconos y respeto del tamaño de fuente del sistema. |
| RNF04 | Seguridad | Ninguna API key de servicios de IA se incluye en la aplicación; las llamadas se realizan a través de una función de servidor (Supabase Edge Function). Los datos remotos se protegen con Row Level Security para que cada usuario acceda solo a sus registros. |
| RNF05 | Privacidad | Las fotos y el historial solo se envían al servicio de IA cuando el usuario ejecuta explícitamente una función de IA; la app lo informa en el primer uso. |
| RNF06 | Confiabilidad | Los registros creados sin conexión se sincronizan automáticamente con la nube al recuperar conexión, sin intervención del usuario y sin pérdida de datos. |
| RNF07 | Recuperación ante errores | Ante un fallo de red o de la IA, la app muestra un mensaje claro y una acción de reintento, sin perder los datos ingresados. |
| RNF08 | Permisos | Si el usuario rechaza el permiso de cámara, puede elegir una foto de la galería o registrar sin foto. |
| RNF09 | Rendimiento y almacenamiento | Las fotos se comprimen antes de guardarse (máximo 500 KB por imagen). |
| RNF10 | Compatibilidad | Android 8.0 (API 26) o superior. |

---

## 11. Diseño en Figma

**Link:** https://www.figma.com/design/NM0utJz4cc9JBflTfu1ep9/Sorbo-App

Las pantallas se prototiparon primero en HTML ([docs/mockups/index.html](mockups/index.html)) siguiendo Material 3 y luego se importaron a Figma. Incluyen 25 frames: todas las pantallas con sus estados, variantes de 320dp y 412dp de ancho y tema oscuro.

**Lineamientos de diseño:**
- **Material Design 3**, el sistema de diseño nativo de Android y el que implementa Jetpack Compose (`androidx.compose.material3`), para que el diseño se traslade directo a componentes reales.
- **Diseño adaptable:** pensado sobre una base de 360dp de ancho y validado para pantallas de 320dp a 412dp. Las listas y formularios ocupan el ancho disponible, sin medidas fijas, y respetan el tamaño de fuente configurado en el sistema.
- **Uso con una mano:** las acciones principales (registrar, guardar, navegar) están en el tercio inferior de la pantalla, en la zona que alcanza el pulgar.
- **Color con significado:** bordó como color principal de la marca. Ninguna información depende solo del color: "me gustó / no me gustó" siempre se muestra con ícono y texto.

**Decisiones de UX (heurísticas de Nielsen):**

| Heurística | Cómo se aplica |
|---|---|
| Visibilidad del estado del sistema | Indicador de sin conexión, de registros pendientes de sincronizar y de "analizando foto"; fecha de última actualización en el análisis de gustos. |
| Relación con el mundo real | Vocabulario cotidiano ("me gustó", "dulce", "con amigos") en lugar de términos de cata. |
| Control y libertad del usuario | La sugerencia de IA siempre se puede editar o descartar; eliminar ofrece "Deshacer". |
| Consistencia y estándares | Componentes estándar de Material 3 (barra de navegación inferior, botón flotante, chips, snackbars). |
| Prevención de errores | Solo dos campos obligatorios; opciones con chips en lugar de texto libre; confirmación antes de eliminar. |
| Reconocer antes que recordar | Las comidas y ocasiones usadas recientemente aparecen primero; la foto identifica cada vino en la lista. |
| Flexibilidad y eficiencia | El botón de registrar abre directamente la cámara; la búsqueda está siempre visible en la pantalla principal. |
| Diseño minimalista | La respuesta a "¿ya lo probé?" se muestra primero y en grande; el detalle queda un toque más abajo. |
| Ayuda ante errores | Mensajes en lenguaje simple con una acción concreta ("Reintentar", "Cargar a mano", "Elegir de la galería"). |

**Pantallas principales:**
1. **Inicio / Historial:** lista de vinos probados con foto, nombre y opinión; buscador fijo arriba; botón flotante "Registrar vino".
2. **Búsqueda / Resultado:** "Ya lo probaste" (con veredicto y cantidad de veces) o "Todavía no lo probaste" (con acceso a registrarlo).
3. **Registrar vino:** foto, sugerencia de IA, opinión con chips de selección rápida, guardar.
4. **Detalle del vino:** foto, datos, historial de pruebas, editar y eliminar.
5. **Mis gustos:** análisis de IA, fecha de última actualización y botón para actualizar.
6. **Ajustes / Cuenta:** estado del respaldo, iniciar sesión con email y contraseña, restaurar datos y cerrar sesión.

**Navegación:** barra inferior con Historial, Mis gustos y Ajustes, más un botón flotante para registrar.

**Estados a representar:**

| Pantalla | Estados |
|---|---|
| Historial | Carga, con contenido, vacío ("Registrá tu primer vino"), error. |
| Búsqueda | Con resultados, sin resultados. |
| Registrar | Analizando foto con IA (carga), sugerencia lista, error de IA, sin conexión ("Se analizará cuando tengas conexión"), permiso de cámara rechazado. |
| Mis gustos | Menos de 5 registros (vacío), generando (carga), análisis disponible, análisis desactualizado sin conexión, error. |
| Global | Indicador de sin conexión / sincronizando / registros pendientes de sincronizar. |

---

## 12. Flujo de pantallas

**Caso de uso principal: consultar en la vinoteca si ya probé un vino.**

```
[Inicio / Historial]
      |
      v
 Toca buscador y escribe nombre
      |
      v
 ¿Hay coincidencias en el historial local?
      |-- Sí --> [Resultado: "Ya lo probaste"] --> [Detalle del vino] --> FIN (decide compra)
      |
      '-- No --> [Resultado: "Todavía no lo probaste"]
                       |
                       |-- No registra --> FIN
                       |
                       '-- Registrar --> [Registrar vino]
                                              |
                                              v
                                        Toma foto de la etiqueta
                                              |
                                              v
                                        ¿Hay conexión?
                                         |-- Sí --> IA sugiere datos --> Usuario confirma/edita
                                         '-- No --> Carga manual + foto "pendiente de análisis"
                                              |
                                              v
                                        Guardar (Room) --> [Detalle del vino] --> FIN
```

*Diagrama editable en [docs/diagramas/sorbo.drawio](diagramas/sorbo.drawio) (página "Flujo de pantallas").*

**Operaciones críticas:**
- Guardar registro: debe persistir siempre.
- Confirmar sugerencia de IA: nunca es automática.
- Eliminar registro: con confirmación y opción de deshacer.

---

## 13. Arquitectura propuesta

Arquitectura MVVM con principios de Clean Architecture, organizada en tres capas. Room es la **fuente única de verdad**: la UI siempre lee de la base local y la sincronización con la nube ocurre en segundo plano.

| Capa | Responsabilidades | Componentes |
|---|---|---|
| Presentación | UI, estado de pantalla, eventos del usuario | Pantallas en Jetpack Compose, ViewModels con `StateFlow<UiState>`, Navigation Compose |
| Dominio | Modelos y reglas de negocio, independientes de Android | Modelos (`Vino`, `Prueba`, `AnalisisGustos`), casos de uso (`RegistrarPrueba`, `BuscarEnHistorial`, `SugerirDatosDesdeFoto`, `ObtenerAnalisisGustos`), interfaces de repositorio |
| Datos | Acceso a fuentes locales y remotas, mapeo y sincronización | Implementaciones de repositorio, Room (DAOs y entidades), Supabase (Postgrest, Storage, Functions), WorkManager para sincronización, mappers |

### Diagrama de flujo de datos

```
Compose UI
   |  eventos            ^ UiState (StateFlow)
   v                     |
ViewModel ---------------'
   |
   v
Casos de uso (Dominio)
   |
   v
Repositorio (interfaz en Dominio, implementación en Datos)
   |                         |
   v                         v
Local: Room + archivos     Remoto: Supabase
(fuente de verdad)         (Postgrest, Storage, Edge Functions)
   ^                         |
   '----- SyncWorker --------'
        (WorkManager)
```

### Diagrama de topología de infraestructura

```
+-----------------------------+
|  Dispositivo Android        |
|  - App (Kotlin + Compose)   |
|  - Room (SQLite)            |
|  - Fotos (almacenamiento    |
|    interno)                 |
|  - WorkManager              |
+--------------+--------------+
               | HTTPS
               v
+-----------------------------+
|  Supabase                   |
|  - Auth (usuario)           |
|  - Postgres + RLS (backup)  |
|  - Storage (fotos)          |
|  - Edge Functions           |
|      analizar-etiqueta      |
|      analizar-gustos        |
+--------------+--------------+
               | HTTPS (API key solo en servidor)
               v
+-----------------------------+
|  Groq (LLM con visión)      |
+-----------------------------+
```

*Diagramas editables en [docs/diagramas/sorbo.drawio](diagramas/sorbo.drawio) (páginas "Arquitectura - Flujo de datos" y "Topología de infraestructura").*

---

## 14. Persistencia y estrategia Offline First

### Entidades principales

- **Vino:** `id` (UUID), `nombre`, `bodega`, `cepa`, `anio`, `fotoPath` (local), `fotoUrl` (remota), `estadoAnalisis` (sin foto / pendiente / analizado / error), `createdAt`, `updatedAt`, `syncState`.
- **Prueba:** `id` (UUID), `vinoId`, `fecha`, `meGusto`, `dulzor` (dulce / seco), `comida`, `compania`, `ocasion`, `nota`, `updatedAt`, `syncState`, `eliminado` (borrado lógico para sincronizar).
- **AnalisisGustos:** `id`, `texto`, `generadoEn`, `cantidadRegistrosUsados`.

**Relaciones:** un Vino tiene muchas Pruebas (1:N), porque el mismo vino puede probarse varias veces con distintas opiniones.

*Diagrama editable en [docs/diagramas/sorbo.drawio](diagramas/sorbo.drawio) (página "Modelo de datos").*

- **Información generada por el usuario:** vinos, pruebas y fotos.
- **Información proveniente de APIs:** sugerencias de datos del vino y análisis de gustos (IA).
- **Almacenamiento local:** todo lo anterior en Room; las fotos en almacenamiento interno, con la ruta guardada en Room. Las preferencias (onboarding visto, aviso de privacidad aceptado) en DataStore.

### Comportamiento según conectividad

| Situación | Comportamiento |
|---|---|
| Con conexión | Todas las funciones disponibles. Los cambios se guardan en Room y se sincronizan con Supabase en segundo plano. |
| Pierde conexión | Registro, historial, búsqueda, edición y eliminación siguen funcionando. Las funciones de IA muestran "Requiere conexión". Los registros nuevos quedan con `syncState = PENDIENTE` y las fotos con `estadoAnalisis = PENDIENTE`. |
| Recupera conexión | WorkManager (con restricción de red) sube los registros pendientes, envía las fotos pendientes a analizar y notifica al usuario para que confirme las sugerencias. |
| Información local desactualizada | El análisis de gustos muestra su fecha de generación y cuántos registros nuevos hay desde entonces, con un botón para actualizar. |
| Sin información local | Estado vacío con acceso a "Registrar tu primer vino". Si el usuario ya tenía respaldo, se ofrece restaurar desde la nube. |

### Estrategia

- **Qué se guarda localmente:** todos los vinos, pruebas, fotos y el último análisis de gustos.
- **Fuente remota:** Supabase (Postgres para datos, Storage para fotos, Edge Functions para IA).
- **Cuándo se actualizan los datos:** sincronización automática al guardar cambios si hay red; si no, al recuperar conexión vía WorkManager. El análisis de IA se actualiza a pedido del usuario.
- **Qué ve el usuario sin conexión:** todo su historial completo y un indicador discreto de "sin conexión" y de registros pendientes.
- **Errores y conflictos:** al usar un único dispositivo, se aplica "última escritura gana" según `updatedAt`. Los borrados son lógicos para que se propaguen correctamente. Si una sincronización falla, se reintenta con backoff exponencial y el registro sigue disponible localmente.

---

## 15. Tecnologías previstas y justificación

| Tecnología | Justificación |
|---|---|
| Kotlin + Android | Lenguaje y plataforma requeridos; acceso nativo a cámara, almacenamiento y tareas en segundo plano. |
| Jetpack Compose | UI declarativa que facilita representar los distintos estados de cada pantalla a partir de un `UiState`. |
| Navigation Compose | Navegación entre pantallas y paso de parámetros (por ejemplo, id del vino al detalle). |
| ViewModel + StateFlow | Mantener el estado de pantalla ante rotaciones y exponerlo de forma reactiva a la UI. |
| Coroutines + Flow | Operaciones asíncronas (base de datos, red) sin bloquear la UI; Room expone Flows que actualizan la UI automáticamente. |
| Room | Persistencia estructurada con relación 1:N (Vino–Prueba) y búsqueda; es la fuente de verdad que permite el funcionamiento offline. |
| DataStore | Preferencias simples (onboarding, aviso de privacidad aceptado). |
| ActivityResult `TakePicture` + Photo Picker | Capturar o elegir la foto de la etiqueta usando la cámara del sistema; es más simple que implementar una cámara propia con CameraX y cubre la necesidad. |
| Coil | Carga eficiente de imágenes locales y remotas en Compose. |
| WorkManager | Sincronización y análisis de fotos pendientes cuando vuelve la conexión, aunque la app esté cerrada. |
| Supabase (supabase-kt: Auth, Postgrest, Storage, Functions) | Backend completo en un solo servicio: autenticación con email y contraseña, respaldo de datos, almacenamiento de fotos y funciones de servidor. Tiene cliente para Kotlin recomendado en su documentación. El login es opcional: la app se usa completa sin cuenta y la cuenta solo habilita el respaldo y las funciones de IA. |
| Supabase Edge Functions | Intermediario hacia el proveedor de IA para no exponer la API key dentro de la aplicación. |
| Groq (LLM con visión) | Lee etiquetas con tipografías artísticas y devuelve datos estructurados (JSON) con mejores resultados que un OCR tradicional; también genera el análisis de gustos en lenguaje simple. Se eligió por tener un plan gratuito suficiente para el volumen del proyecto, baja latencia y una API compatible con OpenAI, lo que permite cambiar de proveedor modificando solo la Edge Function, sin tocar la app. |
| ConnectivityManager (`NetworkCallback`) | Detectar el estado de conexión para mostrar el indicador y habilitar o deshabilitar las funciones de IA. |

---

## 16. Repositorio y forma de trabajo

- **URL del repositorio:** https://github.com/alejogarciabosio/sorbo-app
- **Integrantes:** Alejo Garcia Bosio, Iñaki Courtinade, Nino Bizzotto.
- **Estrategia de ramas:** `main` (estable, solo vía pull request), `develop` (integración) y ramas por funcionalidad: `feature/rf01-registrar-vino`, `feature/rf02-busqueda`, etc.
- **Commits:** Conventional Commits (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`), pequeños y frecuentes.
- **Pull requests:** cada PR debe ser revisado por al menos otro integrante antes de integrarse a `develop`.
- **Organización:** tareas en GitHub Projects (o similar), vinculadas a cada requisito funcional.

---

## 17. Integrantes, roles y responsabilidades

| Integrante | Rol principal | Responsabilidades | Áreas en las que participa |
|---|---|---|---|
| Alejo Garcia Bosio | Arquitectura y documentación | Definición de capas y estructura del proyecto, configuración de Supabase (Auth, tablas, Row Level Security, Storage) y Edge Functions de IA, README y documentación de decisiones | Dominio, backend, documentación |
| Iñaki Courtinade | Integración, persistencia y sensores | Base de datos local con Room, sincronización con Supabase mediante WorkManager, manejo de conectividad, captura de fotos con la cámara | Datos, offline first, capacidades del dispositivo |
| Nino Bizzotto | UI, UX y testing | Diseño en Figma, pantallas en Jetpack Compose, estados de pantalla, navegación, accesibilidad y tests | Presentación, calidad |

**Cambios en los miembros del equipo:** ninguno.

*Los roles no son exclusivos: todos los integrantes participan en el desarrollo y comprenden el funcionamiento general de la aplicación.*
