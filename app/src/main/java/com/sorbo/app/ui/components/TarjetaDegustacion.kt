// Capa de presentación · componente reutilizable sin estado (state hoisting): recibe datos y eventos por parámetro.
package com.sorbo.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sorbo.app.R

/** Una degustación dentro del detalle de un vino. */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun TarjetaDegustacion(
    fecha: String,
    meGusto: Boolean,
    modifier: Modifier = Modifier,
    comida: String? = null,
    compania: String? = null,
    ocasion: String? = null,
    dulzor: String? = null,
    nota: String? = null,
) {
    val colores = MaterialTheme.colorScheme
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colores.surfaceContainerLow),
        border = BorderStroke(1.dp, colores.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(fecha, style = MaterialTheme.typography.labelLarge)
                Veredicto(meGusto)
            }
            // Si no se cargó ningún dato no se dibuja la fila, para no dejar un hueco vacío en la tarjeta.
            if (comida != null || compania != null || ocasion != null || dulzor != null) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (comida != null) Dato(R.drawable.ic_comida, comida)
                    if (compania != null) Dato(R.drawable.ic_grupo, compania)
                    if (ocasion != null) Dato(R.drawable.ic_fecha, ocasion)
                    if (dulzor != null) Text(dulzor, style = MaterialTheme.typography.bodyMedium, color = colores.onSurfaceVariant)
                }
            }
            if (nota != null) {
                Text("“$nota”", style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
            }
        }
    }
}

@Composable
private fun Dato(@DrawableRes icono: Int, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(painterResource(icono), contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(texto, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Normal)
    }
}
