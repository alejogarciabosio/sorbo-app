// Capa de presentación · componente reutilizable sin estado (state hoisting): recibe datos y eventos por parámetro.
package com.sorbo.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sorbo.app.R
import com.sorbo.app.domain.usecase.DetalleVino
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * "¿Es el mismo vino?": se muestra cuando lo que se está cargando se parece a un vino del diario.
 * Cerrarla sin elegir equivale a "Volver a editar".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaMismoVino(
    parecido: DetalleVino,
    onEsEsteVino: () -> Unit,
    onEsOtroVino: () -> Unit,
    onVolverAEditar: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onVolverAEditar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp).padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(56.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painterResource(R.drawable.ic_vino),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            Text(stringResource(R.string.registro_mismo_vino_titulo), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Text(
                stringResource(R.string.registro_mismo_vino_mensaje),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            TarjetaDelVinoParecido(parecido)
            Button(onClick = onEsEsteVino, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                Text(stringResource(R.string.registro_mismo_vino_si))
            }
            FilledTonalButton(onClick = onEsOtroVino, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                Text(stringResource(R.string.registro_mismo_vino_no))
            }
            TextButton(onClick = onVolverAEditar, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.registro_volver_a_editar))
            }
        }
    }
}

@Composable
private fun TarjetaDelVinoParecido(parecido: DetalleVino) {
    val formato = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())
    val vino = parecido.vino
    val veces = pluralStringResource(R.plurals.registro_veces, parecido.degustaciones.size, parecido.degustaciones.size)
    val ultima = parecido.degustaciones.firstOrNull()?.fecha?.let { stringResource(R.string.diario_probado, it.format(formato)) }
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        ItemVino(
            nombre = vino.nombre,
            detalle = vino.detalleEnLista(),
            cuando = listOfNotNull(veces, ultima).joinToString(" · "),
            meGusto = parecido.degustaciones.firstOrNull()?.meGusto ?: true,
            iniciales = vino.inicialesDeEtiqueta(),
            tono = vino.tonoDeEtiqueta(),
            onClick = null,
        )
    }
}
