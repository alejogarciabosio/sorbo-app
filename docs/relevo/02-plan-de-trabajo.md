# Plan de trabajo

*Qué hace cada uno, en qué orden y cuándo una tarea está terminada. Cada tarea tiene un código (I = Iñaki, N = Nino, A = Alejo) para poder nombrarla en commits, ramas y charlas.*

## Cómo leer este plan

- **Dependencia** = qué tiene que existir antes. Todo lo marcado "ninguna" se puede arrancar hoy.
- **Rama** = nombre sugerido. Una rama por tarea, chica, que se mergea seguido.
- **Terminada** = criterio concreto. Además de lo de cada tarea, siempre rige la definición de terminado de `00-leer-primero.md` (compila, tests en verde, probado en el teléfono, textos en `strings.xml`).
- Los contratos de `domain/repository` ya están definidos y no se cambian sin hablarlo (ver `00`). Eso es lo que permite trabajar en paralelo.

## Vista general

```
 Iñaki:  I1 Room ─▶ I2 Preferencias ─▶ I3 Conectividad ─▶ I4 Cliente Supabase ─▶ I5 Sincronización ─▶ I6 Restauración
 Nino:   N1 Bienvenida ─▶ N2 Estados Diario/Buscar ─▶ N3 Cuenta ─▶ N4 Ajustes ─▶ N5 Mis gustos con IA ─▶ N6 Accesibilidad
 Alejo:  A1 Base Supabase ─▶ A2 Auth ─▶ A3 Edge Function de gustos ─▶ (A4 lectura en la nube, opcional) ─▶ A5 Borrar cuenta
```

Los tres carriles son independientes hasta las **uniones**:

| Unión | Qué se junta | Cuándo |
|---|---|---|
| U1 | I4 necesita las tablas y la seguridad de A1 | Antes de probar contra la nube de verdad |
| U2 | N3 (cuenta real) necesita A2 + la implementación real de `SesionRepository` (I4) | Cuando I4 y A2 estén |
| U3 | N5 necesita A3 + la implementación real de `AnalisisGustosRepository` | Al final |

Hasta que se junten, **cada uno trabaja contra las versiones simuladas de `data/memoria`**.

---

## Iñaki — datos locales, red y sincronización

Detalle técnico: `03-diseno-datos-y-sincronizacion.md`.

| Cód. | Tarea | Dependencia | Rama | Terminada cuando |
|---|---|---|---|---|
| **I1** | **Room.** Entidades (vino, degustación), conversores, mappers, `VinoRepositoryRoom`. Reemplaza a `VinoRepositoryEnMemoria` en `AppContainer` | ninguna | `feature/room` | Los vinos sobreviven a cerrar la app; el diario, la búsqueda y las estadísticas funcionan igual que antes; los tests de contrato pasan contra **las dos** implementaciones; toda escritura queda `PENDIENTE` con `actualizadoEn` |
| **I2** | **Preferencias** con DataStore: `PreferenciasRepositoryDataStore` | ninguna | `feature/preferencias` | Bienvenida vista, aviso de IA, IA activada y búsquedas recientes sobreviven a cerrar la app; test de la lógica con la versión simulada ya existente |
| **I3** | **Conectividad real:** `ConectividadObserverAndroid`. Agregar `INTERNET` y `ACCESS_NETWORK_STATE` al manifiesto | ninguna | `feature/conectividad` | Al poner modo avión el flujo emite `false` y al sacarlo `true`; probado en el teléfono |
| **I4** | **Cliente de Supabase** con Retrofit + OkHttp: iniciar sesión, crear cuenta, recuperar contraseña, tablas y fotos. Implementa `SesionRepository` real | A1 y A2 para probar de punta a punta | `feature/supabase-cliente` | Se puede crear cuenta, iniciar sesión y cerrar sesión desde la app; la sesión persiste; ninguna clave en el repo |
| **I5** | **Sincronización:** caso de uso `SincronizarDiario`, `SyncWorker` con WorkManager | I1, I3, I4 | `feature/sincronizacion` | Un vino guardado sin conexión aparece en la nube al volver la red; si falla reintenta sin duplicar; la eliminación y la edición también se propagan; el usuario sin cuenta no sincroniza nada |
| **I6** | **Restauración** al iniciar sesión en un teléfono nuevo (F8) | I5 | `feature/restauracion` | Iniciar sesión en un teléfono limpio trae el diario y las fotos; no duplica si ya hay datos |

Orden recomendado: **I1** primero (es lo que más desbloquea y lo más grande), I2 e I3 en cualquier momento (son chicas, buenas para cortar un rato).

## Nino — pantallas, estados, cuenta y accesibilidad

Detalle: `04-diseno-cuenta-y-pantallas.md`.

