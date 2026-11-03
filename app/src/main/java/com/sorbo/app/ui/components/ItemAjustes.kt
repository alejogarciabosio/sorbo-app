package com.sorbo.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.sorbo.app.R

/** Cómo termina la fila de ajustes: sin nada, con flecha o con un interruptor. */
sealed interface FinalItemAjustes {
    data object Ninguno : FinalItemAjustes
    data object Flecha : FinalItemAjustes
    data class Interruptor(val activo: Boolean, val onCambio: (Boolean) -> Unit) : FinalItemAjustes
}

@Composable
fun ItemAjustes(
    titulo: String,
    @DrawableRes icono: Int,
    modifier: Modifier = Modifier,
    subtitulo: String? = null,
    final: FinalItemAjustes = FinalItemAjustes.Ninguno,
    onClick: (() -> Unit)? = null,
) {
    ListItem(
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier,
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
        headlineContent = { Text(titulo, style = MaterialTheme.typography.bodyLarge) },
        supportingContent = subtitulo?.let { { Text(it, style = MaterialTheme.typography.bodyMedium) } },
        leadingContent = { Icon(painterResource(icono), contentDescription = null, modifier = Modifier.size(24.dp)) },
        trailingContent = when (final) {
            FinalItemAjustes.Ninguno -> null
            FinalItemAjustes.Flecha -> {
                { Icon(painterResource(R.drawable.ic_flecha_derecha), contentDescription = null) }
            }
            is FinalItemAjustes.Interruptor -> {
                { Switch(checked = final.activo, onCheckedChange = final.onCambio) }
            }
        },
    )
}
