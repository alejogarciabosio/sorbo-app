// Capa de presentación · componente reutilizable sin estado (state hoisting): aviso del estado de la lectura de la etiqueta.
package com.sorbo.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.sorbo.app.R

/** Lo que el formulario le cuenta al usuario sobre la lectura de la etiqueta. */
enum class AvisoLectura { Leyendo, Sugerido, SinDatos, Fallo }

/** Se anuncia solo (región viva) para que quien usa TalkBack se entere de que terminó la lectura. */
@Composable
fun AvisoLecturaEtiqueta(
    aviso: AvisoLectura,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val avisoVivo = modifier.semantics { liveRegion = LiveRegionMode.Polite }
    when (aviso) {
        AvisoLectura.Leyendo -> Row(
            modifier = avisoVivo.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.tertiary,
            )
            Text(
                stringResource(R.string.lectura_leyendo),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        AvisoLectura.Sugerido -> Banner(
            tipo = TipoBanner.IA,
            mensaje = stringResource(R.string.lectura_sugerido),
            modifier = avisoVivo,
        )
        AvisoLectura.SinDatos -> Banner(
            tipo = TipoBanner.Info,
            mensaje = stringResource(R.string.lectura_sin_datos),
            modifier = avisoVivo,
            textoAccion = stringResource(R.string.lectura_reintentar),
            onAccion = onReintentar,
        )
        AvisoLectura.Fallo -> Banner(
            tipo = TipoBanner.Error,
            mensaje = stringResource(R.string.lectura_error),
            modifier = avisoVivo,
            textoAccion = stringResource(R.string.lectura_reintentar),
            onAccion = onReintentar,
        )
    }
}
