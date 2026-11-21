// Capa de presentación · componente reutilizable sin estado: pasa cada error de cuenta a su mensaje en lenguaje simple.
package com.sorbo.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sorbo.app.R
import com.sorbo.app.domain.repository.MotivoCuenta
import com.sorbo.app.domain.util.LARGO_MINIMO_CLAVE
import com.sorbo.app.ui.viewmodels.ErrorDeCuenta

/** El texto de cada error. Los dos `when` son exhaustivos: si se agrega un motivo nuevo, el compilador obliga a escribir su mensaje. */
@Composable
fun ErrorDeCuenta.mensaje(): String = when (this) {
    ErrorDeCuenta.EmailInvalido -> stringResource(R.string.cuenta_error_email_invalido)
    ErrorDeCuenta.ClaveVacia -> stringResource(R.string.cuenta_error_clave_vacia)
    ErrorDeCuenta.ClaveCorta -> stringResource(R.string.cuenta_error_clave_corta, LARGO_MINIMO_CLAVE)
    ErrorDeCuenta.ClavesDistintas -> stringResource(R.string.cuenta_error_claves_distintas)
    ErrorDeCuenta.TerminosSinAceptar -> stringResource(R.string.cuenta_error_terminos)
    is ErrorDeCuenta.Servidor -> when (motivo) {
        MotivoCuenta.CREDENCIALES_INCORRECTAS -> stringResource(R.string.cuenta_error_credenciales)
        MotivoCuenta.EMAIL_YA_REGISTRADO -> stringResource(R.string.cuenta_error_email_registrado)
        MotivoCuenta.EMAIL_INVALIDO -> stringResource(R.string.cuenta_error_email_invalido)
        MotivoCuenta.CLAVE_DEBIL -> stringResource(R.string.cuenta_error_clave_debil, LARGO_MINIMO_CLAVE)
        MotivoCuenta.SIN_CONEXION -> stringResource(R.string.cuenta_error_sin_conexion)
        MotivoCuenta.SIN_SESION -> stringResource(R.string.cuenta_error_sin_sesion)
        MotivoCuenta.ERROR_DESCONOCIDO -> stringResource(R.string.cuenta_error_desconocido)
    }
}

/** Los errores del servidor van en un aviso arriba; los de validación van pegados al campo que hay que corregir. */
val ErrorDeCuenta.esDelServidor: Boolean get() = this is ErrorDeCuenta.Servidor

val ErrorDeCuenta.esDelEmail: Boolean
    get() = this == ErrorDeCuenta.EmailInvalido ||
        (this is ErrorDeCuenta.Servidor && (motivo == MotivoCuenta.EMAIL_YA_REGISTRADO || motivo == MotivoCuenta.EMAIL_INVALIDO))

val ErrorDeCuenta.esDeLaClave: Boolean
    get() = this == ErrorDeCuenta.ClaveVacia || this == ErrorDeCuenta.ClaveCorta ||
        (this is ErrorDeCuenta.Servidor && (motivo == MotivoCuenta.CREDENCIALES_INCORRECTAS || motivo == MotivoCuenta.CLAVE_DEBIL))

val ErrorDeCuenta.esDeRepetirClave: Boolean get() = this == ErrorDeCuenta.ClavesDistintas
