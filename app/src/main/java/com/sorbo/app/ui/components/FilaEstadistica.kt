package com.sorbo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** "Secos: 8 de 9" con una barra de progreso. [progreso] va de 0 a 1. */
@Composable
fun FilaEstadistica(
    etiqueta: String,
    valor: String,
    progreso: Float,
    modifier: Modifier = Modifier,
    positiva: Boolean = true,
) {
    val colores = MaterialTheme.colorScheme
    val relleno = if (positiva) colores.primary else colores.onSurfaceVariant
    val forma = RoundedCornerShape(4.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clearAndSetSemantics { contentDescription = "$etiqueta: $valor" },
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(etiqueta, style = MaterialTheme.typography.bodyMedium)
            Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(colores.surfaceContainerHighest, forma),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progreso.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(relleno, forma),
            )
        }
    }
}
