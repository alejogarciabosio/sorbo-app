# Sorbo — Preentrega de análisis y diseño

*Desarrollo de Aplicaciones I — UADE · Equipo: Alejo Garcia Bosio, Iñaki Courtinade, Nino Bizzotto*

---

## 1. Nombre provisorio

**Sorbo** — diario personal de vinos.

---

## 2. Descripción del problema

**¿Qué problema existe?**
Las personas que toman vino de forma ocasional no recuerdan qué vinos probaron ni si les gustaron. Al momento de comprar eligen por precio, por la etiqueta o al azar, y repiten malas experiencias o no logran volver a encontrar un vino que sí disfrutaron.

**¿Quién lo experimenta?**
Consumidores no expertos, que compran vino algunas veces por mes para cenas, asados, reuniones o regalos, y que no manejan vocabulario técnico de cata.

**¿En qué contexto ocurre?**
En dos momentos separados en el tiempo:
1. **Cuando toman el vino** (bar, restaurante, casa de amigos, cena): es cuando se forma la opinión.
2. **Cuando están frente a la góndola** de una vinoteca o supermercado: es cuando necesitan recordar esa opinión.

El problema central es que entre ambos momentos pasan semanas, y la opinión se pierde.

**¿Cómo se resuelve actualmente?**
Con la memoria, sacando fotos sueltas que quedan perdidas en la galería, anotando en apps de notas, o usando apps como Vivino.

**¿Qué dificultades presenta la solución actual?**
- La memoria falla, sobre todo con nombres de vinos parecidos entre sí.
- Las fotos en la galería no tienen contexto (¿me gustó? ¿con qué lo tomé?) ni se pueden buscar.
- Las notas de texto libre no se pueden comparar ni resumir.
- Las apps existentes están orientadas a la opinión de la comunidad y al catálogo global, usan vocabulario de sommelier, piden muchos datos y dependen de conexión, algo que en sótanos de vinotecas o supermercados suele fallar.

**¿Por qué una app móvil podría mejorarlo?**
Porque ambos momentos ocurren lejos de una computadora, con el celular en la mano: la cámara permite registrar la etiqueta en segundos, el almacenamiento local permite consultar el historial sin señal en el punto de venta, y la app acompaña la situación sin obligar a cargar datos complejos.

**Hipótesis a validar durante el desarrollo:**
- H1: el usuario está dispuesto a registrar un vino si le lleva menos de un minuto.
- H2: la consulta en la góndola se resuelve con texto parcial del nombre (no hace falta catálogo).
- H3: un resumen de los gustos propios resulta más útil para este usuario que las reseñas de otras personas.

---

## 3. Usuario principal

**Persona: Lucía, 31 años.** Trabaja en una oficina y comparte cenas y asados con amigos un par de veces por mes. Le gusta el vino pero no sabe de cepas ni regiones. Cuando le toca llevar el vino, se queda varios minutos en la góndola y termina eligiendo por la etiqueta. Más de una vez compró un vino que ya había probado y no le había gustado.

| Aspecto | Descripción |
|---|---|
| Características relevantes | Persona de 25 a 45 años, consumidora ocasional de vino, no experta. Usa el celular con soltura. |
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

**Escenario B — Consulta (caso principal).** Semanas después, en la vinoteca, duda entre dos botellas. Escribe "rutini" y la app le muestra que ya probó el Rutini Malbec dos veces y le gustó con carne. Funciona aunque el local no tenga señal.

**Escenario C — Autoconocimiento.** Después de registrar varios vinos, Lucía abre "Mis gustos" y descubre que casi siempre le gustan los tintos secos con carne, y que los dulces solo le gustaron con el postre. La app le sugiere qué tipo de vino probar la próxima vez.

**Mirada de computación ubicua (Weiser).** La app busca integrarse a la actividad sin interrumpirla:
- La foto reemplaza el tipeo y la IA completa los datos de la etiqueta.
- Las opiniones se cargan con opciones de un toque ("me gustó / no", "dulce / seco") en lugar de texto libre o puntajes técnicos.
- La consulta en la góndola se resuelve en pocos toques y la respuesta se lee de un vistazo.
- La sincronización y el análisis de fotos ocurren en segundo plano, sin que el usuario tenga que esperar ni acordarse.

---

## 5. Propuesta de solución

