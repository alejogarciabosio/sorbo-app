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

Hito 2 (en curso): los datos ya se guardan en el teléfono y la cuenta ya habla con Supabase. Todavía falta el respaldo.

**Hecho**

- Todo lo del Hito 1: diario, búsqueda, detalle, editar, eliminar con deshacer, "Lo volví a probar", registrar con foto y lectura de la etiqueta con ML Kit, Mis gustos con estadísticas locales.
- Persistencia con Room: los vinos sobreviven a cerrar la app (el diario arranca vacío). Los mismos tests de contrato corren contra la versión en memoria y contra Room.
- Preferencias con DataStore y conectividad real (la app sabe si hay internet).
- Cliente de Supabase Auth con Retrofit: crear cuenta, iniciar sesión, renovar la sesión y cerrar sesión (la interfaz de cuenta todavía es provisoria).
- Base en la nube: tablas `perfiles`, `vinos` y `degustaciones` con seguridad por fila y bucket privado `etiquetas` (`supabase/migrations`).
- Edge Functions desplegadas: `eliminar-cuenta` (probada de punta a punta) y `analizar-gustos` (valida sesión, pedido y cupo diario; falta conectarla a la IA y a la app) en `supabase/functions`.

**Pendiente, en orden** (ver `docs/relevo/02-plan-de-trabajo.md`)

1. Sincronización con WorkManager y restauración en un teléfono nuevo.
2. Pantallas reales de bienvenida, cuenta y Ajustes; estados sin conexión y error del Diario.
3. Conectar `analizar-gustos` a la IA y a la app, y las pantallas del resumen con IA.
4. Accesibilidad con TalkBack real, más tests y README final.

**Para validar con personas**

- Precisión de la lectura de etiquetas con botellas reales (criterio de la preentrega: año y cepa correctos en al menos 7 de 10).
- Los umbrales de `CalcularEstadisticasGustos` pueden dejar la lista de gustos vacía con pocas degustaciones parecidas; conviene ajustarlos con datos reales.

## Arquitectura

MVVM con patrón Repository y flujo unidireccional de datos, en tres capas: `ui` (screens, viewmodels, components, theme, navegacion), `domain` (model, repository, usecase) y `data`. Las pantallas solo dibujan el estado que expone el ViewModel; nunca acceden a datos.

**Casos de uso (`domain/usecase`):** son una decisión propia, no figuran en lo visto en clase. Cada uno concentra una regla de negocio (detectar si un vino ya está en el diario, calcular estadísticas, interpretar el texto de una etiqueta) para que sea pura, testeable sin Android y reutilizable. El ViewModel los llama y ellos usan el Repository. Se prueban con tests unitarios.

## Documentación

El trabajo en curso está en la rama `develop`; `main` solo se actualiza al cerrar cada hito.

- [Preentrega de análisis y diseño](docs/preentrega.md): incluye la validación de supuestos, la comparación con alternativas y la estimación de costos
- [Diagramas (draw.io)](docs/diagramas/sorbo.drawio): flujo de pantallas, arquitectura, topología y modelo de datos
- [Guía de relevo](docs/relevo/00-leer-primero.md): por dónde empezar, cómo está armado el código, plan de trabajo por persona, diseño de datos, sincronización, cuenta e IA, y cómo probar
- [Evaluación de la lectura de etiquetas](docs/evaluacion-lectura-etiquetas.md): qué se probó para leer la etiqueta (ML Kit, PaddleOCR, Tesseract, modelos de visión), los resultados y la decisión
- [Mockups de pantallas](docs/mockups/index.html): todas las pantallas y estados, base del diseño en [Figma](https://www.figma.com/design/NM0utJz4cc9JBflTfu1ep9/Sorbo-App)

## Equipo

| Integrante | Rol |
|---|---|
| Alejo Garcia Bosio | Arquitectura y documentación |
| Iñaki Courtinade | Integración, persistencia y sensores |
| Nino Bizzotto | UI, UX y testing |
