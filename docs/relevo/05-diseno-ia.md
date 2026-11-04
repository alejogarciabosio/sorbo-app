# Diseño de la IA en la nube

*Para Alejo (A3, A4) y para quien integre la respuesta en la app (I4, N5). Hay dos usos de IA, los dos **opcionales**: la app funciona completa sin ellos.*

| Uso | Estado | Quién |
|---|---|---|
| **Resumen de gustos** (RF04): texto y sugerencias a partir de estadísticas | Contrato y versión simulada hechos; falta la función en la nube y el cliente real | A3, I4, N5 |
| **Lectura de etiqueta en la nube** (mejora opcional de RF03) | Probada en el escritorio, no integrada | A4 (opcional) |

## Reglas que valen para los dos

1. **Ninguna clave va en la app ni en el repo.** El teléfono llama a una Edge Function de Supabase; la función guarda la clave del proveedor como secreto del lado servidor.
2. **Requieren cuenta.** La función verifica el token del usuario (`Authorization: Bearer`), así no queda abierta a cualquiera y se puede limitar por usuario.
3. **Consentimiento.** La primera vez se muestra el aviso (E7) y queda guardado (`avisoIaAceptado`). En Ajustes se puede desactivar (`iaActivada`).
4. **Nada bloquea al usuario.** Con un tiempo de espera corto y cualquier falla, la app sigue con lo local y avisa en lenguaje simple.
5. **Qué sale del teléfono, y qué no:**
   - Resumen de gustos: **solo estadísticas** (cantidades y porcentajes). **Nunca fotos, nombres de vinos ni notas.**
   - Lectura de etiqueta: la **foto de la etiqueta** (esto cambia lo que dice RNF06, por eso exige consentimiento propio y debe quedar reflejado en la pantalla de privacidad G7).
6. **El nombre del modelo es una variable de la función**, no está escrito en el código: los proveedores dan de baja modelos con frecuencia.
7. **Nada de lo que devuelve el modelo se confía a ciegas:** se valida la forma de la respuesta y se acota el largo antes de devolverla.

## Proveedor: Groq

Elegido porque tiene plan gratuito, responde en 1 a 2 segundos y informa que por defecto no conserva los datos de las consultas (puede guardar registros hasta 30 días por seguridad; hay una opción para desactivarlo). Se descartó Gemini gratuito porque Google usa lo que se envía para mejorar sus productos.

- Endpoint compatible con el formato de chat de OpenAI: `POST https://api.groq.com/openai/v1/chat/completions` con `Authorization: Bearer <clave>`.
- **Límites del plan gratuito** (medidos en las pruebas): del orden de 3 fotos por minuto con el modelo de visión (cada foto cuesta unos 2.000 tokens y el tope es de unos 7.000 por minuto). Al pasarse responde **HTTP 429**: la función lo traduce a `LIMITE_ALCANZADO`. El texto del resumen (sin imagen) consume mucho menos.
- Modelo con visión probado: `qwen/qwen3.8-27b`. Para texto puede usarse un modelo de texto más barato. **Revisar en la consola de Groq qué modelos están vigentes el día que se implemente.**
- La clave se carga como secreto de la función (`supabase secrets set GROQ_API_KEY=…` o desde el panel). **Una clave se compartió por chat durante las pruebas y debe estar revocada**; la definitiva la crea Alejo y solo vive en los secretos de Supabase.

---

## A3 — `analizar-gustos` (resumen de gustos)

### Contrato con la app

Lo que espera el cliente es `AnalisisGustosRepository.generar(estadisticas): Result<AnalisisGustos>` (ver `domain/repository`).

**Pedido** (`POST /functions/v1/analizar-gustos`, con `Authorization: Bearer <token del usuario>` y `apikey`): las estadísticas que calcula `CalcularEstadisticasGustos` (`EstadisticasGustos`), serializadas en JSON. Mirar esa clase para los campos exactos y **no agregar nada que no sea numérico o una categoría**.

**Respuesta correcta (200):**
```json
{ "resumen": "texto corto", "sugerencias": ["…", "…", "…"] }
```
El cliente completa `generadoEn` y `cantidadDegustaciones`.

**Errores** (cuerpo `{ "error": "<codigo>" }`), que el cliente traduce a `MotivoAnalisis`:

| HTTP | `error` | `MotivoAnalisis` |
|---|---|---|
| 401 | `sin_sesion` | `SIN_SESION` |
| 422 | `pocas_degustaciones` | (no debería ocurrir: la app ya exige 5) |
| 429 | `limite` | `LIMITE_ALCANZADO` |
| 502 / 503 / 504 | `servicio` | `SERVICIO_NO_DISPONIBLE` |
| otro | — | `ERROR_DESCONOCIDO` |
| (sin respuesta de red) | — | `SIN_CONEXION` |

### Lo que hace la función

1. Verifica el token y obtiene el usuario.
2. **Limita por usuario** (por ejemplo, 5 resúmenes por día) con una tabla `uso_ia(usuario_id, dia, cantidad)` y responde 429 si se pasó. Así un usuario no agota el cupo gratuito de todos.
3. Valida que el cuerpo tenga solo los campos esperados, con tipos numéricos, y que sumen al menos 5 degustaciones.
4. Arma el pedido a Groq (abajo), con `temperature` baja, `max_completion_tokens` acotado y tiempo de espera (~15 s).
5. Valida y recorta la respuesta (resumen de hasta ~600 caracteres; hasta 4 sugerencias de hasta ~120).
6. **No guarda las estadísticas ni la respuesta.** Solo el contador de uso.

