// Capa de presentación · componente reutilizable sin estado (state hoisting): recibe datos y eventos por parámetro.
package com.sorbo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sorbo.app.R
import java.io.File

/** La foto de la etiqueta en el formulario: miniatura, estado y las acciones para sacarla, elegirla o quitarla. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilaFoto(
    fotoPath: String?,
    procesando: Boolean,
    onSacarFoto: () -> Unit,
    onElegirDeGaleria: () -> Unit,
    onQuitar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            when {
                procesando -> CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                fotoPath != null -> AsyncImage(
                    model = File(fotoPath),
                    contentDescription = stringResource(R.string.foto_descripcion),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                else -> Icon(
                    painterResource(R.drawable.ic_camara),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.foto_titulo), style = MaterialTheme.typography.titleMedium)
            val estado = when {
                procesando -> R.string.foto_preparando
                fotoPath != null -> R.string.foto_lista
                else -> R.string.foto_sin_foto
            }
            Text(
                stringResource(estado),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onSacarFoto, enabled = !procesando) {
                    Icon(painterResource(R.drawable.ic_camara), contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.foto_sacar), modifier = Modifier.padding(start = 6.dp))
                }
                TextButton(onClick = onElegirDeGaleria, enabled = !procesando) {
                    Icon(painterResource(R.drawable.ic_galeria), contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.foto_galeria), modifier = Modifier.padding(start = 6.dp))
                }
                if (fotoPath != null && !procesando) {
                    TextButton(onClick = onQuitar) { Text(stringResource(R.string.foto_quitar)) }
                }
            }
        }
    }
}
