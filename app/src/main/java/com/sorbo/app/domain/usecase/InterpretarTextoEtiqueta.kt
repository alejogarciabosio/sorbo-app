// Capa de dominio · caso de uso: una regla de negocio pura y testeable. No estaba en la guía de clase; se justifica en el README.
package com.sorbo.app.domain.usecase

import com.sorbo.app.domain.model.BloqueTexto
import com.sorbo.app.domain.model.SugerenciaEtiqueta
import com.sorbo.app.domain.model.TextoEtiqueta
import com.sorbo.app.domain.util.distanciaEntre
import com.sorbo.app.domain.util.extraerAnio
import com.sorbo.app.domain.util.normalizar
import com.sorbo.app.domain.util.quitarAnio

/**
 * Convierte el texto crudo de una etiqueta en nombre, bodega, cepa y año con reglas simples:
 * - el año por su patrón numérico y la cepa por una lista de variedades, tolerando una letra mal leída ("MALBEG");
 * - la bodega por palabras clave;
 * - el nombre por el grupo de renglones grandes y contiguos que está cerca del año, la cepa o la zona, porque la
 *   etiqueta es lo que rodea a esos datos (el texto más grande de la foto puede ser el fondo).
 * Lo que la lectura derecha no encuentra se completa con las lecturas giradas, sin pisar lo que ya tiene.
 */
class InterpretarTextoEtiqueta {

    operator fun invoke(texto: TextoEtiqueta): SugerenciaEtiqueta {
        // La lectura derecha va primero: manda sobre las giradas.
        val lecturas = (listOf(texto.bloques) + texto.alternativas).map(::renglonesDe)

        val anio = lecturas.firstNotNullOfOrNull { lectura -> lectura.firstNotNullOfOrNull { extraerAnio(it.texto) } }
        val cepa = lecturas.firstNotNullOfOrNull(::buscarCepa)
        val bodega = lecturas.firstNotNullOfOrNull { lectura -> lectura.firstOrNull { esBodega(it.texto) }?.texto }
        val nombre = lecturas.firstNotNullOfOrNull { elegirNombre(it, cepa) }

        return SugerenciaEtiqueta(nombre = nombre, bodega = bodega, cepa = cepa, anio = anio)
    }

    private fun renglonesDe(bloques: List<BloqueTexto>): List<BloqueTexto> =
        bloques
            .flatMap { bloque -> bloque.texto.lines().map { BloqueTexto(it.trim(), bloque.altura) } }
            .filter { it.texto.isNotBlank() }

    // ---- Cepa -------------------------------------------------------------------------------------------------

    /** Primero la palabra exacta y, si no está, la más parecida: el OCR confunde una letra ("MALBEG", "MALEEC"). */
    private fun buscarCepa(renglones: List<BloqueTexto>): String? {
        val palabras = palabrasDe(renglones.joinToString(" ") { it.texto })
        return buscarCepa(palabras, tolerante = false) ?: buscarCepa(palabras, tolerante = true)
    }

    private fun buscarCepa(palabras: List<String>, tolerante: Boolean): String? =
        CEPAS.firstOrNull { cepa ->
            val partes = palabrasDe(cepa)
            (0..palabras.size - partes.size).any { desde ->
                partes.indices.all { i -> coincide(palabras[desde + i], partes[i], tolerante, cepa = partes) }
            }
        }

    /** Una palabra leída coincide con la de la cepa si es igual o, siendo tolerante, difiere en pocas letras. */
    private fun coincide(leida: String, deCepa: String, tolerante: Boolean, cepa: List<String>): Boolean {
        if (leida == deCepa) return true
        if (!tolerante) return false
        val tolerancia = when {
            cepa.size == 1 && deCepa.length <= 5 -> 0 // "Blend", "Syrah": cortas y sueltas, solo exactas
            deCepa.length < 5 -> 0
            deCepa.length < 9 -> 1
            else -> 2
        }
        return tolerancia > 0 && distanciaEntre(leida, deCepa) <= tolerancia
    }

    /** El renglón no dice nada más que la cepa y el año ("CABERNET :", "MALBEC 2023"). */
    private fun esSoloCepaOAnio(renglon: String, cepa: String?): Boolean {
        val palabras = palabrasDe(quitarAnio(renglon))
        if (palabras.isEmpty()) return true
        if (cepa == null) return false
        val partes = palabrasDe(cepa)
        return palabras.all { palabra -> partes.any { coincide(palabra, it, tolerante = true, cepa = partes) } }
    }

    // ---- Bodega -----------------------------------------------------------------------------------------------

    private fun esBodega(renglon: String): Boolean = palabrasDe(renglon).any { it in PALABRAS_DE_BODEGA }

    // ---- Nombre -----------------------------------------------------------------------------------------------

    private fun elegirNombre(renglones: List<BloqueTexto>, cepa: String?): String? {
        val elegibles = renglones.map { esNombrePosible(it.texto, cepa) }
        if (elegibles.none { it }) return null

        // La etiqueta es lo que rodea al año, a la cepa y a la zona: se prefieren los renglones cercanos a esas anclas.
        val anclas = renglones.indices.filter { esAncla(renglones[it].texto, cepa) }
        val cercanos = renglones.indices.filter { i -> elegibles[i] && anclas.any { kotlin.math.abs(it - i) <= RADIO_DE_ANCLA } }
        val candidatos = cercanos.ifEmpty { renglones.indices.filter { elegibles[it] } }
            .filter { letrasDe(renglones[it].texto) >= MINIMO_DE_LETRAS_PARA_EMPEZAR }
            .ifEmpty { return null }

        val mejorGrupo = candidatos
            .map { grupoAlrededorDe(it, renglones, elegibles) }
            .maxByOrNull { puntaje(it, renglones) }
            ?: return null

        val nombre = sinRepetidos(mejorGrupo.map { renglones[it].texto })
            .flatMap(::palabrasEnOriginal)
            .filter { it.length > 1 || it.first().isDigit() } // letras sueltas que el OCR agrega ("a DESIERTO")
            .joinToString(" ")
        return nombre.takeIf { it.isNotBlank() }
    }

