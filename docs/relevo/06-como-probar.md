# Cómo probar

*Un cambio no está terminado hasta que se probó en un teléfono. Esta guía junta los comandos y las listas de chequeo que usamos.*

## 1. Antes de cada commit grande y antes de mergear

```bash
./gradlew assembleDebug testDebugUnitTest
```

Tiene que terminar en `BUILD SUCCESSFUL`. Al día de hoy son **233 tests** y todos pasan. Si algo falla, no se mergea. Si un test que no tocaste falla, avisá: puede ser un conflicto con el trabajo de otro.

Si Gradle falla con un mensaje que solo muestra un número de versión, es el JDK: en *Settings → Build → Gradle → Gradle JDK* elegí un JDK 17 o 21.

## 2. Probar en el teléfono

1. Teléfono con *Depuración por USB* activada, conectado y desbloqueado. Comprobar con `adb devices` (tiene que aparecer como `device`).
2. Instalar y abrir:
   ```bash
   ./gradlew installDebug
   adb shell am start -n com.sorbo.app/.MainActivity
   ```
3. Capturar la pantalla (guardala **fuera del repo**):
   ```bash
   adb exec-out screencap -p > ~/captura.png
   ```
4. Ver errores en vivo: `adb logcat | grep -i sorbo` (o el panel Logcat de Android Studio).

`adb` está en `~/Library/Android/sdk/platform-tools` (Mac) o `%LOCALAPPDATA%\Android\Sdk\platform-tools` (Windows). Si no lo encuentra, agregalo al `PATH`.

**Dejar el teléfono despierto:** la lectura de etiquetas es hasta 8 veces más lenta con la pantalla apagada, y los comandos no funcionan con el bloqueo puesto. Ajustes → Pantalla → Tiempo de espera, al máximo mientras probás.

### Tocar sin adivinar coordenadas

`scripts/tocar.sh` busca un texto en pantalla y lo toca:

```bash
scripts/tocar.sh --lista        # muestra los textos visibles con sus coordenadas
scripts/tocar.sh "Guardar"      # toca el elemento que contiene ese texto
```

Funciona con textos y descripciones de accesibilidad. Es solo una ayuda; todo se puede probar a mano.

### Trampas conocidas al probar

- `adb shell input keyevent KEYCODE_BACK` con el teclado cerrado **sale de la pantalla**. Mirá primero si hay teclado.
- Una captura con el teléfono bloqueado es una captura del bloqueo, no de la app.
- Matar la app (`adb shell am force-stop com.sorbo.app`) borra los datos mientras sean en memoria; con Room (I1) ya no.
- Para probar que la app sobrevive a que Android la cierre: activá *Opciones de desarrollador → No mantener actividades*, o con la app en segundo plano: `adb shell am kill com.sorbo.app`, y volvé a abrirla desde recientes. Tiene que reaparecer con lo escrito.

## 3. Qué probar en cada pantalla (lista de chequeo)

Marcá cada punto con el teléfono en la mano. Si algo no aplica, no lo marques como hecho: escribilo en el commit o en la rama.

**Funcionamiento**
- [ ] El camino feliz de principio a fin.
- [ ] Cada error posible muestra su mensaje y una salida.
- [ ] Volver atrás desde cada punto no deja la pantalla rota ni pierde datos sin avisar.
- [ ] Rotar el teléfono: la app está bloqueada en vertical y no debería girar.
- [ ] Sin conexión (modo avión): la pantalla sigue andando o explica qué falta.
- [ ] Android cierra la app en segundo plano y la reabrís: lo escrito sigue ahí (formularios).

**Accesibilidad**
- [ ] Objetivos táctiles de al menos 48 dp (se ven con *Opciones de desarrollador → Mostrar límites de diseño*).
- [ ] Fuente al máximo: *Ajustes → Pantalla → Tamaño de fuente*. Nada recortado ni solapado.
- [ ] Tema oscuro: *Ajustes → Pantalla → Modo oscuro*.
- [ ] Pantalla angosta (320 dp): `adb shell wm density 450` la achica; **al terminar, `adb shell wm density reset`** (verificá con `adb shell wm density`).
- [ ] TalkBack: *Ajustes → Accesibilidad → TalkBack*. Recorrer deslizando a la derecha: cada elemento se anuncia con sentido, el orden es lógico, los botones dicen qué hacen y los títulos se anuncian como encabezado.
- [ ] Con el teclado abierto, el botón principal sigue a la vista.

**Textos**
- [ ] Voseo y tono de la app. Sin textos en inglés ni "Lorem".
- [ ] Una búsqueda sin resultados dice "No lo encontramos en tu diario" (nunca "no lo probaste").

**Datos**
- [ ] Lo que se guarda aparece en el diario, el detalle y las estadísticas.
- [ ] Eliminar y deshacer vuelve todo a su lugar, incluidas las degustaciones.

## 4. Probar la lectura de etiquetas

Pendiente del equipo (criterio de la preentrega: **año y cepa correctos en al menos 7 de cada 10 etiquetas y nombre útil en al menos 5 de 10**).

1. Conseguí 10 botellas o etiquetas distintas (no las 7 que ya se usaron para ajustar las reglas).
2. En Registrar → *Sacar foto*, con buena luz y la etiqueta de frente. Probá también con botellas acostadas.
3. Anotá, por etiqueta: qué dice la etiqueta de verdad y qué sugirió la app (nombre, bodega, cepa, año), y cuánto tardó.
4. Contá aciertos y agregá el resultado, con las limitaciones, a `docs/evaluacion-lectura-etiquetas.md`.
5. Si una etiqueta se lee mal por una razón que una regla simple puede arreglar, sumá la etiqueta como caso de prueba en `InterpretarEtiquetasRealesTest` **y** la regla.

## 5. Escribir buenos tests

- Un test comprueba **un comportamiento** y se llama por lo que pasa (`unVinoEliminadoNoApareceEnElDiario`).
- Los tests de ViewModel usan las versiones simuladas y un despachador de prueba (ver `DetalleViewModelTest`).
- **Verificá que el test sirve:** rompé a propósito la lógica que prueba y mirá que el test falle. Después deshacé el cambio.
- Los tests de Room y de WorkManager necesitan un teléfono o emulador (`androidTest`, que todavía no existe: crear la carpeta con su configuración es parte de I1).
- Cada defecto que se encuentra deja un test que lo hubiera detectado.

## 6. Si algo no anda

| Síntoma | Probá |
|---|---|
| `adb devices` no lista el teléfono | Cable de datos, aceptar el cartel de depuración en el teléfono, `adb kill-server && adb start-server` |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | Desinstalar la app del teléfono y volver a instalar |
| La app se cierra al abrirla | `adb logcat -b crash` |
| Gradle no resuelve dependencias | Revisar conexión y *File → Sync Project with Gradle Files* |
| `SUPABASE_ANON_KEY` vacío | Copiar `local.properties.example` a `local.properties` y pedirle la clave a Alejo |
