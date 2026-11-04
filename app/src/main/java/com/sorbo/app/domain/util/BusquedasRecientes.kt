// Capa de dominio · utilidad: regla de las búsquedas recientes, compartida por la implementación real y la simulada.
package com.sorbo.app.domain.util

const val MAXIMO_BUSQUEDAS_RECIENTES = 5

/**
 * Agrega [nueva] al principio de la lista. Ignora el texto vacío, no repite una búsqueda ya guardada
 * (sin importar mayúsculas ni tildes: la repetida sube al principio) y conserva solo las últimas [maximo].
 */
fun agregarBusquedaReciente(actuales: List<String>, nueva: String, maximo: Int = MAXIMO_BUSQUEDAS_RECIENTES): List<String> {
    val texto = nueva.trim()
    if (texto.isEmpty()) return actuales
    val clave = normalizar(texto)
    return (listOf(texto) + actuales.filter { normalizar(it) != clave }).take(maximo)
}
