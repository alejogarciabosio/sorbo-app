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
- H4: con 2 a 6 registros por mes, el usuario sostiene el hábito porque la consulta en la góndola (1 a 4 veces por mes) le devuelve valor cada vez que su diario crece.
- H5: el costo de IA y de almacenamiento por usuario es lo bastante bajo como para que la app funcione gratis; no se asume que el usuario pague (ver "Validación de supuestos, alternativas y costos").

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
- La foto reemplaza el tipeo y la app lee la etiqueta en el teléfono para completar los datos.
- Las opiniones se cargan con opciones de un toque ("me gustó / no", "dulce / seco") en lugar de texto libre o puntajes técnicos.
- La consulta en la góndola se resuelve en pocos toques y la respuesta se lee de un vistazo.
- El respaldo ocurre en segundo plano, sin que el usuario tenga que esperar ni acordarse.

---

## 5. Propuesta de solución

- **Nombre provisorio:** Sorbo.
- **Descripción breve:** diario personal de vinos. El usuario registra cada vino que prueba con una foto de la etiqueta y una opinión simple (me gustó / no, dulce / seco, comida, compañía, ocasión). Puede buscar en su diario para saber si ya lo probó; la app lee la etiqueta en el teléfono para completar los datos del vino (sin conexión) y, con conexión, usa IA para resumir sus propios gustos.
- **Usuario principal:** consumidor ocasional, no experto.
- **Propuesta de valor:** en lugar de decir qué opina el mundo de un vino, la app arma un espejo de los gustos propios del usuario, con vocabulario simple.
- **Escenario principal de uso:** consultar en la vinoteca si un vino ya fue probado y qué opinión se tuvo.
- **Beneficio esperado:** menos compras fallidas, posibilidad de repetir los vinos que gustaron y mayor autoconocimiento de las preferencias sin necesitar conocimientos técnicos.

---

## 6. ¿Por qué tiene que ser una app móvil?

| Capacidad del dispositivo | Qué problema resuelve |
|---|---|
| Cámara | La foto de la etiqueta es la forma natural de registrar un vino en el momento; en un sitio web o de escritorio el registro ocurriría tarde o nunca. |
| Procesamiento en el dispositivo (ML Kit) | La app lee el texto de la etiqueta en el teléfono, sin conexión y sin enviar la foto a ningún servidor. |
| Almacenamiento local | El diario vive en el teléfono y está disponible al instante, sin depender de la red. |
| Funcionamiento offline | La consulta ocurre en lugares con mala señal (sótanos, supermercados), donde un sitio web no funcionaría. |
| Trabajo en segundo plano | El respaldo de los registros pendientes se completa solo al recuperar la conexión, aunque la app esté cerrada. |
| Integración con otras apps | La recuperación de contraseña abre la app de email del teléfono, y el enlace del email vuelve a abrir Sorbo (deep link). |
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
| Funciona sin señal | Sí | No (Vivino no funciona sin conexión) | Sí, completo para registro y consulta |
| Qué me muestra | Lo que recuerdo | Lo que opina la comunidad | Lo que **a mí** me gustó y por qué |
| Útil con un solo usuario | Sí | Depende de la comunidad | Sí, desde el primer registro |

**Valor generado:**
- **De experiencia:** compras con más seguridad, menos frustración.
- **Económico para el usuario:** menos dinero gastado en vinos que no le gustan.
- **De accesibilidad:** vocabulario simple, apto para personas sin conocimientos de cata.
- **Posible evolución comercial (fuera de alcance):** modelo freemium (análisis de IA ilimitado como función paga) o alianzas con vinotecas.

### Validación de supuestos, alternativas y costos

Datos consultados el 3 de noviembre de 2026. Los precios y límites de los proveedores cambian: se vuelven a verificar antes de la entrega final.

**1. ¿El usuario sostiene el registro con una frecuencia tan baja?** Es el supuesto más débil del proyecto y no está validado todavía. Lo que sí sostiene la hipótesis:
- La frecuencia que importa no es solo la del registro (2 a 6 por mes) sino también la de la consulta (1 a 4 por mes, en cada compra): cada consulta es una razón para volver a abrir la app.
- El esfuerzo de registrar es bajo (foto más un máximo de 5 toques) y el diario se vuelve más útil con cada vino cargado.
- El riesgo es el arranque en frío: con pocos vinos cargados la app aporta poco. Por eso lo imprescindible tiene que funcionar de punta a punta desde el primer registro, sin cuenta ni IA.

Plan de validación para la etapa 2 (todavía no hay resultados): que entre 5 y 8 personas que toman vino de forma ocasional usen la app durante 3 o 4 semanas y medir registros por persona y por semana, cuántas siguen registrando en la última semana y cuántas veces consultaron antes de comprar. Cierre con una encuesta de tres preguntas: si seguirían usándola, qué les faltó y si pagarían. Criterio de éxito propuesto: al menos la mitad sigue registrando en la última semana y consultó al menos una vez.

**2. Diferenciación frente a alternativas reales.**

| Alternativa | Qué hace bien | En qué se diferencia Sorbo | Dónde Sorbo es más débil |
|---|---|---|---|
| Vivino | Reconoce la etiqueta contra un catálogo de millones de vinos, muestra puntajes de la comunidad, guarda un diario personal de lo que probaste y vende vino. | Funciona con vinos que no están en ningún catálogo, sin depender de la red (Vivino no funciona sin conexión, según la prueba del equipo en modo avión del 3 de noviembre de 2026), con vocabulario cotidiano y centrada en los gustos propios en lugar del puntaje de otros. | Vivino también guarda un historial personal: la diferencia no es "tener un diario" sino que funcione sin conexión y sea un espejo de los gustos propios. Además ya está instalada en muchos teléfonos y reconoce la etiqueta con su catálogo. |
| CellarTracker | Gestión de una bodega propia (inventario) y notas de la comunidad; pensada para coleccionistas. | No pide inventario ni vocabulario de cata. | Es mucho más completa para quien tiene una bodega. |
| Delectable | App social y editorial para compartir catas. | No tiene funciones sociales ni exige una comunidad. | Su comunidad es más grande (las reseñas que encontramos son de 2015 y 2016: se confirman con la app actual). |
| Fotos, notas o planilla | Sin fricción y ya instaladas. | Búsqueda por nombre, bodega o cepa, veredicto de un vistazo y estadísticas. | Una app más que instalar y abrir. |