| Cód. | Tarea | Dependencia | Rama | Terminada cuando |
|---|---|---|---|---|
| **N1** | **Bienvenida** (O1–O3): se muestra la primera vez y no vuelve; con y sin cuenta | ninguna (usa `PreferenciasRepository` simulada) | `feature/bienvenida` | Primera apertura muestra la bienvenida; "Empezar sin cuenta" y "Crear cuenta" llevan a donde corresponde; no reaparece |
| **N2** | **Estados que faltan:** Diario sin conexión y con error (A3, A4); **búsquedas recientes** en Buscar (B1) | ninguna | `feature/estados-diario-buscar` | Cada estado tiene su pantalla, su texto, su test de ViewModel y un botón que hace algo |
| **N3** | **Cuenta (F1–F8):** iniciar sesión, crear cuenta, recuperar y cambiar contraseña, cerrar sesión | ninguna (usa `SesionRepository` simulada) | `feature/cuenta` | Todos los errores de `MotivoCuenta` tienen mensaje claro; validación con `Credenciales`; con el repositorio simulado se puede recorrer todo el flujo |
| **N4** | **Ajustes (G1–G7)** reales; borrar `GaleriaComponentes` y `PantallaEnConstruccion` cuando ya no se usen | N3 | `feature/ajustes` | Cada fila de Ajustes lleva a una pantalla real; "Borrar mis datos" y "Eliminar cuenta" piden confirmación |
| **N5** | **Mis gustos con IA (E2–E7):** aviso de consentimiento, botón "Generar resumen", resultado, errores (sin conexión, límite, servicio caído), mínimo de 5 degustaciones | N2 | `feature/gustos-ia` | Todos los casos de `MotivoAnalisis` tienen su estado; el resumen se ve y se puede volver a generar; se prueba con la versión simulada |
| **N6** | **Accesibilidad y pulido** | N1–N5 | `feature/accesibilidad` | Se cumple la lista de `06-como-probar.md` en todas las pantallas |

N1, N2 y N3 son independientes entre sí: se pueden repartir con Iñaki si Nino quiere adelantar.

## Alejo — Supabase, IA en la nube e integración

Detalle: `03` (base y seguridad) y `05` (IA).

| Cód. | Tarea | Dependencia | Terminada cuando |
|---|---|---|---|
| **A1** | Migraciones SQL en `supabase/migrations`: tablas `perfiles`, `vinos`, `degustaciones`; seguridad por fila (RLS); bucket privado `etiquetas` | ninguna | Un usuario solo ve y modifica lo suyo (verificado con dos cuentas); las migraciones están en el repo |
| **A2** | Autenticación: email y contraseña, largo mínimo, recuperación con enlace, decisión sobre confirmar el email | A1 | Se puede crear cuenta, iniciar sesión y recuperar contraseña desde un cliente HTTP |
| **A3** | Edge Function `analizar-gustos` (Groq) con límite por usuario | A2 | Responde `resumen` y `sugerencias` con solo estadísticas; falla de forma controlada |
| **A4** | *(Opcional)* Edge Function `leer-etiqueta` y su integración en el registro | A3 | Ver `05`; solo si se decide hacerla |
| **A5** | Función para eliminar la cuenta y todos sus datos (requiere la clave de servicio, solo del lado servidor) | A2 | Borra usuario, filas y fotos |
| **A6** | Integración: revisar y mergear ramas, correr la suite, cuidar `develop`, preparar `main`, README final | continuo | `main` siempre compila y pasa los tests |

## Tareas compartidas del final

| Tarea | Quién |
|---|---|
| Medir ML Kit con 10 etiquetas reales (criterio: año y cepa en ≥ 7 de 10, nombre útil en ≥ 5 de 10). Anotar el resultado en `docs/evaluacion-lectura-etiquetas.md` | Iñaki o Nino, con botellas reales |
| TalkBack real en el teléfono (hoy solo se auditó el árbol de accesibilidad) | Nino |
| Revisar el README contra lo que realmente quedó | Alejo |
| Actualizar el estado del README cada vez que se cierra una tarea | Quien la cierra |

## Decisiones abiertas

Resolverlas antes de la tarea indicada. Mientras tanto, se usa la opción por defecto.

| # | Decisión | Opción por defecto | La necesita |
|---|---|---|---|
| 1 | ¿Los vinos de ejemplo (`DatosDeEjemplo`) siguen en la versión final? | **No**: solo para desarrollo. Con Room arranca vacío (la pantalla vacía ya está hecha) | I1 |
| 2 | Largo mínimo de contraseña | 8 (ya es `LARGO_MINIMO_CLAVE`) | A2, N3 |
| 3 | ¿Se exige confirmar el email al crear la cuenta? | No (más simple para la demostración) | A2, N3 |
| 4 | Esquema del enlace para recuperar contraseña (deep link) | `sorbo://recuperar` | A2, N3 |
| 5 | ¿Dónde se pide el consentimiento de la IA? | La primera vez que se toca "Generar resumen" | N5 |
| 6 | ¿Se hace la lectura de etiqueta en la nube (A4)? | No, hasta que lo anterior esté terminado | A4 |
| 7 | ¿Cuántas fotos se suben por lote al sincronizar? | De a una por vez, con reintento | I5 |

## Cómo evitar pisarse

1. Cada tarea toca **sus** carpetas (`data/local`, `data/remote`, `data/sync` → Iñaki; `ui/screens`, `ui/viewmodels` → Nino; `supabase/` → Alejo).
2. Los archivos compartidos (lista en `00`) se tocan con cambios **mínimos**, en commits propios y avisando antes. Los casos típicos: `AppContainer.kt` (cambiar una línea por la implementación real), `SorboNavHost.kt` (agregar una ruta), `strings.xml` (agregar textos dentro de la sección de tu pantalla).
3. Antes de empezar el día: `git fetch` y `git merge origin/develop` en tu rama.
4. Si hay que cambiar un contrato de `domain/repository`: se avisa en el grupo, se cambia en una rama aparte con su versión simulada y sus tests, y se mergea primero.
