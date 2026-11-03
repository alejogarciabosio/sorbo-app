// Capa de presentación · textos de las listas cerradas del dominio (dulzor, comida, compañía, ocasión), todos desde strings.xml.
package com.sorbo.app.ui.components

import androidx.annotation.StringRes
import com.sorbo.app.R
import com.sorbo.app.domain.model.Compania
import com.sorbo.app.domain.model.Comida
import com.sorbo.app.domain.model.Dulzor
import com.sorbo.app.domain.model.Ocasion

@StringRes
fun Dulzor.etiqueta(): Int = when (this) {
    Dulzor.DULCE -> R.string.dulzor_dulce
    Dulzor.SECO -> R.string.dulzor_seco
}

@StringRes
fun Comida.etiqueta(): Int = when (this) {
    Comida.CARNE -> R.string.comida_carne
    Comida.PASTA -> R.string.comida_pasta
    Comida.PESCADO -> R.string.comida_pescado
    Comida.PIZZA -> R.string.comida_pizza
    Comida.QUESOS -> R.string.comida_quesos
    Comida.PICADA -> R.string.comida_picada
    Comida.POSTRE -> R.string.comida_postre
    Comida.SIN_COMIDA -> R.string.comida_sin_comida
    Comida.OTRA -> R.string.opcion_otra
}

@StringRes
fun Compania.etiqueta(): Int = when (this) {
    Compania.SOLO -> R.string.compania_solo
    Compania.PAREJA -> R.string.compania_pareja
    Compania.AMIGOS -> R.string.compania_amigos
    Compania.FAMILIA -> R.string.compania_familia
    Compania.TRABAJO -> R.string.compania_trabajo
    Compania.OTRA -> R.string.opcion_otra
}

@StringRes
fun Ocasion.etiqueta(): Int = when (this) {
    Ocasion.CENA -> R.string.ocasion_cena
    Ocasion.ASADO -> R.string.ocasion_asado
    Ocasion.PREVIA -> R.string.ocasion_previa
    Ocasion.ALMUERZO -> R.string.ocasion_almuerzo
    Ocasion.CELEBRACION -> R.string.ocasion_celebracion
    Ocasion.OTRA -> R.string.opcion_otra
}