Conclusión: Sorbo no compite con el catálogo ni con la comunidad de Vivino; apunta a la persona que quiere recordar lo suyo, también sin señal. Que esa diferencia alcance se confirma con las pruebas con usuarios.

**3. Alternativas de IA para leer la etiqueta.** Evaluamos tres enfoques de visión por computadora:

| Opción | Qué hace | Dónde corre | Sin conexión | Costo | Esfuerzo | Decisión |
|---|---|---|---|---|---|---|
| ML Kit (Text Recognition v2) | Lee el texto de la foto; no sabe qué es cada dato | En el teléfono | Sí, con el modelo incluido en la app (unos 4 MB) | Sin costo por uso | Bajo: una dependencia más unas reglas simples para ubicar año, cepa, bodega y nombre | **Elegida** |
| Roboflow (plataforma de visión por computadora) | Entrena y despliega modelos que detectan o clasifican objetos; su repositorio público (Universe) tiene datasets y modelos de etiquetas de vino, de 33 a 5.500 imágenes y varios con licencia CC BY 4.0 | En la nube de Roboflow; en dispositivo está documentado para Jetson, Raspberry Pi y servidores con GPU, no para Android | No, con la API en la nube | Plan gratuito de 10 créditos por mes (unas 80.000 inferencias) | Alto: elegir y probar un modelo, exportarlo (el plan gratuito permite descargar los pesos) e integrarlo a mano en Android | No elegida |
| Modelo de IA con visión en la nube (Groq, `qwen/qwen3.8-27b`) | Lee la etiqueta y devuelve los datos ya estructurados | En la nube | No | Gratis dentro del plan gratuito; unos USD 0,0023 por etiqueta fuera de él | Medio: Edge Function, cuenta y cola de análisis | **Plan B** |

Por qué no Roboflow: sus modelos de etiquetas de vino detectan dónde está la etiqueta o la botella, pero no leen el texto ni dicen de qué vino se trata; para identificar el vino haría falta un catálogo, que está fuera de alcance. Subir fotos de usuarios a su nube agregaría además un servicio externo que necesita conexión y cuenta, y sus espacios nuevos permiten por defecto el licenciamiento de datos y el entrenamiento sobre proyectos privados (se puede desactivar). Podría sumarse más adelante para recortar la etiqueta antes de leerla si la luz es un problema.

Por qué ML Kit: resuelve el problema donde ocurre (en el bar o en la vinoteca, sin señal), no cuesta nada a ninguna escala, la foto no sale del teléfono y es lo más simple de explicar y de defender. Su límite es la precisión, porque devuelve texto y no datos; por eso las sugerencias son siempre editables. Criterio para decidir al comienzo de la etapa 2, con 10 etiquetas reales: si el año y la cepa salen bien en al menos 7 y el nombre sugerido es útil en al menos 5, se queda; si no, se activa el plan B.

**4. Estimación de costos hoy (por usuario y por año).**

*Almacenamiento (Supabase, plan gratuito: 500 MB de base de datos y 1 GB de archivos).* Una foto pesa como máximo 500 KB y se guarda una por vino nuevo. Los registros de texto ocupan unos cientos de bytes y no son el límite.

| Uso | Fotos por año | Fotos por usuario y año | Usuarios que entran en 1 GB |
|---|---|---|---|
| Liviano (2 vinos nuevos por mes) | 24 | 12 MB | unos 80 |
| Típico (4 por mes) | 48 | 24 MB | unos 40 |
| Intenso (6 por mes) | 72 | 36 MB | unos 27 |

Para el TPO sobra. Si la app creciera, el límite que aparece primero es el de fotos; la salida es el plan Pro de Supabase (desde USD 25 por mes) o comprimir más las imágenes.

*IA para leer etiquetas (ML Kit).* Corre en el teléfono: no hay servidor, cuota ni costo por pedido, con 3 usuarios o con 100.000.

*IA para el resumen de gustos (Groq, modelo `qwen/qwen3.8-27b`: USD 0,80 por millón de tokens de entrada y USD 4,00 por millón de salida).* Supuestos nuestros, a medir al implementar: un resumen usa unos 700 tokens de entrada (las estadísticas) y 250 de salida, sin modo de razonamiento.

| Operación | Costo aproximado |
|---|---|
| Un resumen de gustos | USD 0,0016 |
| Un usuario por año (un resumen por mes) | USD 0,02 |

El costo de IA por usuario son centavos de dólar por año. Hay que cuidar que el resumen use el modo sin razonamiento, porque el razonamiento multiplicaría los tokens de salida y el costo.

*Límites de los planes gratuitos.* Groq, para ese modelo: 30 pedidos por minuto, 1.000 por día, 8.000 tokens por minuto y 200.000 por día, de sobra para resúmenes ocasionales. Supabase gratuito: 500 MB de base de datos, 1 GB de archivos, 50.000 usuarios activos por mes, 500.000 invocaciones de Edge Functions por mes, y el proyecto se pausa tras 1 semana sin actividad.

**5. Qué pasaría si la app crece.** Hoy el proyecto no paga nada: usa los planes gratuitos de Supabase y de Groq, y la lectura de etiquetas corre en el teléfono. Si crecieran los usuarios habría que pagar tanto la base de datos y el almacenamiento (Supabase) como la IA (Groq), pero casi todo el costo sería Supabase. Con un año de uso típico (24 MB de fotos por usuario):

