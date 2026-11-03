package com.sorbo.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Colores del sistema de diseño (variables de Figma). Los valores que Figma no define
// (por ejemplo los "on" de secondary y tertiary) se derivaron del esquema de Material 3.

val EsquemaClaro: ColorScheme = lightColorScheme(
    primary = Color(0xFF7D2A42),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFD9E0),
    onPrimaryContainer = Color(0xFF3F0018),
    secondary = Color(0xFF74565E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFD9E1),
    onSecondaryContainer = Color(0xFF2B151B),
    tertiary = Color(0xFF7B5733),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDCBE),
    onTertiaryContainer = Color(0xFF2C1600),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFFF8F7),
    onBackground = Color(0xFF22191B),
    surface = Color(0xFFFFF8F7),
    onSurface = Color(0xFF22191B),
    surfaceVariant = Color(0xFFF0DEE0),
    onSurfaceVariant = Color(0xFF524346),
    outline = Color(0xFF847375),
    outlineVariant = Color(0xFFD6C2C4),
    inverseSurface = Color(0xFF382E30),
    inverseOnSurface = Color(0xFFFEEDEF),
    inversePrimary = Color(0xFFFFB1C2),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFF0F2),
    surfaceContainer = Color(0xFFFCEAEC),
    surfaceContainerHigh = Color(0xFFF6E4E6),
    surfaceContainerHighest = Color(0xFFF0DEE0),
)

val EsquemaOscuro: ColorScheme = darkColorScheme(
    primary = Color(0xFFFFB1C2),
    onPrimary = Color(0xFF5E1130),
    primaryContainer = Color(0xFF7D2A42),
    onPrimaryContainer = Color(0xFFFFD9E0),
    secondary = Color(0xFFE3BDC6),
    onSecondary = Color(0xFF422932),
    secondaryContainer = Color(0xFF5B3F46),
    onSecondaryContainer = Color(0xFFFFD9E1),
    tertiary = Color(0xFFEEBE91),
    onTertiary = Color(0xFF462A0A),
    tertiaryContainer = Color(0xFF613F1E),
    onTertiaryContainer = Color(0xFFFFDCBE),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF191113),
    onBackground = Color(0xFFF0DEE0),
    surface = Color(0xFF191113),
    onSurface = Color(0xFFF0DEE0),
    surfaceVariant = Color(0xFF3C3234),
    onSurfaceVariant = Color(0xFFD6C2C4),
    outline = Color(0xFF9F8C8F),
    outlineVariant = Color(0xFF524346),
    inverseSurface = Color(0xFFF0DEE0),
    inverseOnSurface = Color(0xFF382E30),
    inversePrimary = Color(0xFF7D2A42),
    surfaceContainerLowest = Color(0xFF140C0E),
    surfaceContainerLow = Color(0xFF22191B),
    surfaceContainer = Color(0xFF261D1F),
    surfaceContainerHigh = Color(0xFF312829),
    surfaceContainerHighest = Color(0xFF3C3234),
)