- **Nombre provisorio:** Sorbo.
- **Descripción breve:** diario personal de vinos. El usuario registra cada vino que prueba con una foto de la etiqueta y una opinión simple (me gustó / no, dulce / seco, comida, compañía, ocasión). Puede buscar en su diario para saber si ya lo probó y, con conexión, usar IA para completar los datos del vino desde la foto y obtener un análisis de sus propios gustos.
- **Usuario principal:** consumidor ocasional, no experto.
- **Propuesta de valor:** en lugar de decir qué opina el mundo de un vino, la app arma un espejo de los gustos propios del usuario, con vocabulario simple.
- **Escenario principal de uso:** consultar en la vinoteca si un vino ya fue probado y qué opinión se tuvo.
- **Beneficio esperado:** menos compras fallidas, posibilidad de repetir los vinos que gustaron y mayor autoconocimiento de las preferencias sin necesitar conocimientos técnicos.

---

## 6. ¿Por qué tiene que ser una app móvil?

| Capacidad del dispositivo | Qué problema resuelve |
|---|---|
| Cámara | La foto de la etiqueta es la forma natural de registrar un vino en el momento; en un sitio web o de escritorio el registro ocurriría tarde o nunca. |
| Almacenamiento local | El diario vive en el teléfono y está disponible al instante, sin depender de la red. |
| Funcionamiento offline | La consulta ocurre en lugares con mala señal (sótanos, supermercados), donde un sitio web no funcionaría. |
| Trabajo en segundo plano | El respaldo y el análisis de fotos pendientes se completan solos al recuperar la conexión, aunque la app esté cerrada. |
| Notificaciones | Avisan cuando la IA terminó de leer una etiqueta cargada sin conexión. |
| Contexto de uso | Los dos momentos clave (tomar y comprar) ocurren en movimiento, fuera de casa, con el celular como único dispositivo disponible. |

---

## 7. Caso de negocio y propuesta de valor

**Problema → Usuario → Solución → Valor generado**

> No recuerdo qué vinos probé ni si me gustaron → consumidor ocasional no experto → diario personal con foto, opinión simple, búsqueda offline y análisis de gustos → elijo mejor, repito lo que me gustó y entiendo mis preferencias sin ser sommelier.

**Comparación con las alternativas actuales:**

| | Memoria / galería / notas | Apps de catálogo y comunidad (ej. Vivino) | **Sorbo** |
|---|---|---|---|
| Registrar en el momento | Rápido pero sin contexto | Muchos datos y vocabulario técnico | Foto + opciones de un toque |
| Encontrar lo que probé | No se puede buscar | Sí, dentro de un catálogo global | Sí, en el diario propio |
| Funciona sin señal | Sí | Limitado | Sí, completo para registro y consulta |
| Qué me muestra | Lo que recuerdo | Lo que opina la comunidad | Lo que **a mí** me gustó y por qué |
| Útil con un solo usuario | Sí | Depende de la comunidad | Sí, desde el primer registro |

**Valor generado:**
- **De experiencia:** compras con más seguridad, menos frustración.
- **Económico para el usuario:** menos dinero gastado en vinos que no le gustan.
- **De accesibilidad:** vocabulario simple, apto para personas sin conocimientos de cata.
- **Posible evolución comercial (fuera de alcance):** modelo freemium (análisis de IA ilimitado como función paga) o alianzas con vinotecas.

---

## 8. Alcance y fuera de alcance

**Priorización de la versión 1 (MoSCoW):**

| Prioridad | Funcionalidad | Conexión |
|---|---|---|
| **Imprescindible** | RF01 — Registrar un vino probado (con foto y opinión) | No requiere |
| **Imprescindible** | RF02 — Consultar si ya probé un vino | No requiere |
| **Imprescindible** | Ver el detalle de un vino, editar y eliminar | No requiere |
| **Importante** | RF03 — Completar los datos del vino con IA desde la foto | Requiere (se encola si no hay) |
| **Importante** | RF04 — Análisis de mis gustos | Estadísticas sin conexión; texto de IA con conexión |
| **Importante** | Cuenta y respaldo en la nube (Supabase) | Requiere (se sincroniza al reconectar) |
| **Deseable** | Filtros del diario (me gustaron, secos, dulces) | No requiere |

Las funcionalidades imprescindibles forman un producto completo por sí solas: si el tiempo no alcanza, la app sigue resolviendo el problema principal sin IA ni nube.

**Fuera de alcance:**
- Catálogo global de vinos, puntajes o reseñas de otros usuarios.
- Funciones sociales (seguir usuarios, compartir, comentarios).
- Precios, compras o integración con tiendas.
- Uso simultáneo en varios dispositivos con edición concurrente.
- Notas de cata técnicas (aromas, taninos, etc.).
- Búsqueda por foto en la góndola (la búsqueda es por texto).
- Versión iOS o web.

**Riesgos y mitigaciones:**

