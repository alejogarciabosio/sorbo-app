// Capa de dominio · utilidad: funciones puras de texto usadas por las reglas de negocio.
package com.sorbo.app.domain.util

/** Cantidad mínima de letras que hay que agregar, quitar o cambiar para pasar de [a] a [b] (distancia de Levenshtein). */
fun distanciaEntre(a: String, b: String): Int {
    if (a == b) return 0
    if (a.isEmpty()) return b.length
    if (b.isEmpty()) return a.length
    var anterior = IntArray(b.length + 1) { it }
    for (i in 1..a.length) {
        val actual = IntArray(b.length + 1)
        actual[0] = i
        for (j in 1..b.length) {
            val costo = if (a[i - 1] == b[j - 1]) 0 else 1
            actual[j] = minOf(actual[j - 1] + 1, anterior[j] + 1, anterior[j - 1] + costo)
        }
        anterior = actual
    }
    return anterior[b.length]
}
