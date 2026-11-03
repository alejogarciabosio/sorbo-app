// Capa de presentación · componente reutilizable sin estado (state hoisting): recibe datos y eventos por parámetro.
package com.sorbo.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Pantalla vacía o de error: ícono, título, mensaje y una acción opcional. */
@Composable
fun EstadoVacio(
    @DrawableRes icono: Int,
    titulo: String,
    mensaje: String,
    modifier: Modifier = Modifier,
    esError: Boolean = false,
    textoAccion: String? = null,
    onAccion: () -> Unit = {},
) {
    val colores = MaterialTheme.colorScheme
    val fondo = if (esError) colores.errorContainer else colores.primaryContainer
    val contenido = if (esError) colores.onErrorContainer else colores.onPrimaryContainer
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size(96.dp).background(fondo, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(icono), contentDescription = null, modifier = Modifier.size(44.dp), tint = contenido)
        }
        Text(
            titulo,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            mensaje,
            style = MaterialTheme.typography.bodyMedium,
            color = colores.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (textoAccion != null) {
            Button(onClick = onAccion, modifier = Modifier.padding(top = 24.dp)) { Text(textoAccion) }
        }
    }
}