| Usuarios activos | Fotos guardadas | Supabase | IA (un resumen por mes) | Total aproximado por mes |
|---|---|---|---|---|
| Hasta unos 40 (el TPO y una prueba con conocidos) | Menos de 1 GB | Plan gratuito: USD 0 | USD 0 (plan gratuito de Groq) | USD 0 |
| 100 | 2,4 GB | Plan Pro: USD 25 | Unos USD 0,2 | Unos USD 25 |
| 1.000 | 24 GB | Plan Pro: USD 25 (incluye 100 GB de archivos) | Unos USD 1,6 | Unos USD 27 |
| 10.000 | 240 GB | Plan Pro: USD 25 más 140 GB extra a USD 0,0213 por GB (unos USD 3) | Unos USD 16 | Unos USD 44, más cómputo adicional si hiciera falta |

El plan Pro (desde USD 25 por mes) incluye 100.000 usuarios activos, 8 GB de base de datos y 100 GB de archivos, y deja de pausar el proyecto por inactividad. Con 1.000 usuarios el costo ronda USD 0,32 por usuario por año. Son estimaciones con los precios vigentes al 3 de noviembre de 2026, sin impuestos, y el cómputo de Supabase se cobra aparte si el uso lo exigiera.

**6. ¿Pagaría el usuario?** No lo asumimos. En esta versión la app es gratuita y puede sostenerse así; si creciera, el costo por usuario es de centavos al año (ver arriba). El modelo freemium mencionado más arriba sigue fuera de alcance y sin validar: la última pregunta de la encuesta con usuarios es la primera evidencia que vamos a tener.