| Riesgo | Mitigación |
|---|---|
| La IA lee mal una etiqueta | Siempre es una sugerencia editable; nunca se guarda sin confirmación y siempre existe la carga manual. |
| Límites del plan gratuito de Groq o caída del servicio | Las funciones de IA son complementarias; ante un error se ofrece reintentar y el resto de la app sigue funcionando. |
| El proyecto gratuito de Supabase se pausa por inactividad | Reactivarlo antes de cada demo; la app funciona igual sin conexión al backend. |
| El alcance no llega a completarse | Priorización MoSCoW: primero se completan RF01 y RF02 de punta a punta. |

---

## 9. Requisitos funcionales

### RF01 — Registrar un vino probado
- **Usuario:** consumidor ocasional.
- **Descripción:** el usuario podrá registrar un vino tomando una foto de la etiqueta (o eligiéndola de la galería) e indicando nombre, si le gustó, si lo sintió dulce o seco, con qué comida lo acompañó, con quién lo tomó, en qué ocasión y una nota opcional. Si el vino ya existe en su diario, se registra una nueva degustación del mismo vino en lugar de duplicarlo.
- **Criterios de aceptación:**
  - *Dado* que el usuario completó el nombre y si le gustó, *cuando* toca "Guardar" con el dispositivo en modo avión, *entonces* el vino aparece primero en el diario en menos de 1 segundo y sigue ahí al cerrar y volver a abrir la app.
  - *Dado* que falta el nombre o la opinión, *cuando* toca "Guardar", *entonces* la app indica qué campo falta y no se pierde nada de lo cargado.
  - *Dado* que el vino "Rutini Malbec 2021" ya está en el diario, *cuando* el usuario lo vuelve a registrar, *entonces* se agrega una segunda degustación a ese vino y no aparece un vino duplicado.

### RF02 — Consultar si ya probé un vino
- **Usuario:** consumidor ocasional, en el punto de venta.
- **Descripción:** el usuario podrá buscar por nombre, bodega o cepa en su diario y ver si ya probó el vino, cuántas veces, si le gustó y con qué lo acompañó.
- **Criterios de aceptación:**
  - *Dado* un diario que contiene "El Esteco Torrontés", *cuando* el usuario escribe "torrontes" o "ESTECO", *entonces* el vino aparece como resultado (la búsqueda ignora mayúsculas y tildes y acepta texto parcial).
  - *Dado* un diario con 500 vinos y el dispositivo sin conexión, *cuando* el usuario escribe en el buscador, *entonces* los resultados se muestran en menos de 1 segundo.
  - *Dado* que no hay coincidencias, *cuando* termina la búsqueda, *entonces* la app informa "Todavía no lo probaste" y ofrece registrarlo con el nombre ya cargado.

### RF03 — Completar los datos del vino con IA a partir de la foto
- **Usuario:** consumidor ocasional con sesión iniciada, al registrar.
- **Descripción:** a partir de la foto de la etiqueta, la app enviará la imagen a un modelo de IA con visión que devolverá una sugerencia de nombre, bodega, cepa y año. El usuario podrá aceptar, editar o descartar la sugerencia.
- **Criterios de aceptación:**
  - *Dado* que hay conexión, *cuando* el usuario saca la foto, *entonces* los campos sugeridos se completan marcados como "sugerido por IA", son editables y nunca se guardan sin que el usuario toque "Guardar".
  - *Dado* que no hay conexión, *cuando* el usuario guarda el vino cargado a mano, *entonces* la foto queda "pendiente de análisis"; al recuperar la conexión se analiza en segundo plano y el usuario recibe un aviso para revisar la sugerencia desde el detalle del vino.
  - *Dado* que la IA no responde o no puede leer la etiqueta, *cuando* termina el intento, *entonces* se muestra un mensaje claro con la opción de reintentar y la carga manual sigue disponible.

### RF04 — Ver el análisis de mis gustos
- **Usuario:** consumidor ocasional.
- **Descripción:** la app mostrará estadísticas simples de los gustos del usuario calculadas a partir de su diario (por ejemplo, "vinos secos: te gustaron 8 de 9") y, con conexión, un resumen en lenguaje simple generado con IA junto con sugerencias del tipo de vino a probar.
- **Criterios de aceptación:**
  - *Dado* que el diario tiene menos de 5 degustaciones, *cuando* el usuario abre "Mis gustos", *entonces* ve cuántas le faltan para habilitar el análisis.
  - *Dado* que tiene 5 o más degustaciones, *cuando* abre "Mis gustos" sin conexión, *entonces* las estadísticas se muestran actualizadas (se calculan en el teléfono) y el resumen de IA muestra el último guardado con su fecha.
  - *Dado* que hay conexión y sesión iniciada, *cuando* toca "Actualizar", *entonces* se genera un resumen nuevo, se guarda con su fecha y se muestra en lugar del anterior.

---

