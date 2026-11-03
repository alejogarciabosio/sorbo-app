// Capa de presentación · tema Material 3: colores, tipografías, formas y espaciados.
package com.sorbo.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sorbo.app.R

// Roboto Serif se usa solo para la marca ("Sorbo") y el título de "Tu estilo".
val RobotoSerif = FontFamily(Font(R.font.roboto_serif_semibold, FontWeight.SemiBold))

val EstiloMarcaDisplay = TextStyle(
    fontFamily = RobotoSerif,
    fontWeight = FontWeight.SemiBold,
    fontSize = 45.sp,
    lineHeight = 52.sp,
)

val EstiloMarcaTitulo = TextStyle(
    fontFamily = RobotoSerif,
    fontWeight = FontWeight.SemiBold,
    fontSize = 24.sp,
    lineHeight = 30.sp,
)

// Escala de texto de Figma. El resto de los estilos usa los valores por defecto de Material 3 (Roboto).
val TipografiaSorbo = Typography(
    headlineSmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
)
