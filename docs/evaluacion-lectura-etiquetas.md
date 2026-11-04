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
| Tesseract 5 | Computadora | Texto ilegible con estas fotos | Descartada |
| EasyOCR, docTR, Surya, TrOCR, Florence-2 | No se probaron en el teléfono | Sin versión para móvil lista para usar; modelos de cientos de MB | Descartadas |
| Gemini Nano (ML Kit GenAI) | No se probó | Según la lista oficial de Google (noviembre de 2026) solo funciona en teléfonos de gama alta recientes; el S23 Ultra no figura. En beta | Descartada como base |
| Gemma 4 E2B y Qwen3-VL-2B (modelos de visión propios) | Computadora | Leyeron bien algunos nombres que ML Kit falla, pero mezclan campos e inventan datos (bodegas que no existen). Descarga de 2 a 3 GB | Descartadas como base; posible mejora opcional futura |
| SmolVLM2 500M | Computadora | Inventó texto | Descartada |

Técnicas de imagen: la lectura girada 90° ayuda con botellas acostadas; mejorar el contraste (CLAHE) no aportó una mejora clara.

Los resultados de la computadora no equivalen a un teléfono (otro procesador y otra cuantización): solo indican qué tan bien lee cada modelo.

## Hallazgos útiles para el resto del proyecto

- Limitar la app a la arquitectura `arm64-v8a` baja el APK de 52 a 22 MB solo con ML Kit (los teléfonos actuales son arm64; dejan de andar los emuladores x86).
- Con la pantalla apagada o en reposo, la lectura puede ser hasta 8 veces más lenta: conviene leer con la pantalla encendida.
- El año 2022 de una de las etiquetas (letra gris sobre gris) no lo lee ningún OCR liviano; el usuario siempre puede editar lo sugerido.

## Decisión y camino siguiente

1. **Base sin conexión: ML Kit con las reglas mejoradas.** Funciona sin red, sin cuenta y sin que la foto salga del teléfono (RNF01, RNF06).
2. **Mejora opcional con conexión (a implementar y validar):** si hay conexión y el usuario lo permite, se puede pedir a un modelo con visión en la nube que lea mejor la etiqueta, a través de una Edge Function de Supabase para no incluir ninguna clave en la app (RNF05). El resultado se muestra como sugerencia editable, igual que hoy, y si no hay respuesta en pocos segundos se queda con la lectura local.
3. Antes de construirla, hay que medir la precisión de ese modelo con estas mismas fotos, revisar las condiciones del plan gratuito del proveedor y definir cómo se pide el consentimiento, porque cambia lo que dice RNF06 sobre las fotos.
