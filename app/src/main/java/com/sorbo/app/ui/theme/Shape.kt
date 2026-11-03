// Capa de presentación · tema Material 3: colores, tipografías, formas y espaciados.
package com.sorbo.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Radios de Figma: chips 8, campos 12, tarjetas 16, diálogos y hojas 28.
val FormasSorbo = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
