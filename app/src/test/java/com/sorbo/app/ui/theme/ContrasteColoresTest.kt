package com.sorbo.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test

/** RNF03: el texto tiene que tener un contraste mínimo de 4.5:1 (WCAG AA) sobre su fondo. */
class ContrasteColoresTest {

    private fun contraste(a: Color, b: Color): Double {
        val claro = maxOf(a.luminance(), b.luminance()).toDouble()
        val oscuro = minOf(a.luminance(), b.luminance()).toDouble()
        return (claro + 0.05) / (oscuro + 0.05)
    }

    private fun parejas(c: ColorScheme) = mapOf(
        "onPrimary sobre primary" to (c.onPrimary to c.primary),
        "onPrimaryContainer sobre primaryContainer" to (c.onPrimaryContainer to c.primaryContainer),
        "onSecondaryContainer sobre secondaryContainer" to (c.onSecondaryContainer to c.secondaryContainer),
        "onTertiaryContainer sobre tertiaryContainer" to (c.onTertiaryContainer to c.tertiaryContainer),
        "onError sobre error" to (c.onError to c.error),
        "onErrorContainer sobre errorContainer" to (c.onErrorContainer to c.errorContainer),
        "onSurface sobre surface" to (c.onSurface to c.surface),
        "onSurfaceVariant sobre surface" to (c.onSurfaceVariant to c.surface),
        "onSurface sobre surfaceContainer" to (c.onSurface to c.surfaceContainer),
        "onSurface sobre surfaceContainerHighest" to (c.onSurface to c.surfaceContainerHighest),
        "onSurfaceVariant sobre surfaceContainerHighest" to (c.onSurfaceVariant to c.surfaceContainerHighest),
        "primary sobre surface" to (c.primary to c.surface),
        "tertiary sobre surface" to (c.tertiary to c.surface),
        "inverseOnSurface sobre inverseSurface" to (c.inverseOnSurface to c.inverseSurface),
    )

    private fun verificar(nombre: String, c: ColorScheme) {
        val fallas = parejas(c).filter { (_, par) -> contraste(par.first, par.second) < 4.5 }
            .map { (k, par) -> "$nombre: $k = ${"%.2f".format(contraste(par.first, par.second))}" }
        assertTrue("Contraste menor a 4.5:1 -> $fallas", fallas.isEmpty())
    }

    @Test
    fun temaClaroCumpleContrasteAA() = verificar("claro", EsquemaClaro)

    @Test
    fun temaOscuroCumpleContrasteAA() = verificar("oscuro", EsquemaOscuro)
}
