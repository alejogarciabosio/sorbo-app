// Capa de dominio · utilidades: reglas puras para achicar las fotos de las etiquetas (sin Android, se prueban con tests simples).
package com.sorbo.app.domain.util

/** Peso máximo de una foto guardada: 500 KB (RNF10). */
const val TAMANO_MAXIMO_FOTO_BYTES = 500 * 1024

/** Lados máximos, de mayor a menor, que se prueban hasta que la foto entra en el peso máximo. */
val LADOS_MAXIMOS_FOTO_PX = listOf(1600, 1280, 1024, 800)

/** Calidades JPEG que se prueban, de mayor a menor. */
val CALIDADES_FOTO = listOf(90, 80, 70, 60, 50, 40)

/** Medidas de una foto en píxeles. */
data class Medidas(val ancho: Int, val alto: Int)

/** Achica la foto para que su lado mayor sea [ladoMaximo], sin deformarla y sin agrandarla nunca. */
fun medidasParaLadoMaximo(ancho: Int, alto: Int, ladoMaximo: Int): Medidas {
    val ladoMayor = maxOf(ancho, alto)
    if (ladoMayor <= ladoMaximo) return Medidas(ancho, alto)
    val factor = ladoMaximo.toDouble() / ladoMayor
    return Medidas(
        ancho = maxOf(1, Math.round(ancho * factor).toInt()),
        alto = maxOf(1, Math.round(alto * factor).toInt()),
    )
}

/**
 * Prueba las [calidades] de mayor a menor y devuelve la primera versión que pesa a lo sumo [maximoBytes].
 * Devuelve null si ninguna entra: ahí hay que achicar las medidas y volver a probar.
 */
fun comprimirHastaTamano(
    maximoBytes: Int = TAMANO_MAXIMO_FOTO_BYTES,
    calidades: List<Int> = CALIDADES_FOTO,
    comprimir: (calidad: Int) -> ByteArray,
): ByteArray? = calidades.firstNotNullOfOrNull { calidad ->
    comprimir(calidad).takeIf { it.size <= maximoBytes }
}