## 10. Requisitos no funcionales

| ID | Tipo | Requisito verificable |
|---|---|---|
| RNF01 | Comportamiento offline | Registrar, consultar, editar y eliminar vinos, y ver las estadísticas de gustos, funcionan con el dispositivo en modo avión. |
| RNF02 | Usabilidad | Un registro se completa con un máximo de 5 toques además de la foto. La búsqueda es accesible desde la pantalla principal con un toque. |
| RNF03 | Accesibilidad | Elementos táctiles de al menos 48dp, contraste mínimo WCAG AA (4.5:1 en texto), descripciones de contenido para TalkBack en todos los íconos, y la interfaz se mantiene usable con la fuente del sistema al 130%. |
| RNF04 | Rendimiento | El diario se muestra en menos de 1 segundo al abrir la app con 500 vinos guardados. |
| RNF05 | Seguridad | Ninguna API key de servicios de IA se incluye en la aplicación: las llamadas pasan por una Supabase Edge Function. Los datos remotos están protegidos con Row Level Security para que cada usuario acceda solo a sus propios registros. |
| RNF06 | Privacidad | Las fotos y el diario solo se envían a la IA cuando el usuario usa una función de IA, que puede desactivar desde Ajustes. La app lo informa en el primer uso. |
| RNF07 | Confiabilidad | Los registros creados sin conexión se respaldan automáticamente al recuperar la conexión, sin intervención del usuario y sin pérdida de datos. |
| RNF08 | Recuperación ante errores | Ante un fallo de red o de la IA, la app muestra un mensaje en lenguaje simple con una acción de reintento y no se pierden los datos ingresados. |
| RNF09 | Permisos | Si el usuario rechaza el permiso de cámara, puede elegir una foto de la galería o registrar sin foto. |
| RNF10 | Consumo de batería y datos | La sincronización se ejecuta solo cuando hay red disponible (sin consultas periódicas) y las fotos se comprimen a un máximo de 500 KB antes de guardarse o subirse. |
| RNF11 | Compatibilidad | Android 8.0 (API 26) o superior, en pantallas de 320dp a 412dp de ancho. |

---

## 11. Diseño en Figma

**Link:** https://www.figma.com/design/NM0utJz4cc9JBflTfu1ep9/Sorbo-App

Las pantallas se prototiparon primero en HTML siguiendo Material 3 y luego se importaron a Figma. Son 29 frames: todas las pantallas con sus estados, variantes de 320dp y 412dp de ancho y tema oscuro.

**Lineamientos de diseño:**
- **Material Design 3**, el sistema de diseño nativo de Android y el que implementa Jetpack Compose (`androidx.compose.material3`), para que el diseño se traslade directo a componentes reales.
- **Diseño adaptable:** base de 360dp de ancho, validado de 320dp a 412dp. Las listas y formularios ocupan el ancho disponible, sin medidas fijas, y respetan el tamaño de fuente del sistema.
- **Uso con una mano:** las acciones principales (registrar, guardar, navegar) están en el tercio inferior de la pantalla, en la zona que alcanza el pulgar.
- **Color con significado:** bordó como color de marca. Ninguna información depende solo del color: "me gustó / no me gustó" siempre se muestra con ícono y texto. Todo lo generado por IA se identifica con un mismo color y el ícono ✦.

**Pantallas principales:**
1. **Bienvenida:** una sola pantalla que explica la propuesta; no requiere cuenta.
2. **Diario (inicio):** buscador "¿Ya lo probaste?" fijo arriba, filtros, lista de vinos con foto y veredicto, botón flotante "Registrar".
3. **Resultado de búsqueda:** "Ya lo probaste" (veredicto, veces, con qué le gustó) o "Todavía no lo probaste" (con acceso a registrarlo).
4. **Registrar vino:** foto, datos sugeridos por IA, opinión con opciones de un toque, guardar fijo abajo.
5. **Detalle del vino:** foto, datos, degustaciones anteriores, "Lo volví a probar", editar y eliminar.
6. **Mis gustos:** estilo del usuario, lo que más disfruta, lo que no lo convence y sugerencias.
7. **Ajustes:** cuenta, estado del respaldo, restaurar y control de la IA.
8. **Iniciar sesión:** email y contraseña, con opción de seguir sin cuenta.

**Navegación:** barra inferior con tres destinos (Diario, Mis gustos y Ajustes) y botón flotante para registrar desde el Diario.

**Estados representados:**

