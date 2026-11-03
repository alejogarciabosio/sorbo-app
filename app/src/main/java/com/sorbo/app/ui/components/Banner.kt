// Capa de presentación · componente reutilizable sin estado (state hoisting): recibe datos y eventos por parámetro.
package com.sorbo.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.sorbo.app.R

enum class TipoBanner(@DrawableRes val icono: Int) {
    SinConexion(R.drawable.ic_sin_conexion),
    IA(R.drawable.ic_ia),
    Error(R.drawable.ic_error),
    Info(R.drawable.ic_info),
}

/** Aviso dentro de una pantalla. Todo lo que sugiere la IA usa el tipo IA (color tertiary y ✦). */
@Composable
fun Banner(
    tipo: TipoBanner,
    mensaje: String,
    modifier: Modifier = Modifier,
    titulo: String? = null,
    textoAccion: String? = null,
    onAccion: () -> Unit = {},
) {
    val colores = MaterialTheme.colorScheme
    val (fondo, contenido) = when (tipo) {
        TipoBanner.IA -> colores.tertiaryContainer to colores.onTertiaryContainer
        TipoBanner.Error -> colores.errorContainer to colores.onErrorContainer
        TipoBanner.SinConexion, TipoBanner.Info -> colores.surfaceContainerHighest to colores.onSurface
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(fondo, MaterialTheme.shapes.medium)
            .padding(start = 12.dp, top = 12.dp, end = 16.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(painterResource(tipo.icono), contentDescription = null, modifier = Modifier.size(20.dp), tint = contenido)
        Column {
            if (titulo != null) {
                Text(titulo, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = contenido)
            }
            Text(mensaje, style = MaterialTheme.typography.bodyMedium, color = contenido)
            if (textoAccion != null) {
                TextButton(onClick = onAccion) { Text(textoAccion, color = contenido) }
            }
        }
    }
}
