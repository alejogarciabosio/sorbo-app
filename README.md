# Sorbo

Diario personal de vinos para Android. Registrás cada vino que probás con una foto de la etiqueta y una opinión simple, y después, en la vinoteca, sabés al instante si ya lo probaste y qué te pareció, aunque no tengas señal.

TPO de Desarrollo de Aplicaciones I — UADE.

## Problema

Las personas que toman vino de forma ocasional no recuerdan qué vinos probaron ni si les gustaron. Al comprar eligen al azar y repiten malas experiencias. Las apps existentes se centran en la opinión de la comunidad, usan vocabulario técnico y dependen de conexión.

## Propuesta

- Registro rápido con foto y opinión simple (me gustó / no, dulce / seco, comida, compañía, ocasión), sin conexión.
- Búsqueda en tu historial: "¿ya lo probé?", sin conexión.
- Con conexión: autocompletado de los datos del vino desde la foto y análisis de tus gustos con IA.
- Respaldo en la nube para no perder el historial.

## Stack

Kotlin · Jetpack Compose · MVVM + Clean Architecture · Room · WorkManager · Supabase (Auth, Postgres, Storage, Edge Functions) · LLM con visión

## Documentación

- [Preentrega de análisis y diseño](docs/preentrega.md)
- [Diagramas (draw.io)](docs/diagramas/sorbo.drawio): flujo de pantallas, arquitectura, topología y modelo de datos
- [Mockups de pantallas](docs/mockups/index.html): todas las pantallas y estados, base del diseño en [Figma](https://www.figma.com/design/NM0utJz4cc9JBflTfu1ep9/Sorbo-App)

## Equipo

| Integrante | Rol |
|---|---|
| Alejo Garcia Bosio | Arquitectura y documentación |
| Iñaki Courtinade | Integración, persistencia y sensores |
| Nino Bizzotto | UI, UX y testing |
