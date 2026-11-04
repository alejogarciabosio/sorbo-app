// Capa de presentación · componente reutilizable sin estado (state hoisting): recibe datos y eventos por parámetro.
package com.sorbo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sorbo.app.R
import com.sorbo.app.ui.theme.EstiloMarcaTitulo
import java.io.File

/**
 * Parte de arriba del detalle: la foto de la etiqueta (o, sin foto, el color del vino con su inicial) y, encima,
 * los botones de volver, editar y eliminar.
 */
@Composable
fun CabeceraVino(
    inicial: String,
    tono: TonoEtiqueta,
    fotoPath: String?,
    onVolver: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth().height(240.dp)) {
        if (fotoPath != null) {
            AsyncImage(
                model = File(fotoPath),
                contentDescription = stringResource(R.string.foto_descripcion),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(tono.inicio, tono.fin))),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = inicial,
                    style = TextStyle(
                        fontFamily = EstiloMarcaTitulo.fontFamily,
                        fontWeight = EstiloMarcaTitulo.fontWeight,
                        fontSize = 72.sp,
                    ),
                    color = tono.texto,
                )
            }
        }
        // Un degradé oscuro arriba asegura que los botones se lean sobre cualquier foto.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
                .drawBehind { drawRect(Brush.verticalGradient(listOf(Color(0x99000000), Color.Transparent))) },
        )
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BotonSobreFoto(R.drawable.ic_atras, stringResource(R.string.accion_volver), onVolver)
            Box(modifier = Modifier.weight(1f))
            BotonSobreFoto(R.drawable.ic_editar, stringResource(R.string.accion_editar), onEditar)
            BotonSobreFoto(R.drawable.ic_eliminar, stringResource(R.string.accion_eliminar), onEliminar)
        }
    }
}

@Composable
private fun BotonSobreFoto(icono: Int, descripcion: String, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0x66000000)),
    ) {
        Icon(painterResource(icono), contentDescription = descripcion, tint = Color.White)
    }
}
