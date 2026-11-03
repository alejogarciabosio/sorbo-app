// Capa de dominio · utilidad: funciones puras de texto usadas por las reglas de negocio.
package com.sorbo.app.domain.util

import java.text.Normalizer
import java.util.Locale

private val MARCAS_DE_TILDE = Regex("\\p{M}+")
private val NO_ALFANUMERICO = Regex("[^a-z0-9]+")
private val ANIO = Regex("\\b(19|20)\\d{2}\\b")

/** Minúsculas, sin tildes ni signos y con un solo espacio entre palabras. "Torrontés!" pasa a "torrontes". */
fun normalizar(texto: String): String {
    val sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replace(MARCAS_DE_TILDE, "")
    return sinTildes.lowercase(Locale.ROOT).replace(NO_ALFANUMERICO, " ").trim()
}

/** Busca un año de cuatro cifras (1900 a 2099) dentro del texto. */
fun extraerAnio(texto: String): Int? = ANIO.find(texto)?.value?.toInt()

fun quitarAnio(texto: String): String = texto.replace(ANIO, " ")

/** Como [normalizar] pero sin el año: "Rutini Malbec 2021" pasa a "rutini malbec". */
fun normalizarSinAnio(texto: String): String = normalizar(quitarAnio(texto))

/** Una consulta coincide si todas sus palabras aparecen en el texto de búsqueda (en cualquier orden). */
fun coincideConConsulta(textoBusqueda: String, consultaNormalizada: String): Boolean {
    val palabras = consultaNormalizada.split(' ').filter { it.isNotEmpty() }
    return palabras.isNotEmpty() && palabras.all { textoBusqueda.contains(it) }
}
