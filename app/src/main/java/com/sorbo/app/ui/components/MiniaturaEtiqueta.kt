// Capa de presentación · componente reutilizable sin estado (state hoisting): recibe datos y eventos por parámetro.
package com.sorbo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sorbo.app.ui.theme.EstiloMarcaTitulo

/** Colores de la miniatura cuando el vino todavía no tiene foto. */
enum class TonoEtiqueta(
    val inicio: Color,
    val fin: Color,
    val texto: Color,
    val lineaOscura: Boolean,
) {
    Malbec(Color(0xFF4A0F24), Color(0xFF8C2B4B), Color(0xEBFFFFFF), false),
    Dorado(Color(0xFFF1D58F), Color(0xFFB8862F), Color(0xFF3A2508), true),
    Claro(Color(0xFFE6E0C6), Color(0xFFA69B6B), Color(0xFF33301E), true),
    Azul(Color(0xFF1F2A44), Color(0xFF4B5B85), Color(0xEBFFFFFF), false),
    Rosado(Color(0xFFF6C4C7), Color(0xFFD66A7A), Color(0xFF4A1420), true),
    Verde(Color(0xFF2F4A3A), Color(0xFF6E8F6B), Color(0xEBFFFFFF), false),
    Marron(Color(0xFF3B2A20), Color(0xFF8A6A4F), Color(0xEBFFFFFF), false),
}

@Composable
fun MiniaturaEtiqueta(
    iniciales: String,
    tono: TonoEtiqueta,
    modifier: Modifier = Modifier,
    tamano: Dp = 56.dp,
) {
    val colorLinea = if (tono.lineaOscura) Color(0x40000000) else Color(0x73FFFFFF)
    Box(
        modifier = modifier
            .size(tamano)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(tono.inicio, tono.fin)))
            .drawBehind {
                val ancho = size.width * 0.64f
                val alto = 3.dp.toPx()
                drawRoundRect(
                    color = colorLinea,
                    topLeft = Offset(size.width * 0.18f, size.height * 0.86f - alto),
                    size = Size(ancho, alto),
                    cornerRadius = CornerRadius(alto / 2),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = iniciales,
            style = TextStyle(
                fontFamily = EstiloMarcaTitulo.fontFamily,
                fontWeight = EstiloMarcaTitulo.fontWeight,
                fontSize = (tamano.value * 0.32f).sp,
            ),
            color = tono.texto,
        )
    }
}
