package com.sorbo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sorbo.app.R

/** "Me gustó / No me gustó": siempre con ícono y texto, nunca solo color. */
@Composable
fun Veredicto(meGusto: Boolean, modifier: Modifier = Modifier) {
    val colores = MaterialTheme.colorScheme
    val fondo = if (meGusto) colores.primaryContainer else colores.surfaceContainerHighest
    val contenido = if (meGusto) colores.onPrimaryContainer else colores.onSurfaceVariant
    val icono = if (meGusto) R.drawable.ic_me_gusto else R.drawable.ic_no_me_gusto
    val texto = if (meGusto) R.string.veredicto_me_gusto else R.string.veredicto_no_me_gusto

    Row(
        modifier = modifier
            .height(24.dp)
            .background(fondo, MaterialTheme.shapes.small)
            .padding(start = 6.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(painterResource(icono), contentDescription = null, modifier = Modifier.size(14.dp), tint = contenido)
        Text(stringResource(texto), style = MaterialTheme.typography.labelMedium, color = contenido)
    }
}
