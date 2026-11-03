// Capa de dominio · caso de uso: una regla de negocio pura y testeable. No estaba en la guía de clase; se justifica en el README.
package com.sorbo.app.domain.usecase

import com.sorbo.app.domain.model.BloqueTexto
import com.sorbo.app.domain.model.SugerenciaEtiqueta
import com.sorbo.app.domain.model.TextoEtiqueta
import com.sorbo.app.domain.util.extraerAnio
import com.sorbo.app.domain.util.normalizar

/**
 * Convierte el texto crudo de una etiqueta en nombre, bodega, cepa y año con reglas simples:
 * el año por su patrón numérico, la cepa por una lista de variedades, la bodega por palabras clave
 * y el nombre por el texto más grande que no sea ninguno de los anteriores.
 */
class InterpretarTextoEtiqueta {

    operator fun invoke(texto: TextoEtiqueta): SugerenciaEtiqueta {
        val renglones = texto.bloques
            .flatMap { bloque -> bloque.texto.lines().map { BloqueTexto(it.trim(), bloque.altura) } }
            .filter { it.texto.isNotBlank() }

        val anio = renglones.firstNotNullOfOrNull { extraerAnio(it.texto) }
        val cepa = buscarCepa(renglones)
        val renglonBodega = renglones.firstOrNull { esBodega(it.texto) }
        val nombre = renglones
            .filter { it !== renglonBodega && !esRuido(it.texto, cepa) }
            .maxByOrNull { it.altura }
            ?.texto

        return SugerenciaEtiqueta(nombre = nombre, bodega = renglonBodega?.texto, cepa = cepa, anio = anio)
    }

    private fun buscarCepa(renglones: List<BloqueTexto>): String? {
        val texto = " " + normalizar(renglones.joinToString(" ") { it.texto }) + " "
        return CEPAS.firstOrNull { texto.contains(" ${normalizar(it)} ") }
    }

    private fun esBodega(renglon: String): Boolean {
        val palabras = normalizar(renglon).split(' ')
        return palabras.any { it in PALABRAS_DE_BODEGA }
    }

    private fun esRuido(renglon: String, cepa: String?): Boolean {
        val normalizado = normalizar(renglon)
        if (normalizado.count { it.isLetter() } < 3) return true
        if (normalizado.all { it.isDigit() || it == ' ' }) return true
        if (cepa != null && normalizado == normalizar(cepa)) return true
        val rodeado = " $normalizado "
        return RUIDO.any { rodeado.contains(" $it ") }
    }

    private companion object {
        // De la más específica a la más general, para que "Cabernet Sauvignon" gane sobre "Cabernet".
        val CEPAS = listOf(
            "Cabernet Sauvignon", "Cabernet Franc", "Sauvignon Blanc", "Pinot Noir", "Pinot Grigio", "Petit Verdot",
            "Malbec", "Merlot", "Syrah", "Shiraz", "Chardonnay", "Torrontés", "Bonarda", "Tempranillo", "Tannat",
            "Chenin", "Semillón", "Viognier", "Riesling", "Garnacha", "Sangiovese", "Carménère", "Blend",
        )

        val PALABRAS_DE_BODEGA = setOf(
            "bodega", "bodegas", "vinedos", "winery", "estate", "vineyards", "cellars", "chateau", "domaine", "finca",
        )

        val RUIDO = listOf(
            "alc", "vol", "producto argentino", "industria argentina", "contiene sulfitos", "contenido neto",
            "gluten", "embotellado", "ml", "cl",
        )
    }
}
