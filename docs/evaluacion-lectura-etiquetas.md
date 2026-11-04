# Evaluación de la lectura de etiquetas (RF03)

*Noviembre de 2026. Resumen de lo que se probó para completar nombre, bodega, cepa y año a partir de la foto de la etiqueta, y qué se decidió. Complementa la sección 7.3 de la [preentrega](preentrega.md), que dejó ML Kit como elegida y un modelo con visión en la nube como plan B.*

## Criterio y muestra

- **Criterio de la preentrega:** año y cepa correctos en al menos 7 de cada 10 etiquetas, y nombre útil en al menos 5 de cada 10.
- **Muestra:** 7 fotos reales de botellas argentinas sacadas con un Samsung S23 Ultra, varias con la botella acostada, tipografías decorativas, letra dorada o gris sobre fondo oscuro, etiquetas curvas y otras botellas de fondo.
- **Límite:** son pocas fotos, de una sola persona y un solo teléfono. Sirven para comparar opciones entre sí, no para dar un porcentaje definitivo.

## Qué se evaluó

| Opción | Dónde corrió | Resultado | Decisión |
|---|---|---|---|
| ML Kit Text Recognition v2 + reglas iniciales | Teléfono | Año y cepa a la vez: 4 de 7 | Punto de partida |
| ML Kit + reglas mejoradas (cepa tolerante a una letra mal leída, nombre armado con los renglones grandes cercanos al año y la cepa, lectura también girada 90°) | Teléfono | Año y cepa a la vez: **6 de 7**. Nombre legible: 4 de 7 | **Elegida como base sin conexión** |
| PaddleOCR PP-OCRv6 (ONNX Runtime) | Teléfono | Año y cepa: 6 de 7 (igual). Nombre legible: 5 de 7. Lee mejor "CHAÑARMUYO", "JOSÉ ZUCCARDI" y "HUENTALA WINES". Tarda 2,2 a 2,9 s contra 0,2 a 0,3 s. APK de 88,6 MB contra 22,2 MB. Unos 215 MB más de memoria al leer. La variante *tiny* es más rápida pero lee peor | Descartada como base: mejora modesta a un costo alto. Queda en la rama `experimento/ocr-paddle` |
| Groq, modelo con visión `qwen/qwen3.8-27b` (plan gratuito) | Nube | Año y cepa a la vez: **7 de 7**. Nombre útil: 6 de 7. Lee el año 2022 de Desierto Pampa, que ninguna otra opción leyó. Tarda 1 a 2 s. Fallos: en Antes y Después tomó el texto de otra botella del fondo, y una bodega salió con una letra inventada ("ARGENTALA"). Con el primer pedido llegó a inventar un nombre que no figuraba. Límite del plan gratuito: unas 3 fotos por minuto (cada foto cuesta unos 2.000 tokens y el tope es de 7.000 por minuto) | Mejor lectura medida; requiere conexión. Candidata para la mejora opcional |
| Tesseract 5 | Computadora | Texto ilegible con estas fotos | Descartada |
| EasyOCR, docTR, Surya, TrOCR, Florence-2 | No se probaron en el teléfono | Sin versión para móvil lista para usar; modelos de cientos de MB | Descartadas |
| Gemini Nano (ML Kit GenAI) | No se probó | Según la lista oficial de Google (noviembre de 2026) solo funciona en teléfonos de gama alta recientes; el S23 Ultra no figura. En beta | Descartada como base |
| Gemma 4 E2B y Qwen3-VL-2B (modelos de visión propios) | Computadora | Leyeron bien algunos nombres que ML Kit falla, pero mezclan campos e inventan datos (bodegas que no existen). Descarga de 2 a 3 GB | Descartadas como base; posible mejora opcional futura |
| SmolVLM2 500M | Computadora | Inventó texto | Descartada |

Técnicas de imagen: la lectura girada 90° ayuda con botellas acostadas; mejorar el contraste (CLAHE) no aportó una mejora clara.

Los resultados de la computadora no equivalen a un teléfono (otro procesador y otra cuantización): solo indican qué tan bien lee cada modelo.

## Hallazgos útiles para el resto del proyecto

- Limitar el APK de entrega a la arquitectura `arm64-v8a` lo baja de unos 50 a unos 21 MB (medido). Se aplicó solo a la versión de release: la de desarrollo conserva todas las arquitecturas para poder usar emuladores x86.
- Con la pantalla apagada o en reposo, la lectura puede ser hasta 8 veces más lenta: conviene leer con la pantalla encendida.
- El año 2022 de una de las etiquetas (letra gris sobre gris) no lo lee ningún OCR liviano; el usuario siempre puede editar lo sugerido.

## Decisión y camino siguiente

1. **Base sin conexión: ML Kit con las reglas mejoradas.** Funciona sin red, sin cuenta y sin que la foto salga del teléfono (RNF01, RNF06).
2. **Mejora opcional con conexión: modelo con visión en la nube (Groq).** En la prueba fue la opción que mejor leyó las 7 fotos (año y cepa 7 de 7; nombre útil 6 de 7, contra 4 de 7 de ML Kit). Se pediría a través de una Edge Function de Supabase para no incluir ninguna clave en la app (RNF05). El resultado se muestra como sugerencia editable, como hoy, y si no hay respuesta en 4 o 5 segundos o falla (por ejemplo por el límite del plan gratuito), se queda con la lectura local.
3. **Cuidados al implementarla:**
   - El modelo a veces responde con datos que parecen razonables pero no están en la etiqueta (lo observado: un nombre inventado, el texto de otra botella del fondo). Conviene cruzar lo que devuelve con el texto que lee ML Kit y descartar lo que no aparece en él.
   - Cambia lo que dice RNF06 sobre las fotos: hay que pedir consentimiento la primera vez y permitir desactivarlo en Ajustes. Groq informa que por defecto no conserva los datos de las consultas (puede guardar registros hasta 30 días por seguridad y se puede desactivar). Con el plan gratuito de Gemini, en cambio, Google usa lo que se envía para mejorar sus productos, por lo que no se eligió.
   - Groq tiene un único modelo con visión y da de baja modelos con frecuencia: el nombre del modelo debe ser una variable de la Edge Function.
   - Falta repetir la medición con más etiquetas (la muestra es de 7 y el segundo pedido se ajustó mirando esas mismas fotos, por lo que el resultado puede ser algo optimista).

## Cómo se evaluó
Las pruebas con ML Kit se hicieron dentro de la app, sobre el celular. PaddleOCR se midió en una rama de experimento (`experimento/ocr-paddle`, con una bandera `LECTOR_ETIQUETA` para elegir el lector). Groq se probó enviando las 7 fotos reducidas a 1280 px a su API de chat con imagen y salida en JSON.
