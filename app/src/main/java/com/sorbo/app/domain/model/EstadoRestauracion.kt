// Capa de dominio · modelo: datos puros del negocio, sin anotaciones de Room ni de Compose.
package com.sorbo.app.domain.model

/** Por qué no se pudo terminar de traer el diario. La pantalla F8 lo traduce a un mensaje con "Reintentar". */
enum class MotivoRestauracion { SIN_CONEXION, SIN_SESION, ERROR_DESCONOCIDO }

/**
 * El progreso de traer el diario de la nube a este teléfono (pantalla F8). Contrato acordado con Alejo:
 * empieza en [SinIniciar], pasa por [Restaurando] (con cuántos vinos van de cuántos) y termina en
 * [Terminada], [SinRespaldo] o [Error]. Después de un error se puede volver a pedir: retoma sin duplicar.
 */
sealed interface EstadoRestauracion {
    data object SinIniciar : EstadoRestauracion

    /** La cuenta no tiene nada en la nube: no hay nada que traer. */
    data object SinRespaldo : EstadoRestauracion

    data class Restaurando(val bajados: Int, val total: Int) : EstadoRestauracion

    data class Terminada(val vinos: Int) : EstadoRestauracion

    data class Error(val motivo: MotivoRestauracion) : EstadoRestauracion
}
