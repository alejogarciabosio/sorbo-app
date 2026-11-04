// Capa de presentación · frases de cada categoría de "Mis gustos" ("Vinos secos", "Con carne"), todas desde strings.xml.
package com.sorbo.app.ui.components

import androidx.annotation.StringRes
import com.sorbo.app.R
import com.sorbo.app.domain.model.Compania
import com.sorbo.app.domain.model.Comida
import com.sorbo.app.domain.model.Dulzor
import com.sorbo.app.domain.model.Ocasion
import com.sorbo.app.domain.usecase.CategoriaGusto

@StringRes
fun CategoriaGusto.frase(): Int = when (this) {
    is CategoriaGusto.PorDulzor -> when (dulzor) {
        Dulzor.DULCE -> R.string.gusto_dulzor_dulce
        Dulzor.SECO -> R.string.gusto_dulzor_seco
    }
    is CategoriaGusto.PorComida -> when (comida) {
        Comida.CARNE -> R.string.gusto_comida_carne
        Comida.PASTA -> R.string.gusto_comida_pasta
        Comida.PESCADO -> R.string.gusto_comida_pescado
        Comida.PIZZA -> R.string.gusto_comida_pizza
        Comida.QUESOS -> R.string.gusto_comida_quesos
        Comida.PICADA -> R.string.gusto_comida_picada
        Comida.POSTRE -> R.string.gusto_comida_postre
        Comida.SIN_COMIDA -> R.string.gusto_comida_sin_comida
        Comida.OTRA -> R.string.gusto_comida_otra
    }
    is CategoriaGusto.PorCompania -> when (compania) {
        Compania.SOLO -> R.string.gusto_compania_solo
        Compania.PAREJA -> R.string.gusto_compania_pareja
        Compania.AMIGOS -> R.string.gusto_compania_amigos
        Compania.FAMILIA -> R.string.gusto_compania_familia
        Compania.TRABAJO -> R.string.gusto_compania_trabajo
        Compania.OTRA -> R.string.gusto_compania_otra
    }
    is CategoriaGusto.PorOcasion -> when (ocasion) {
        Ocasion.CENA -> R.string.gusto_ocasion_cena
        Ocasion.ASADO -> R.string.gusto_ocasion_asado
        Ocasion.PREVIA -> R.string.gusto_ocasion_previa
        Ocasion.ALMUERZO -> R.string.gusto_ocasion_almuerzo
        Ocasion.CELEBRACION -> R.string.gusto_ocasion_celebracion
        Ocasion.OTRA -> R.string.gusto_ocasion_otra
    }
}