### Prompt sugerido (a ajustar al ver respuestas reales)

> Sos un sommelier amable que habla en español rioplatense. Recibís estadísticas del diario de vinos de una persona (cantidades y porcentajes; no tenés sus notas ni los nombres de los vinos). Escribí un resumen de 2 o 3 oraciones sobre qué estilo de vinos le gusta, y de 2 a 4 sugerencias concretas de qué probar a continuación (cepas, estilos o zonas, no marcas). Usá solo lo que está en las estadísticas; si algo no se puede afirmar, no lo afirmes. No hagas diagnósticos ni recomendaciones de salud. Respondé solo un JSON con las claves `resumen` (texto) y `sugerencias` (lista de textos).

Pedir el formato JSON del proveedor (`response_format: { "type": "json_object" }`) y, si algo falla al interpretarlo, responder `servicio`.

---

## A4 (opcional) — `leer-etiqueta` (lectura en la nube)

**Hacerla solo si lo anterior está terminado.** ML Kit (en el teléfono, sin red) ya cumple el rol de base; ver la evaluación en [`docs/evaluacion-lectura-etiquetas.md`](../evaluacion-lectura-etiquetas.md). En la muestra de 7 etiquetas, el modelo en la nube leyó mejor (año y cepa 7 de 7 y nombre útil 6 de 7, contra 6 de 7 y 4 de 7 de ML Kit), pero es una muestra chica y a veces inventa.

### Comportamiento en la app

1. El usuario elige o saca la foto. **ML Kit lee primero, siempre** y completa los campos como hoy.
2. Si hay cuenta, conexión, consentimiento y `iaActivada`, **en paralelo** se pide la lectura en la nube con un tiempo de espera de **4 a 5 segundos**.
3. Si llega a tiempo, se mejora lo sugerido; si no, o falla (429 incluido), **se queda lo de ML Kit sin avisar de ningún error**.
4. **Cruce de seguridad:** del resultado de la nube solo se aceptan los datos cuyo texto aparece también en lo que leyó ML Kit (por ejemplo, la cepa debe figurar en el texto leído). Lo observado en las pruebas: un nombre inventado y el texto de otra botella del fondo. Lo que no se pueda confirmar se descarta.
5. Todo sigue siendo una **sugerencia editable**; nunca se guarda sin que el usuario toque "Guardar".
6. La foto se envía **reducida** (1280 px del lado largo alcanzó en las pruebas) para gastar menos cupo.

### Función

`POST /functions/v1/leer-etiqueta` con la imagen en base64 y el token del usuario. Misma estructura que `analizar-gustos`: verifica sesión, limita por usuario, valida el tamaño de la imagen, llama a Groq y devuelve `{ nombre, bodega, cepa, anio }` con `null` donde no hay dato. No guarda la foto.

### Prompt probado (versión 2)

> Esta es la foto de una botella de vino. Hay UNA botella principal (la que está en primer plano, sostenida por la mano): leé solo su etiqueta e ignorá otras botellas, papeles o textos del fondo. Devolvé solo un JSON con estas claves: nombre (la marca o nombre del vino tal como se lee, sin la cepa ni el año), bodega (el productor; si la etiqueta solo muestra una marca y no hay otro productor, repetí esa marca), cepa (la variedad de uva, p. ej. Malbec; si hay dos, separalas con ' y '), anio (número), lineas (lista con todas las líneas de texto que leés en esa etiqueta, de la más grande a la más chica). Si un dato no figura o no se lee con claridad, poné null. No inventes nada: es mejor null que adivinar.

Parámetros usados: `temperature: 0`, `max_completion_tokens: 700`, `response_format: json_object`, `reasoning_effort: none`. El campo `lineas` sirve para el cruce de seguridad con ML Kit.

La primera versión del prompt ("Extraé nombre, bodega, cepa y año… Respondé solo el JSON") llegó a inventar un nombre. La versión 2 se ajustó mirando esas mismas 7 fotos, por lo que su resultado puede ser algo optimista.

### Antes de darla por buena

- Repetir la medición con **10 o más etiquetas nuevas**, distintas de las 7 usadas para ajustar el prompt, y anotar el resultado en `docs/evaluacion-lectura-etiquetas.md`.
- Actualizar G7 (privacidad) y el aviso de consentimiento: ahora la foto sale del teléfono.
- Probarla sin red, con el cupo agotado y con una respuesta tardía.

## Qué probar de la IA (para quien la integre)

- Sin cuenta: el botón de resumen explica que se necesita cuenta (E5).
- Sin conexión: se ve el resumen anterior con su fecha (E4).
- Límite alcanzado: mensaje propio y sin perder lo que ya se ve (E6).
- Servicio caído / respuesta mal formada: error genérico con reintento.
- Con `iaActivada = false`: no se llama a la nube en ningún caso.
- Revisar en los registros de la función que **no se imprimen** las estadísticas, la imagen ni los tokens.
