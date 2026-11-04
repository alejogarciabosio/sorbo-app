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

Kotlin · Jetpack Compose · MVVM + Clean Architecture · Room · WorkManager · ML Kit (lectura de etiquetas en el teléfono) · Supabase (Auth, Postgres, Storage, Edge Functions) · LLM de texto (resumen de gustos)

## Cómo ejecutar

1. Abrir la carpeta del repositorio en Android Studio y esperar la sincronización de Gradle.
2. Copiar `local.properties.example` como `local.properties` y completar `SUPABASE_ANON_KEY` (pedírsela al equipo). Sin la clave la app compila igual; solo no funcionan la cuenta, el respaldo ni la IA.
3. Ejecutar la configuración `app` en un emulador o un teléfono con Android 8.0 (API 26) o superior.

Desde la terminal: `./gradlew assembleDebug` compila y `./gradlew test` corre los tests.

## Estado del proyecto

Hito 1 (versión local): la app funciona completa sin conexión, con los datos en memoria.

**Hecho**

- Diario, búsqueda ("No lo encontramos en tu diario"), detalle del vino con otras añadas, editar, eliminar con deshacer y "Lo volví a probar" (que arranca con lo elegido la última vez).
- Registrar (RF01): foto con cámara o galería (permiso explicado, alternativas si se rechaza, foto de hasta 500 KB y enderezada), fecha de la degustación, regla de "¿Es el mismo vino?" y aviso "Sumamos esta vez" con deshacer. Lo cargado sobrevive a que Android cierre la app.
- Lectura de la etiqueta (RF03): ML Kit en el teléfono, sin conexión; completa solo los campos vacíos, marcados como sugeridos y editables.
- Mis gustos (RF04, parte local): estadísticas calculadas en el teléfono y estado "te faltan N degustaciones".
- Cada pantalla se probó en un teléfono real, con fuente al 130 %, modo oscuro, ancho de 320 dp y en modo avión.

**Pendiente, en orden** (ver la sección 9 de los requisitos en `docs/preentrega.md`)

1. Persistencia con Room: hoy `VinoRepositoryEnMemoria` pierde los datos al cerrar la app. Las fotos guardadas que ningún vino usa (por ejemplo si Android mata la app a mitad de un registro) deberían limpiarse al arrancar.
2. Supabase (migraciones SQL, RLS, Storage, Auth), `SyncWorker` y observador de conectividad.
3. Edge Function `analizar-gustos` y las pantallas del resumen con IA; la cuenta y los Ajustes siguen siendo pantallas provisorias.
4. Accesibilidad con TalkBack real (hoy se revisaron etiquetas y tamaños, no cómo suena), más tests y README final.

**Para validar con personas**

- Precisión de la lectura de etiquetas (el criterio de la preentrega es año y cepa correctos en al menos 7 de 10 y nombre útil en al menos 5). Primera medición con 7 fotos reales sacadas con el celular, cuatro de ellas con la botella acostada: año correcto en 6 de 7; año y cepa correctos a la vez en 4 de 7 (57 %); nombre útil en 3 o 4 de 7. Con esta muestra no se llega al criterio. Fallan sobre todo la cepa cuando está en letra chica o dorada sobre fondo oscuro, y el nombre cuando la tipografía es decorativa o curva. La muestra es chica: conviene repetirla con 10 etiquetas y, si se confirma, evaluar el plan B de la preentrega (modelo con visión por la Edge Function) o mejorar las reglas de `InterpretarTextoEtiqueta`.
- Los umbrales de `CalcularEstadisticasGustos` pueden dejar la lista de gustos vacía con pocas degustaciones parecidas; conviene ajustarlos con datos reales.

## Arquitectura

MVVM con patrón Repository y flujo unidireccional de datos, en tres capas: `ui` (screens, viewmodels, components, theme, navegacion), `domain` (model, repository, usecase) y `data`. Las pantallas solo dibujan el estado que expone el ViewModel; nunca acceden a datos.

**Casos de uso (`domain/usecase`):** son una decisión propia, no figuran en lo visto en clase. Cada uno concentra una regla de negocio (detectar si un vino ya está en el diario, calcular estadísticas, interpretar el texto de una etiqueta) para que sea pura, testeable sin Android y reutilizable. El ViewModel los llama y ellos usan el Repository. Se prueban con tests unitarios.

## Documentación

- [Preentrega de análisis y diseño](docs/preentrega.md): incluye la validación de supuestos, la comparación con alternativas y la estimación de costos
- [Diagramas (draw.io)](docs/diagramas/sorbo.drawio): flujo de pantallas, arquitectura, topología y modelo de datos
- [Mockups de pantallas](docs/mockups/index.html): todas las pantallas y estados, base del diseño en [Figma](https://www.figma.com/design/NM0utJz4cc9JBflTfu1ep9/Sorbo-App)

## Equipo

| Integrante | Rol |
|---|---|
| Alejo Garcia Bosio | Arquitectura y documentación |
| Iñaki Courtinade | Integración, persistencia y sensores |
| Nino Bizzotto | UI, UX y testing |