*Fuentes:* [ML Kit: procesamiento en el dispositivo](https://developers.google.com/ml-kit), [ML Kit: reconocimiento de texto en Android](https://developers.google.com/ml-kit/vision/text-recognition/v2/android), [Roboflow: precios y despliegue](https://roboflow.com/pricing), [Roboflow Universe: wine-label](https://universe.roboflow.com/wine-label), [Roboflow Universe: wine-label-detection](https://universe.roboflow.com/space-yo3ke/wine-label-detection-ru03j), [Groq: visión](https://console.groq.com/docs/vision), [Groq: modelo Qwen 3.8 27B](https://console.groq.com/docs/model/qwen/qwen3.8-27b), [Groq: límites](https://console.groq.com/docs/rate-limits), [Groq: modelos dados de baja](https://console.groq.com/docs/deprecations), [Supabase: precios](https://supabase.com/pricing), [Vivino en la App Store](https://apps.apple.com/app/414461255), [Jancis Robinson: apps para escanear etiquetas](https://jancisrobinson.com/articles/best-wine-labelscanning-apps).

---

## 8. Alcance y fuera de alcance

**Priorización de la versión 1 (MoSCoW):**

| Prioridad | Funcionalidad | Conexión |
|---|---|---|
| **Imprescindible** | RF01 — Registrar un vino probado (con foto y opinión) | No requiere |
| **Imprescindible** | RF02 — Consultar si ya probé un vino | No requiere |
| **Imprescindible** | Ver el detalle de un vino, editar y eliminar | No requiere |
| **Importante** | RF03 — Completar los datos del vino leyendo la etiqueta (en el teléfono) | No requiere |
| **Importante** | RF04 — Análisis de mis gustos | Estadísticas sin conexión; texto de IA con conexión |
| **Importante** | Cuenta (crear, iniciar sesión, recuperar y cambiar contraseña, eliminar) y respaldo en la nube | Requiere (se sincroniza al reconectar) |
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
| La lectura de la etiqueta falla o se equivoca (poca luz, tipografías artísticas) | Siempre es una sugerencia editable; nunca se guarda sin confirmación y siempre existe la carga manual. Se prueba con etiquetas reales al comienzo de la etapa 2; si no alcanza, el plan B es un modelo de IA con visión en la nube mediante una Edge Function. |
| Límites del plan gratuito de Groq o caída del servicio | Solo afecta al resumen de gustos: ante un error se ofrece reintentar y se muestra el último resumen guardado. Registrar, leer la etiqueta y respaldar no dependen de ese servicio, así que una falla de la IA no deja registros pendientes. |
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
  - *Dado* que "Rutini Malbec 2021" está en el diario, *cuando* el usuario registra "Rutini Malbec 2022", *entonces* se crea un vino nuevo (otra añada) y su ficha muestra "Otras añadas que probaste: 2021".
  - *Dado* que "Rutini Malbec 2021" está en el diario, *cuando* el usuario carga "Rutini Malbek 2021" (una letra distinta) y toca "Guardar", *entonces* aparece "¿Es el mismo vino?" con el vino parecido; si elige "Sí, es este vino" se agrega la degustación a ese vino, y si elige "No, es otro vino" se crea un vino nuevo.

**Regla para reconocer un mismo vino (RF01 y RF02).** Cada añada es un vino distinto: "Rutini Malbec 2020" y "Rutini Malbec 2021" tienen su propia ficha, sus propias degustaciones y su propio veredicto, porque la añada cambia mucho un vino. Para no duplicar fichas ni juntar vinos que no son el mismo, al guardar la app compara lo cargado con el diario:

1. Normaliza nombre y bodega: minúsculas, sin tildes ni signos y, en el nombre, sin el año.
2. Si la bodega está cargada en los dos y es distinta, o si la añada está cargada en los dos y es distinta, son vinos distintos. Si además los nombres coinciden, la ficha muestra "Otras añadas que probaste".
3. En cualquier otro caso se comparan los nombres normalizados:

| Situación | Qué hace la app |
|---|---|
| Nombres iguales y misma añada (o ninguno tiene añada) | Es el mismo vino: agrega la degustación y avisa "Sumamos esta vez a Rutini Malbec 2021", con "Deshacer". |
| Nombres parecidos (uno contiene al otro, o difieren en hasta 2 letras), o nombres iguales con la añada cargada solo en uno | No decide sola: pregunta "¿Es el mismo vino?" mostrando el vino parecido. El usuario elige "Sí, es este vino" o "No, es otro vino". |
| Nada parecido | Es un vino nuevo: lo crea. |

Ante la duda la app pregunta: confirmar cuesta un toque, y juntar por error dos vinos distintos hace perder información. **Limitación conocida:** la comparación es por texto, así que dos nombres escritos de forma muy distinta pueden no detectarse como el mismo vino; en la primera versión no hay una función para unir fichas duplicadas.

### RF02 — Consultar si ya probé un vino
- **Usuario:** consumidor ocasional, en el punto de venta.
- **Descripción:** el usuario podrá buscar por nombre, bodega o cepa en su diario y ver si ya probó el vino, cuántas veces, si le gustó y con qué lo acompañó.
- **Criterios de aceptación:**
  - *Dado* un diario que contiene "El Esteco Torrontés", *cuando* el usuario escribe "torrontes" o "ESTECO", *entonces* el vino aparece como resultado (la búsqueda ignora mayúsculas y tildes y acepta texto parcial).
  - *Dado* un diario con 500 vinos y el dispositivo sin conexión, *cuando* el usuario escribe en el buscador, *entonces* los resultados se muestran en menos de 1 segundo.
  - *Dado* que no hay coincidencias, *cuando* termina la búsqueda, *entonces* la app informa "No lo encontramos en tu diario" y ofrece registrarlo con el nombre ya cargado. El mensaje no afirma que el usuario nunca lo probó: puede haberlo tomado y no haberlo cargado, o haberlo cargado con otro nombre.
  - *Dado* que "Rutini Malbec 2021" está en el diario, *cuando* el usuario busca "rutini malbec 2022", *entonces* la app informa "No lo encontramos en tu diario" y, aparte, "Probaste otra añada: 2021". Si la búsqueda no da resultados y trae un año, se repite sin el año y esos resultados se marcan como otra añada.

### RF03 — Completar los datos del vino leyendo la etiqueta
- **Usuario:** consumidor ocasional, al registrar (con o sin conexión y con o sin cuenta).
- **Descripción:** a partir de la foto de la etiqueta, la app lee el texto en el teléfono y propone nombre, bodega, cepa y año. El usuario puede aceptar, editar o descartar cada dato.
- **Criterios de aceptación:**
  - *Dado* que el usuario sacó la foto de una etiqueta legible, *cuando* termina la lectura (sin bloquear el formulario), *entonces* los campos sugeridos se completan marcados como "sugerido", son editables y nunca se guardan sin que el usuario toque "Guardar".
  - *Dado* el dispositivo en modo avión, *cuando* el usuario saca la foto, *entonces* la lectura funciona igual, porque ocurre en el teléfono y no envía la foto a ningún servidor.
  - *Dado* que la lectura falla o no encuentra datos, *cuando* termina el intento, *entonces* se muestra un mensaje claro con la opción de reintentar, la carga manual sigue disponible y no se pierde lo ya cargado.

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
| RNF06 | Privacidad | Las fotos de las etiquetas no se envían a ningún servicio de IA: la lectura se hace en el teléfono (con cuenta, las fotos se respaldan en Supabase). Para el resumen de gustos se envían estadísticas, no notas ni fotos; esa función se puede desactivar desde Ajustes y la app pide consentimiento en el primer uso. El usuario puede eliminar su cuenta y su respaldo desde la app. |
| RNF07 | Confiabilidad | Los registros creados sin conexión se respaldan automáticamente al recuperar la conexión, sin intervención del usuario y sin pérdida de datos. |
| RNF08 | Recuperación ante errores | Ante un fallo de red, de la lectura de la etiqueta o del resumen con IA, la app muestra un mensaje en lenguaje simple con una acción de reintento y no se pierden los datos ingresados. |
| RNF09 | Permisos | Si el usuario rechaza el permiso de cámara, puede elegir una foto de la galería o registrar sin foto. |
| RNF10 | Consumo de batería y datos | La sincronización se ejecuta solo cuando hay red disponible (sin consultas periódicas) y las fotos se comprimen a un máximo de 500 KB antes de guardarse o subirse. |
| RNF11 | Compatibilidad | Android 8.0 (API 26) o superior, en pantallas de 320dp a 412dp de ancho. |

---

## 11. Diseño en Figma

**Link:** https://www.figma.com/design/NM0utJz4cc9JBflTfu1ep9/Sorbo-App

El diseño está construido en Figma como un sistema de componentes, no como imágenes:
- **Página "Sistema de diseño":** variables de color de Material 3 (con su equivalente `MaterialTheme.colorScheme` de Compose), variables de espaciado y radios, 10 estilos de texto, 32 íconos y 27 componentes con variantes y propiedades editables (botón, chip, campo de texto, barra de navegación, barra superior, buscador, ítem de vino, tarjeta de degustación, banner, diálogo, hoja inferior, estado vacío, entre otros).
- **Página "Pantallas":** 46 pantallas organizadas en 9 secciones por flujo, armadas con instancias de esos componentes y auto layout, más una décima sección con wireframes de baja fidelidad de las pantallas principales y una undécima con 4 pantallas descartadas de la versión anterior (lectura de etiquetas con IA en la nube). Un cambio en un componente se refleja en todas las pantallas.
- **Prototipo navegable:** más de 60 conexiones entre pantallas siguiendo el diagrama de flujo, con cuatro puntos de inicio: recorrido completo, Diario, Cuenta y Wireframes.
- Como referencia, el repositorio incluye los mockups originales en HTML y su exportación en PNG ([docs/mockups](mockups/)), que contemplan además el tema oscuro.

**Lineamientos de diseño:**
- **Material Design 3**, el sistema de diseño nativo de Android y el que implementa Jetpack Compose (`androidx.compose.material3`), para que el diseño se traslade directo a componentes reales.
- **Diseño adaptable:** base de 360dp de ancho, validado de 320dp a 412dp. Las listas y formularios ocupan el ancho disponible, sin medidas fijas, y respetan el tamaño de fuente del sistema.
- **Uso con una mano:** las acciones principales (registrar, guardar, navegar) están en el tercio inferior de la pantalla, en la zona que alcanza el pulgar.
- **Color con significado:** bordó como color de marca. Ninguna información depende solo del color: "me gustó / no me gustó" siempre se muestra con ícono y texto. Todo lo que sugiere la app (lectura de la etiqueta y resumen con IA) se identifica con un mismo color y el ícono ✦.

**Inventario de pantallas:**

| Grupo | Pantallas y estados |
|---|---|
| Primer uso | Splash, bienvenida, diario vacío. |
| Diario (inicio) | Con contenido, cargando, sin conexión, error. |
| ¿Ya lo probé? (RF02) | Buscador con búsquedas recientes, "Ya lo probaste", "No lo encontramos en tu diario". |
| Registrar (RF01 + RF03) | Explicación del permiso de cámara, leyendo etiqueta, sugerencia lista, sin conexión, error de lectura y validación, permiso de cámara rechazado, "¿Es el mismo vino?" (vino parecido ya cargado). |
| Detalle del vino | Detalle con confirmación de guardado, "Lo volví a probar" (nueva degustación), editar vino, confirmar eliminación, eliminado con "Deshacer". |
| Mis gustos (RF04) | Menos de 5 degustaciones, generando, análisis completo, sin conexión, requiere cuenta, error, aviso de IA en el primer resumen. |
| Cuenta | Iniciar sesión, error al iniciar sesión, crear cuenta, recuperar contraseña, "Revisá tu email", nueva contraseña (desde el enlace del email), respaldo de los vinos ya cargados al crear la cuenta, restaurar diario en un teléfono nuevo. |
| Ajustes y perfil | Ajustes con sesión, ajustes sin sesión, mi cuenta, cambiar contraseña, cerrar sesión con vinos sin respaldar, eliminar cuenta, cómo usamos tus datos. |
| Adaptabilidad | Diario en 320dp y 412dp (en los mockups HTML también en tema oscuro). |

**Navegación:** barra inferior con tres destinos (Diario, Mis gustos y Ajustes) y botón flotante para registrar desde el Diario.

**Decisiones de UX (heurísticas de Nielsen):**

| Heurística | Cómo se aplica |
|---|---|
| Visibilidad del estado del sistema | Indicador de sin conexión, de registros pendientes de respaldo y de "leyendo la etiqueta"; fecha del último análisis de gustos. |
| Relación con el mundo real | Vocabulario cotidiano ("me gustó", "dulce", "con amigos") en lugar de términos de cata. |
| Control y libertad del usuario | Lo que sugiere la lectura de la etiqueta siempre se puede editar; eliminar ofrece "Deshacer". |
| Consistencia y estándares | Componentes estándar de Material 3 (barra de navegación inferior, botón flotante, chips, snackbars, diálogos). |
| Prevención de errores | Solo dos campos obligatorios; opciones con chips en lugar de texto libre; confirmación antes de eliminar. |
| Reconocer antes que recordar | La foto identifica cada vino en la lista; las comidas y ocasiones son opciones visibles, no hay que escribirlas. |
| Flexibilidad y eficiencia | Registrar abre directamente la cámara; la app lee la etiqueta mientras el usuario ya responde si le gustó. |
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
      '-- No --> [Resultado: "No lo encontramos en tu diario"]
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
                                        ¿La app pudo leer la etiqueta?
                                         |-- Sí --> Sugiere nombre, bodega, cepa y año --> Usuario confirma o edita
                                         '-- No --> Carga manual de los datos
                                              |
                                              v
                                   ¿Hay un vino igual o parecido en el diario?
                                     |-- Igual -----> suma la degustación a ese vino
                                     |-- Parecido --> pregunta "¿Es el mismo vino?" (Sí: suma / No: vino nuevo)
                                     '-- Ninguno ---> crea el vino nuevo
                                              |
                                              v
                                        Guardar en Room --> [Detalle del vino] --> FIN
```

*Diagrama completo en la página "Flujo de pantallas" del archivo draw.io del repositorio.*

**Operaciones críticas:**
- **Guardar:** debe persistir siempre, con o sin conexión.
- **Aplicar una sugerencia de la lectura de la etiqueta:** nunca se guarda sola; requiere que el usuario toque "Guardar".
- **Eliminar:** pide confirmación y ofrece deshacer.

---

## 13. Arquitectura propuesta

Arquitectura **MVVM con principios de Clean Architecture**, en tres capas. **Room es la fuente única de verdad:** la UI siempre lee de la base local y la comunicación con la nube ocurre en segundo plano. La capa de dominio no depende de Android ni de Supabase, por lo que sus reglas se pueden probar con tests unitarios simples.

| Capa | Responsabilidades | Componentes |
|---|---|---|
| Presentación | Mostrar el estado y capturar las acciones del usuario | Pantallas en Jetpack Compose, un ViewModel por pantalla que expone `StateFlow<UiState>`, Navigation Compose |
| Dominio | Modelos y reglas del negocio | Modelos (`Usuario`, `Vino`, `Degustacion`, `AnalisisGustos`), casos de uso e interfaces (repositorios y `LectorEtiqueta`) |
| Datos | Obtener y guardar datos, sincronizar | Implementaciones de repositorio, Room (DAOs y entidades), cliente de Supabase, `SyncWorker` (respaldo) de WorkManager, lector de etiquetas con ML Kit, mappers entre entidades, modelos y DTOs |

**Casos de uso del dominio:**

| Caso de uso | Responsabilidad | Conexión |
|---|---|---|
| `RegistrarDegustacion` | Valida nombre y opinión; según la decisión de `BuscarVinoParecido` (y del usuario) crea el vino o agrega la degustación al existente | No |
| `BuscarVinoParecido` | Aplica la regla de la sección 9 y devuelve: mismo vino, vino parecido (hay que preguntar) o vino nuevo | No |
| `BuscarEnDiario` | Normaliza el texto (minúsculas, sin tildes) y busca por nombre, bodega o cepa; si no hay resultados y la búsqueda traía un año, repite sin el año y marca los resultados como otra añada | No |
| `ObtenerDetalleVino` | Devuelve el vino con sus degustaciones y las otras añadas del mismo nombre | No |
| `EliminarVino` | Borrado lógico del vino y sus degustaciones, con posibilidad de deshacer | No |
| `CalcularEstadisticasGustos` | Calcula, por ejemplo, "secos: 8 de 9 te gustaron" a partir del diario | No |
| `SugerirDatosDesdeFoto` | Lee la etiqueta en el teléfono (a través de `LectorEtiqueta`, implementado con ML Kit) y la interpreta con reglas simples (`InterpretarTextoEtiqueta`) para devolver nombre, bodega, cepa y año como sugerencia | No |
| `GenerarAnalisisGustos` | Envía las estadísticas a la IA y guarda el resumen con su fecha | Sí |
| `SincronizarDiario` | Sube los registros y las fotos pendientes (lo ejecuta el `SyncWorker`). No usa ningún servicio de IA | Sí |

**Estructura del proyecto (paquete `com.sorbo.app`):**

| Paquete | Capa | Contenido |
|---|---|---|
| `ui` | Presentación | Pantallas Compose y ViewModels (diario, búsqueda, registro, detalle, gustos, ajustes), tema y navegación |
| `domain` | Dominio | `model` (modelos), `usecase` (casos de uso), `repository` (interfaces) |
| `data` | Datos | `local` (Room: entidades y DAOs), `remote` (Supabase: DTOs), `ml` (lector de etiquetas con ML Kit), `repository` (implementaciones), `sync` (SyncWorker), `mapper` |
| `AppContainer` | Transversal | Crea las dependencias y se las pasa a los ViewModels |

### Flujo de datos del caso principal: registrar un vino

**Lectura de la etiqueta (al sacar la foto, con o sin conexión)**
- El ViewModel invoca `SugerirDatosDesdeFoto`, que pide el texto de la foto a `LectorEtiqueta` (ML Kit, en el teléfono) y lo interpreta con `InterpretarTextoEtiqueta`: el año por su patrón numérico, la cepa por una lista de variedades, y la bodega y el nombre por la posición y el tamaño del texto.
- El formulario muestra los datos como sugeridos y editables. Si la lectura falla o no encuentra datos, muestra el error con "Reintentar" y el usuario carga a mano. Guardar nunca espera a la lectura.

**A. Guardado local (funciona con o sin conexión)**
1. La UI envía el evento `onGuardar(formulario)` al `RegistroViewModel`.
2. El ViewModel consulta `BuscarVinoParecido` (si hay un vino parecido, pregunta "¿Es el mismo vino?") e invoca el caso de uso `RegistrarDegustacion`, que valida las reglas de negocio.
3. El caso de uso llama a `VinoRepository.guardar(vino, degustacion)`.
4. El repositorio inserta el vino y la degustación en Room con `syncState = PENDIENTE`.
5. El repositorio encola un `SyncWorker` en WorkManager con la restricción "requiere red".
6. Room emite el diario actualizado mediante `Flow`; el ViewModel actualiza el `UiState` y la UI muestra "Vino guardado en tu diario".

**B. Al recuperar la conexión (en segundo plano, aunque la app esté cerrada)**

El respaldo no usa ningún servicio de IA. El único servicio de IA en la nube es el resumen de gustos (RF04), que no participa del guardado ni del respaldo: una falla suya nunca deja registros pendientes.

7. El `SyncWorker` lee los registros pendientes de Room.
8. Los sube a Supabase (Postgrest para los datos, Storage para la foto).
9. Marca los registros como `SINCRONIZADO` y Room emite el cambio para que la UI quite el indicador de "pendiente".

Si el respaldo falla, el registro sigue `PENDIENTE` (todavía no está respaldado) y WorkManager reintenta con backoff exponencial.

### Topología de infraestructura

```
+-----------------------------+
|  Dispositivo Android        |
|  - App (Kotlin + Compose)   |
|  - Room (SQLite)            |
|  - Fotos (almacenamiento    |
|    interno) y DataStore     |
|  - WorkManager              |
|  - ML Kit (lee etiquetas)   |
+--------------+--------------+
               | HTTPS (supabase-kt)
               v
+-----------------------------+
|  Supabase                   |
|  - Auth (email y clave)     |
|  - Postgres + RLS           |
|  - Storage (fotos)          |
|  - Edge Functions           |
|      analizar-gustos        |
+--------------+--------------+
               | HTTPS (API key solo en el servidor)
               v
+-----------------------------+
|  Groq (LLM de texto)        |
+-----------------------------+
```

*Diagramas completos en las páginas "Arquitectura en capas", "Flujo de datos - Registrar vino" y "Topología de infraestructura" del archivo draw.io del repositorio.*

### Estrategia de pruebas (testabilidad)
- **Tests unitarios del dominio:** casos de uso como `RegistrarDegustacion`, `BuscarVinoParecido`, `BuscarEnDiario`, `SugerirDatosDesdeFoto`, `CalcularEstadisticasGustos` y `SincronizarDiario`, usando repositorios y servicios falsos en memoria. Casos que se prueban a propósito:
  - Búsqueda sin resultados: la respuesta es "no encontrado en el diario", nunca "no probado"; con un año sin resultados se ofrece la otra añada.
  - Identidad de un vino: mismo nombre y misma añada, nombre con una letra distinta ("Malbek"), añadas distintas, añada cargada solo en uno, bodegas distintas y nombre contenido en otro.
  - Interpretación de la etiqueta: `InterpretarTextoEtiqueta` con textos de etiquetas de ejemplo (año, cepa, bodega y nombre) y con un texto sin datos reconocibles.
  - Lectura fallida: con un `LectorEtiqueta` falso que siempre falla, el registro se guarda igual con los datos cargados a mano y `SincronizarDiario` lo respalda sin llamar a ningún servicio de IA.
  - Resumen con IA caído: con un servicio falso que falla, `GenerarAnalisisGustos` informa el error y conserva el último resumen, sin afectar registros ni respaldo.
- **Tests de ViewModels:** verificar que cada acción produce el `UiState` esperado (cargando, contenido, vacío, error).
- **Tests de Room:** DAOs probados con una base en memoria (búsqueda sin tildes, relación vino–degustaciones).
- **Pruebas manuales de conectividad:** cada requisito se valida en modo avión, perdiendo la conexión a mitad de una operación y recuperándola, y con la Edge Function del resumen apagada para comprobar que registrar y respaldar siguen funcionando. La lectura de etiquetas se prueba además con fotos reales (ver riesgos).

---

## 14. Persistencia y estrategia Offline First

### Entidades principales

| Entidad | Atributos principales | Dónde se guarda |
|---|---|---|
| **Usuario** | `id` (el mismo de Supabase Auth), `email`, `nombre`, `creadoEn` | Supabase (`auth.users` + tabla `perfiles`); en el teléfono, la sesión activa |
| **Vino** | `id` (UUID), `usuarioId`, `nombre`, `nombreNormalizado`, `textoBusqueda`, `bodega`, `cepa`, `anio`, `fotoPath`, `fotoUrl`, `creadoEn`, `actualizadoEn`, `syncState`, `eliminado` | Room y Supabase |
| **Degustacion** | `id` (UUID), `vinoId`, `fecha`, `meGusto`, `dulzor`, `comida`, `compania`, `ocasion`, `nota`, `actualizadoEn`, `syncState`, `eliminado` | Room y Supabase |
| **AnalisisGustos** | `id`, `usuarioId`, `resumen`, `sugerencias`, `generadoEn`, `cantidadDegustaciones` | Solo Room (caché del último análisis) |

**Relaciones:**
- Un **Usuario** tiene muchos **Vinos** (1:N): cada persona tiene su propio diario.
- Un **Vino** tiene muchas **Degustaciones** (1:N): el mismo vino puede probarse varias veces, con opiniones distintas.
- Un **Usuario** tiene un **AnalisisGustos** vigente (1:0..1).

**Decisiones de modelado:**
- **El vino pertenece al usuario y no a un catálogo global**, coherente con el fuera de alcance: no hay que mantener una base de vinos del mundo.
- **`nombreNormalizado`** (minúsculas, sin tildes ni signos y sin el año) sirve para reconocer si dos registros son el mismo vino. **`textoBusqueda`** junta nombre, bodega, cepa y año ya normalizados: el buscador de RF02 hace una consulta simple sobre ese único campo, que con 500 vinos responde muy por debajo de 1 segundo.
- **La añada es parte del vino:** cada añada es un vino distinto, con su propia ficha, degustaciones y veredicto, porque la añada cambia mucho el vino. La ficha enlaza las otras añadas del mismo nombre ("Otras añadas que probaste").
- **Comida, compañía, ocasión y dulzor son listas cerradas** (enumeraciones con una opción "Otra"): hacen posible calcular estadísticas confiables en RF04, algo que el texto libre no permitiría.
- **Las sugerencias de la lectura de la etiqueta no se guardan:** viven en el formulario hasta que el usuario toca "Guardar", así nunca pisan datos del usuario sin que él lo decida y el modelo queda más simple.
- **`usuarioId` puede estar vacío** en los vinos cargados antes de crear una cuenta; al iniciar sesión se asignan al usuario y se respaldan.
- **`syncState`, `actualizadoEn` y `eliminado`** son los campos que permiten sincronizar: qué falta subir, qué versión es más nueva y qué se borró.

*Diagrama completo en la página "Modelo de datos" del archivo draw.io del repositorio.*

**Origen de la información:**
- **Generada por el usuario:** vinos, degustaciones y fotos.
- **Proveniente de servicios externos:** resumen de gustos (IA vía Edge Function). **Generada en el teléfono:** sugerencias de datos del vino a partir de la foto (ML Kit).
- **Almacenada localmente:** vinos, degustaciones y último análisis en Room; fotos en el almacenamiento interno con su ruta en Room; preferencias (bienvenida vista, aviso de IA aceptado, IA activada, búsquedas recientes) en DataStore. La sesión de Supabase la guarda supabase-kt de forma segura en el dispositivo.

### Comportamiento según conectividad

| Situación | Comportamiento |
|---|---|
| Con conexión | Todas las funciones disponibles. Los cambios se guardan primero en Room y se respaldan en Supabase en segundo plano. |
| Pierde conexión | Registro, consulta, edición, eliminación, lectura de la etiqueta y estadísticas siguen funcionando. El resumen con IA indica "Requiere conexión". Los registros nuevos quedan con `syncState = PENDIENTE`. |
| Recupera conexión | WorkManager sube los registros pendientes (respaldo). El resumen de IA se actualiza cuando el usuario lo pide. |
| No se puede leer la etiqueta | El vino se guarda igual con los datos cargados a mano; el formulario ofrece "Reintentar" la lectura. |
| Falla el resumen con IA | Se muestra el último resumen guardado con su fecha y la acción "Reintentar"; registrar y respaldar no se ven afectados. |
| Información local desactualizada | Las estadísticas siempre están al día porque se calculan en el teléfono. El resumen de IA muestra su fecha y cuántas degustaciones nuevas hay desde entonces, con la opción de actualizarlo. |
| Sin información local | Estado vacío con acceso a "Registrar mi primer vino". Si el usuario inicia sesión y tenía un respaldo, se ofrece restaurarlo desde la nube. |

### Estrategia
- **Qué se guarda localmente:** todo el diario (vinos, degustaciones, fotos) y el último análisis de gustos.
- **Fuente remota:** Supabase (Postgres para los datos, Storage para las fotos, Edge Functions para la IA).
- **Cuándo se actualizan los datos:** al guardar un cambio se intenta respaldar de inmediato; si no hay red, WorkManager lo hace al recuperarla. El resumen de IA se actualiza a pedido del usuario.
- **Qué ve el usuario sin conexión:** todo su diario y sus estadísticas, con un indicador discreto de "sin conexión" y de registros pendientes.
- **Errores y conflictos:** al tratarse de un solo dispositivo por usuario, se aplica "última escritura gana" según `actualizadoEn`. Los borrados son lógicos (`eliminado = true`) para que también se propaguen. Si una sincronización falla, se reintenta con backoff exponencial y el registro sigue disponible en el teléfono. El respaldo no depende de ningún servicio de IA: una falla de la lectura de la etiqueta o del resumen no deja registros pendientes.

---

## 15. Tecnologías previstas y justificación

| Tecnología | Justificación |
|---|---|
| Kotlin + Android | Lenguaje y plataforma requeridos; acceso nativo a cámara, almacenamiento y tareas en segundo plano. |
| Jetpack Compose + Material 3 | UI declarativa que representa cada estado de pantalla a partir de un `UiState`; Material 3 es el sistema de diseño usado en Figma. |
| Navigation Compose | Navegación entre pantallas y paso de parámetros (por ejemplo, el id del vino al detalle). |
| ViewModel + StateFlow | Conservar el estado de pantalla ante rotaciones y exponerlo de forma reactiva a la UI. |
| Coroutines + Flow | Operaciones asíncronas (base de datos, red) sin bloquear la UI; Room expone `Flow` que actualizan la UI automáticamente. |
| Room | Persistencia estructurada con relaciones (usuario–vinos–degustaciones) y búsqueda sobre un campo normalizado; es la fuente de verdad que permite el funcionamiento offline. |
| DataStore | Preferencias simples (bienvenida vista, aviso de IA aceptado, IA activada, búsquedas recientes). |
| ActivityResult `TakePicture` + Photo Picker | Capturar o elegir la foto de la etiqueta con la cámara del sistema; es más simple que implementar una cámara propia con CameraX y cubre la necesidad. |
| Coil | Carga eficiente de las fotos de etiquetas en Compose. |
| WorkManager | Respaldo de los registros pendientes al volver la conexión, aunque la app esté cerrada, respetando la batería. |
| ML Kit (Text Recognition v2, modelo incluido en la app) | Lee el texto de la etiqueta en el teléfono, sin conexión, sin cuenta y sin enviar la foto a ningún servidor; suma unos 4 MB a la app. Es la alternativa más simple que cumple (ver "Alternativas de IA para leer la etiqueta"). |
| Supabase (supabase-kt: Auth, Postgrest, Storage, Functions) | Backend en un solo servicio: autenticación con email y contraseña, respaldo de datos, almacenamiento de fotos y funciones de servidor. El login es opcional: la app se usa completa sin cuenta y la cuenta habilita el respaldo y el resumen con IA. |
| Supabase Edge Functions | Intermediario hacia la IA del resumen de gustos para no exponer la API key dentro de la aplicación. |
| Groq (LLM de texto) | Redacta el resumen de gustos y las sugerencias a partir de estadísticas (no recibe fotos ni notas). Se eligió por su plan gratuito suficiente para el proyecto, baja latencia y API compatible con OpenAI, lo que permite cambiar de proveedor modificando solo la Edge Function. Los modelos de Groq se dan de baja con frecuencia (Llama 4 Scout dejó de estar disponible el 17 de julio de 2026), por eso el nombre del modelo vive como variable de la Edge Function y se verifica al implementar. Como plan B para leer etiquetas con visión en la nube, el modelo vigente al 3 de noviembre de 2026 es `qwen/qwen3.8-27b`. |
| ConnectivityManager (`NetworkCallback`) | Detectar el estado de la conexión para mostrar el indicador y habilitar o deshabilitar el resumen con IA y mostrar el estado del respaldo. |

---

## 16. Repositorio y forma de trabajo

- **URL del repositorio:** https://github.com/alejogarciabosio/sorbo-app
- **Integrantes:** Alejo Garcia Bosio, Iñaki Courtinade, Nino Bizzotto.
- **Estrategia de ramas:** `main` (versión estable, solo recibe cambios desde `develop`), `develop` (integración) y una rama por funcionalidad creada desde `develop`: `feature/rf01-registrar-vino`, `feature/rf02-busqueda`, etc.
- **Commits:** Conventional Commits (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`), pequeños y frecuentes, en español.
- **Integración:** cada funcionalidad se desarrolla en su rama `feature/*` y se integra a `develop` con un merge directo, sin pull request, después de verificar que compile, que pasen los tests y que cumpla los criterios de aceptación del requisito. `develop` pasa a `main` cuando hay una versión estable.
- **Organización:** tareas en GitHub Projects, una por requisito o pantalla, asignadas a un responsable.

---

## 17. Integrantes, roles y responsabilidades

| Integrante | Rol principal | Responsabilidades | Áreas en las que participa |
|---|---|---|---|
| Alejo Garcia Bosio | Arquitectura y documentación | Definición de capas y estructura del proyecto, configuración de Supabase (Auth, tablas, Row Level Security, Storage), Edge Function de IA (resumen de gustos), README y documentación de decisiones | Dominio, backend, documentación |
| Iñaki Courtinade | Integración, persistencia y sensores | Base de datos local con Room, sincronización con Supabase mediante WorkManager, manejo de conectividad, captura de fotos con la cámara, lectura de etiquetas con ML Kit | Datos, offline first, capacidades del dispositivo |
| Nino Bizzotto | UI, UX y testing | Diseño en Figma, pantallas en Jetpack Compose, estados de pantalla, navegación, accesibilidad y tests | Presentación, calidad |

**Cambios en los miembros del equipo:** ninguno.

*Los roles no son exclusivos: todos los integrantes participan en el desarrollo y comprenden el funcionamiento general de la aplicación.*