| Pantalla | Estados |
|---|---|
| Diario | Vacío (primer uso), cargando, con contenido, sin conexión, error, eliminado con "Deshacer". |
| Búsqueda | Ya lo probaste, todavía no lo probaste. |
| Registrar | Analizando etiqueta, sugerencia lista, sin conexión, error de IA y validación, permiso de cámara rechazado. |
| Detalle | Guardado con confirmación, sugerencia pendiente de revisar, confirmación de eliminación. |
| Mis gustos | Menos de 5 degustaciones, generando, con análisis, sin conexión (resumen desactualizado), requiere cuenta, error. |
| Global | Indicador de sin conexión, de respaldo al día y de registros pendientes. |

**Decisiones de UX (heurísticas de Nielsen):**

| Heurística | Cómo se aplica |
|---|---|
| Visibilidad del estado del sistema | Indicador de sin conexión, de registros pendientes de respaldo y de "leyendo la etiqueta"; fecha del último análisis de gustos. |
| Relación con el mundo real | Vocabulario cotidiano ("me gustó", "dulce", "con amigos") en lugar de términos de cata. |
| Control y libertad del usuario | La sugerencia de IA siempre se puede editar o descartar; eliminar ofrece "Deshacer". |
| Consistencia y estándares | Componentes estándar de Material 3 (barra de navegación inferior, botón flotante, chips, snackbars, diálogos). |
| Prevención de errores | Solo dos campos obligatorios; opciones con chips en lugar de texto libre; confirmación antes de eliminar. |
| Reconocer antes que recordar | La foto identifica cada vino en la lista; las comidas y ocasiones son opciones visibles, no hay que escribirlas. |
| Flexibilidad y eficiencia | Registrar abre directamente la cámara; la IA lee la etiqueta mientras el usuario ya responde si le gustó. |
| Diseño minimalista | La respuesta a "¿ya lo probé?" se muestra primero y en grande; el detalle queda un toque más abajo. |
| Ayuda ante errores | Mensajes en lenguaje simple con una acción concreta ("Reintentar", "Elegir de la galería", "Continuar sin foto"). |

---

## 12. Flujo de pantallas

**Caso de uso principal: consultar en la vinoteca si ya probé un vino (y registrarlo si no).**

```
[Inicio / Diario]
      |
      v
 Escribe el nombre en el buscador
      |
      v
 ¿Hay coincidencias en el diario local?
      |-- Sí --> [Resultado: "Ya lo probaste"] --> [Detalle del vino] --> FIN (decide la compra)
      |
      '-- No --> [Resultado: "Todavía no lo probaste"]
                       |
                       |-- No registra --> FIN
                       |
                       '-- Registrar --> [Registrar vino]
                                              |
                                              v
                                   ¿Permiso de cámara concedido?
                                     |-- Sí --> Toma foto de la etiqueta
                                     '-- No --> Elige de la galería o sigue sin foto
                                              |
                                              v
                                        ¿Hay conexión?
                                         |-- Sí --> IA sugiere datos --> Usuario confirma o edita
                                         '-- No --> Carga manual + foto "pendiente de análisis"
                                              |
                                              v
                                        Guardar en Room --> [Detalle del vino] --> FIN
```

*Diagrama completo en la página "Flujo de pantallas" del archivo draw.io del repositorio.*

**Operaciones críticas:**
- **Guardar:** debe persistir siempre, con o sin conexión.
- **Aplicar una sugerencia de IA:** nunca es automático; requiere confirmación.
- **Eliminar:** pide confirmación y ofrece deshacer.

---

## 13. Arquitectura propuesta

Arquitectura **MVVM con principios de Clean Architecture**, en tres capas. **Room es la fuente única de verdad:** la UI siempre lee de la base local y la comunicación con la nube ocurre en segundo plano. La capa de dominio no depende de Android ni de Supabase, por lo que sus reglas se pueden probar con tests unitarios simples.

| Capa | Responsabilidades | Componentes |
|---|---|---|
| Presentación | Mostrar el estado y capturar las acciones del usuario | Pantallas en Jetpack Compose, un ViewModel por pantalla que expone `StateFlow<UiState>`, Navigation Compose |
| Dominio | Modelos y reglas del negocio | Modelos (`Usuario`, `Vino`, `Degustacion`, `SugerenciaIA`, `AnalisisGustos`), casos de uso e interfaces de repositorio |
| Datos | Obtener y guardar datos, sincronizar | Implementaciones de repositorio, Room (DAOs y entidades), cliente de Supabase, `SyncWorker` de WorkManager, mappers entre entidades, modelos y DTOs |

**Casos de uso del dominio:**

