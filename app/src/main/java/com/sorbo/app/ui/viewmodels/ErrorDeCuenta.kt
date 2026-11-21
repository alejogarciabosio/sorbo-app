// Capa de presentación · estado de pantalla: lo que puede salir mal en los formularios de cuenta, en un tipo cerrado.
package com.sorbo.app.ui.viewmodels

import com.sorbo.app.domain.repository.CuentaException
import com.sorbo.app.domain.repository.MotivoCuenta

/**
 * Por qué no avanzó un formulario de cuenta. Las validaciones locales se hacen antes de llamar a la red;
 * lo que responde el servidor llega como [Servidor] con su [MotivoCuenta].
 */
sealed interface ErrorDeCuenta {
    data object EmailInvalido : ErrorDeCuenta
    data object ClaveVacia : ErrorDeCuenta
    data object ClaveCorta : ErrorDeCuenta
    data object ClavesDistintas : ErrorDeCuenta
    data object TerminosSinAceptar : ErrorDeCuenta
    data class Servidor(val motivo: MotivoCuenta) : ErrorDeCuenta
}

/** Pasa la falla de una operación de cuenta a un [ErrorDeCuenta]; cualquier otra excepción es un error desconocido. */
internal fun Throwable.comoErrorDeCuenta(): ErrorDeCuenta =
    ErrorDeCuenta.Servidor((this as? CuentaException)?.motivo ?: MotivoCuenta.ERROR_DESCONOCIDO)
