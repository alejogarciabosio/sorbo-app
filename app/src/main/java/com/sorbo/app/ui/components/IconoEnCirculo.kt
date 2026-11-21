// Capa de presentación · componente reutilizable sin estado: ícono grande dentro de un círculo, para las pantallas de aviso (F5, F7).
package com.sorbo.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

/** Es decorativo: el significado lo da el título que está debajo, así que TalkBack lo salta. */
@Composable
fun IconoEnCirculo(@DrawableRes icono: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(96.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(icono),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}