| Caso de uso | Responsabilidad | Conexión |
|---|---|---|
| `RegistrarDegustacion` | Valida nombre y opinión; crea el vino si no existe o agrega la degustación al existente | No |
| `BuscarEnDiario` | Normaliza el texto (minúsculas, sin tildes) y busca por nombre, bodega o cepa | No |
| `ObtenerDetalleVino` | Devuelve el vino con sus degustaciones y su sugerencia pendiente | No |
| `EliminarVino` | Borrado lógico del vino y sus degustaciones, con posibilidad de deshacer | No |
| `CalcularEstadisticasGustos` | Calcula, por ejemplo, "secos: 8 de 9 te gustaron" a partir del diario | No |
| `SugerirDatosDesdeFoto` | Pide a la IA los datos de la etiqueta y los devuelve como sugerencia | Sí |
| `AplicarSugerencia` | Copia al vino los datos que el usuario aceptó | No |
| `GenerarAnalisisGustos` | Envía las estadísticas a la IA y guarda el resumen con su fecha | Sí |
| `SincronizarDiario` | Sube los registros pendientes y procesa las fotos pendientes (lo ejecuta el `SyncWorker`) | Sí |

**Estructura del proyecto:**

```
com.sorbo
├── ui/            pantallas Compose y ViewModels (diario, busqueda, registro, detalle, gustos, ajustes), tema y navegación
├── domain/        model/, usecase/, repository/ (interfaces)
├── data/          local/ (Room: entidades y DAOs), remote/ (Supabase: DTOs), repository/ (implementaciones), sync/ (SyncWorker), mapper/
└── AppContainer   crea las dependencias y se las pasa a los ViewModels
```

### Flujo de datos del caso principal: registrar un vino

**A. Guardado local (funciona con o sin conexión)**
1. La UI envía el evento `onGuardar(formulario)` al `RegistroViewModel`.
2. El ViewModel invoca el caso de uso `RegistrarDegustacion`, que valida las reglas de negocio.
3. El caso de uso llama a `VinoRepository.guardar(vino, degustacion)`.
4. El repositorio inserta el vino y la degustación en Room con `syncState = PENDIENTE`.
5. El repositorio encola un `SyncWorker` en WorkManager con la restricción "requiere red".
6. Room emite el diario actualizado mediante `Flow`; el ViewModel actualiza el `UiState` y la UI muestra "Vino guardado en tu diario".

**B. Al recuperar la conexión (en segundo plano, aunque la app esté cerrada)**
7. El `SyncWorker` lee los registros pendientes de Room.
8. Los sube a Supabase (Postgrest para los datos, Storage para la foto).
9. Invoca la Edge Function `analizar-etiqueta`, que llama a Groq con la API key guardada en el servidor y devuelve un JSON con nombre, bodega, cepa y año.
10. El Worker guarda la `SugerenciaIA` en Room y marca el registro como `SINCRONIZADO`.
11. Room emite el cambio y la UI muestra "La IA leyó la etiqueta" para que el usuario la revise.

Si no hay conexión, la fase B espera; si falla, WorkManager reintenta con backoff exponencial. Cuando sí hay conexión al registrar, el ViewModel invoca `SugerirDatosDesdeFoto` desde el formulario y los pasos 8 y 9 ocurren antes de guardar.

### Topología de infraestructura

```
+-----------------------------+
|  Dispositivo Android        |
|  - App (Kotlin + Compose)   |
|  - Room (SQLite)            |
|  - Fotos (almacenamiento    |
|    interno) y DataStore     |
|  - WorkManager              |
+--------------+--------------+
               | HTTPS (supabase-kt)
               v
+-----------------------------+
|  Supabase                   |
|  - Auth (email y clave)     |
|  - Postgres + RLS           |
|  - Storage (fotos)          |
|  - Edge Functions           |
|      analizar-etiqueta      |
|      analizar-gustos        |
+--------------+--------------+
               | HTTPS (API key solo en el servidor)
               v
+-----------------------------+
|  Groq (LLM con visión)      |
+-----------------------------+
```

*Diagramas completos en las páginas "Arquitectura en capas", "Flujo de datos - Registrar vino" y "Topología de infraestructura" del archivo draw.io del repositorio.*

### Estrategia de pruebas (testabilidad)
- **Tests unitarios del dominio:** casos de uso como `RegistrarDegustacion`, `BuscarEnDiario` y `CalcularEstadisticasGustos`, usando repositorios falsos en memoria.
- **Tests de ViewModels:** verificar que cada acción produce el `UiState` esperado (cargando, contenido, vacío, error).
- **Tests de Room:** DAOs probados con una base en memoria (búsqueda sin tildes, relación vino–degustaciones).
- **Pruebas manuales de conectividad:** cada requisito se valida en modo avión, perdiendo la conexión a mitad de una operación y recuperándola.

---

## 14. Persistencia y estrategia Offline First

### Entidades principales

