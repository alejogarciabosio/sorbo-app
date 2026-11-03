// Capa de presentación · navegación: rutas y mapa de pantallas (Navigation Compose, una sola Activity).
package com.sorbo.app.ui.navegacion

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.sorbo.app.R

/** Un botón de la barra inferior: ícono y texto siempre juntos. */
enum class DestinoPrincipal(
    val ruta: String,
    @StringRes val etiqueta: Int,
    @DrawableRes val icono: Int,
) {
    Diario(Rutas.DIARIO, R.string.destino_diario, R.drawable.ic_vino),
    Gustos(Rutas.GUSTOS, R.string.destino_gustos, R.drawable.ic_ia),
    Ajustes(Rutas.AJUSTES, R.string.destino_ajustes, R.drawable.ic_ajustes),
}