    /** Los renglones contiguos con letra de tamaño parecido forman un solo nombre ("DESIERTO" + "PAMPA"). */
    private fun grupoAlrededorDe(centro: Int, renglones: List<BloqueTexto>, elegibles: List<Boolean>): List<Int> {
        fun parecido(a: Int, b: Int) = renglones[a].altura >= renglones[b].altura * PARECIDO_DE_ALTURA &&
            renglones[b].altura >= renglones[a].altura * PARECIDO_DE_ALTURA

        val grupo = ArrayDeque(listOf(centro))
        var izquierda = centro - 1
        while (izquierda >= 0 && elegibles[izquierda] && parecido(izquierda, grupo.first())) grupo.addFirst(izquierda--)
        var derecha = centro + 1
        while (derecha < renglones.size && elegibles[derecha] && parecido(derecha, grupo.last())) grupo.addLast(derecha++)
        return grupo.toList()
    }

    /** El OCR a veces lee dos veces la misma palabra ("DESIERTO" y "DEST ERTO"): se queda con la primera. */
    private fun sinRepetidos(renglones: List<String>): List<String> {
        val conservados = mutableListOf<String>()
        for (renglon in renglones) {
            val sinEspacios = normalizar(renglon).replace(" ", "")
            val repetido = conservados.any { previo ->
                val otro = normalizar(previo).replace(" ", "")
                minOf(otro.length, sinEspacios.length) >= LARGO_PARA_COMPARAR && distanciaEntre(otro, sinEspacios) <= 2
            }
            if (!repetido) conservados += renglon
        }
        return conservados
    }

    /** Un grupo de varios renglones grandes vale más que uno solo, pero muchos renglones chicos no le ganan a un título. */
    private fun puntaje(grupo: List<Int>, renglones: List<BloqueTexto>): Float {
        val mayor = grupo.maxOf { renglones[it].altura }
        return mayor * (1f + BONO_POR_RENGLON * (minOf(grupo.size, MAXIMO_DE_RENGLONES) - 1))
    }

    private fun esNombrePosible(renglon: String, cepa: String?): Boolean {
        if (esBodega(renglon) || esZona(renglon) || esSoloCepaOAnio(renglon, cepa)) return false
        val normalizado = normalizar(renglon)
        // Un renglón de 2 letras ("RO" en SOM-BRE-RO) solo vale como parte de un grupo: nunca arranca uno.
        if (letrasDe(renglon) < MINIMO_DE_LETRAS_PARA_SUMAR) return false
        val rodeado = " $normalizado "
        return RUIDO.none { rodeado.contains(" $it ") }
    }

    private fun esAncla(renglon: String, cepa: String?): Boolean =
        extraerAnio(renglon) != null || esZona(renglon) || (cepa != null && esSoloCepaOAnio(renglon, cepa))

    private fun esZona(renglon: String): Boolean {
        val normalizado = normalizar(renglon)
        return ZONAS.any { normalizado.contains(it) } || palabrasDe(renglon).any { it in PALABRAS_DE_ZONA }
    }

    // ---- Texto ------------------------------------------------------------------------------------------------

    private fun letrasDe(texto: String): Int = normalizar(texto).count { it.isLetter() }

    private fun palabrasDe(texto: String): List<String> = normalizar(texto).split(' ').filter { it.isNotBlank() }

    /** Las palabras del renglón tal como se leyeron (con sus mayúsculas), sin signos sueltos. */
    private fun palabrasEnOriginal(renglon: String): List<String> =
        renglon.split(' ').map { it.trim(':', ';', ',', '.', '·', '-', '–') }.filter { it.isNotBlank() }

    private companion object {
        const val RADIO_DE_ANCLA = 3
        const val PARECIDO_DE_ALTURA = 0.65f
        const val BONO_POR_RENGLON = 0.5f
        const val MAXIMO_DE_RENGLONES = 4
        const val MINIMO_DE_LETRAS_PARA_EMPEZAR = 3
        const val MINIMO_DE_LETRAS_PARA_SUMAR = 2
        const val LARGO_PARA_COMPARAR = 6

        // De la más específica a la más general, para que "Cabernet Sauvignon" gane sobre "Cabernet".
        val CEPAS = listOf(
            "Cabernet Sauvignon", "Cabernet Franc", "Sauvignon Blanc", "Pinot Noir", "Pinot Grigio", "Petit Verdot",
            "Malbec", "Merlot", "Syrah", "Shiraz", "Chardonnay", "Torrontés", "Bonarda", "Tempranillo", "Tannat",
            "Chenin", "Semillón", "Viognier", "Riesling", "Garnacha", "Sangiovese", "Carménère", "Blend",
        )

        val PALABRAS_DE_BODEGA = setOf(
            "bodega", "bodegas", "vinedos", "winery", "wines", "estate", "vineyards", "cellars", "chateau", "domaine", "finca",
        )

        // Zonas y países: acompañan al nombre en la etiqueta pero no son el nombre.
        val ZONAS = listOf("argentin", "mendoz", "patagonia", "valle de")
        val PALABRAS_DE_ZONA = setOf("uco", "lujan", "cuyo", "salta", "cafayate", "neuquen", "tupungato", "maipu")

        val RUIDO = listOf(
            "alc", "vol", "producto argentino", "industria argentina", "contiene sulfitos", "contenido neto",
            "gluten", "embotellado", "ml", "cl",
        )
    }
}