| Entidad | Atributos principales | Dónde se guarda |
|---|---|---|
| **Usuario** | `id` (el mismo de Supabase Auth), `email`, `nombre`, `creadoEn` | Supabase (`auth.users` + tabla `perfiles`); en el teléfono, la sesión activa |
| **Vino** | `id` (UUID), `usuarioId`, `nombre`, `nombreNormalizado`, `bodega`, `cepa`, `anio`, `fotoPath`, `fotoUrl`, `estadoAnalisis`, `creadoEn`, `actualizadoEn`, `syncState`, `eliminado` | Room y Supabase |
| **Degustacion** | `id` (UUID), `vinoId`, `fecha`, `meGusto`, `dulzor`, `comida`, `compania`, `ocasion`, `nota`, `actualizadoEn`, `syncState`, `eliminado` | Room y Supabase |
| **SugerenciaIA** | `id`, `vinoId`, `nombre`, `bodega`, `cepa`, `anio`, `estado` (pendiente de revisar / aceptada / descartada), `creadaEn` | Solo Room |
| **AnalisisGustos** | `id`, `usuarioId`, `resumen`, `sugerencias`, `generadoEn`, `cantidadDegustaciones` | Solo Room (caché del último análisis) |

**Relaciones:**
- Un **Usuario** tiene muchos **Vinos** (1:N): cada persona tiene su propio diario.
- Un **Vino** tiene muchas **Degustaciones** (1:N): el mismo vino puede probarse varias veces, con opiniones distintas.
- Un **Vino** puede tener una **SugerenciaIA** pendiente de revisar (1:0..1).
- Un **Usuario** tiene un **AnalisisGustos** vigente (1:0..1).

**Decisiones de modelado:**
- **El vino pertenece al usuario y no a un catálogo global**, coherente con el fuera de alcance: no hay que mantener una base de vinos del mundo.
- **`nombreNormalizado`** (minúsculas y sin tildes) permite cumplir el criterio de búsqueda de RF02 con una consulta simple e indexada en Room.
- **Comida, compañía, ocasión y dulzor son listas cerradas** (enumeraciones con una opción "Otra"): hacen posible calcular estadísticas confiables en RF04, algo que el texto libre no permitiría.
- **La sugerencia de IA es una entidad separada** del vino: los datos de la IA nunca pisan los del usuario hasta que este los acepta.
- **`usuarioId` puede estar vacío** en los vinos cargados antes de crear una cuenta; al iniciar sesión se asignan al usuario y se respaldan.
- **`syncState`, `actualizadoEn` y `eliminado`** son los campos que permiten sincronizar: qué falta subir, qué versión es más nueva y qué se borró.

*Diagrama completo en la página "Modelo de datos" del archivo draw.io del repositorio.*

**Origen de la información:**
- **Generada por el usuario:** vinos, degustaciones y fotos.
- **Proveniente de APIs:** sugerencias de datos del vino y resumen de gustos (IA vía Edge Functions).
- **Almacenada localmente:** vinos, degustaciones, sugerencias y último análisis en Room; fotos en el almacenamiento interno con su ruta en Room; preferencias (bienvenida vista, aviso de privacidad aceptado, IA activada) en DataStore.

### Comportamiento según conectividad

| Situación | Comportamiento |
|---|---|
| Con conexión | Todas las funciones disponibles. Los cambios se guardan primero en Room y se respaldan en Supabase en segundo plano. |
| Pierde conexión | Registro, consulta, edición, eliminación y estadísticas siguen funcionando. Las funciones de IA indican "Requiere conexión". Los registros nuevos quedan con `syncState = PENDIENTE` y las fotos con `estadoAnalisis = PENDIENTE`. |
| Recupera conexión | WorkManager sube los registros pendientes, analiza las fotos pendientes y avisa al usuario para que revise las sugerencias. |
| Información local desactualizada | Las estadísticas siempre están al día porque se calculan en el teléfono. El resumen de IA muestra su fecha y cuántas degustaciones nuevas hay desde entonces, con la opción de actualizarlo. |
| Sin información local | Estado vacío con acceso a "Registrar mi primer vino". Si el usuario inicia sesión y tenía un respaldo, se ofrece restaurarlo desde la nube. |

### Estrategia
- **Qué se guarda localmente:** todo el diario (vinos, degustaciones, fotos), las sugerencias pendientes y el último análisis de gustos.
- **Fuente remota:** Supabase (Postgres para los datos, Storage para las fotos, Edge Functions para la IA).
- **Cuándo se actualizan los datos:** al guardar un cambio se intenta respaldar de inmediato; si no hay red, WorkManager lo hace al recuperarla. El resumen de IA se actualiza a pedido del usuario.
- **Qué ve el usuario sin conexión:** todo su diario y sus estadísticas, con un indicador discreto de "sin conexión" y de registros pendientes.
- **Errores y conflictos:** al tratarse de un solo dispositivo por usuario, se aplica "última escritura gana" según `actualizadoEn`. Los borrados son lógicos (`eliminado = true`) para que también se propaguen. Si una sincronización falla, se reintenta con backoff exponencial y el registro sigue disponible en el teléfono.

---

## 15. Tecnologías previstas y justificación

| Tecnología | Justificación |
|---|---|
| Kotlin + Android | Lenguaje y plataforma requeridos; acceso nativo a cámara, almacenamiento y tareas en segundo plano. |
| Jetpack Compose + Material 3 | UI declarativa que representa cada estado de pantalla a partir de un `UiState`; Material 3 es el sistema de diseño usado en Figma. |
| Navigation Compose | Navegación entre pantallas y paso de parámetros (por ejemplo, el id del vino al detalle). |
| ViewModel + StateFlow | Conservar el estado de pantalla ante rotaciones y exponerlo de forma reactiva a la UI. |
| Coroutines + Flow | Operaciones asíncronas (base de datos, red) sin bloquear la UI; Room expone `Flow` que actualizan la UI automáticamente. |
| Room | Persistencia estructurada con relaciones (usuario–vinos–degustaciones) y búsqueda indexada; es la fuente de verdad que permite el funcionamiento offline. |
| DataStore | Preferencias simples (bienvenida vista, aviso de privacidad, IA activada). |
| ActivityResult `TakePicture` + Photo Picker | Capturar o elegir la foto de la etiqueta con la cámara del sistema; es más simple que implementar una cámara propia con CameraX y cubre la necesidad. |
| Coil | Carga eficiente de las fotos de etiquetas en Compose. |
| WorkManager | Sincronización y análisis de fotos pendientes al volver la conexión, aunque la app esté cerrada, respetando la batería. |
| Supabase (supabase-kt: Auth, Postgrest, Storage, Functions) | Backend en un solo servicio: autenticación con email y contraseña, respaldo de datos, almacenamiento de fotos y funciones de servidor. El login es opcional: la app se usa completa sin cuenta y la cuenta habilita el respaldo y la IA. |
| Supabase Edge Functions | Intermediario hacia la IA para no exponer la API key dentro de la aplicación. |
| Groq (LLM con visión) | Lee etiquetas con tipografías artísticas y devuelve datos estructurados (JSON) mejor que un OCR tradicional; también redacta el resumen de gustos. Se eligió por su plan gratuito suficiente para el proyecto, baja latencia y API compatible con OpenAI, lo que permite cambiar de proveedor modificando solo la Edge Function. |
| ConnectivityManager (`NetworkCallback`) | Detectar el estado de la conexión para mostrar el indicador y habilitar o deshabilitar las funciones de IA. |

---

## 16. Repositorio y forma de trabajo

- **URL del repositorio:** https://github.com/alejogarciabosio/sorbo-app
- **Integrantes:** Alejo Garcia Bosio, Iñaki Courtinade, Nino Bizzotto.
- **Estrategia de ramas:** `main` (versión estable, solo recibe cambios desde `develop`), `develop` (integración) y una rama por funcionalidad creada desde `develop`: `feature/rf01-registrar-vino`, `feature/rf02-busqueda`, etc.
- **Commits:** Conventional Commits (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`), pequeños y frecuentes, en español.
- **Pull requests:** toda funcionalidad entra a `develop` mediante un pull request revisado por al menos otro integrante, que verifica que compile, que pasen los tests y que cumpla los criterios de aceptación del requisito.
- **Organización:** tareas en GitHub Projects, una por requisito o pantalla, asignadas a un responsable.

---

## 17. Integrantes, roles y responsabilidades

| Integrante | Rol principal | Responsabilidades | Áreas en las que participa |
|---|---|---|---|
| Alejo Garcia Bosio | Arquitectura y documentación | Definición de capas y estructura del proyecto, configuración de Supabase (Auth, tablas, Row Level Security, Storage), Edge Functions de IA, README y documentación de decisiones | Dominio, backend, documentación |
| Iñaki Courtinade | Integración, persistencia y sensores | Base de datos local con Room, sincronización con Supabase mediante WorkManager, manejo de conectividad, captura de fotos con la cámara | Datos, offline first, capacidades del dispositivo |
| Nino Bizzotto | UI, UX y testing | Diseño en Figma, pantallas en Jetpack Compose, estados de pantalla, navegación, accesibilidad y tests | Presentación, calidad |

**Cambios en los miembros del equipo:** ninguno.

*Los roles no son exclusivos: todos los integrantes participan en el desarrollo y comprenden el funcionamiento general de la aplicación.*
