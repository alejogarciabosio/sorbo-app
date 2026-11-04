package com.sorbo.app.data.memoria

import com.sorbo.app.domain.model.EstadoSesion
import com.sorbo.app.domain.repository.CuentaException
import com.sorbo.app.domain.repository.MotivoCuenta
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SesionRepositoryEnMemoriaTest {

    private val repositorio = SesionRepositoryEnMemoria()

    private fun <T> Result<T>.motivo() = (exceptionOrNull() as CuentaException).motivo

    @Test
    fun alCrearUnaCuentaQuedaConLaSesionIniciada() = runTest {
        val usuario = repositorio.crearCuenta("Lucia@Correo.com", "clave1234").getOrThrow()

        assertEquals("lucia@correo.com", usuario.email)
        assertEquals(EstadoSesion.ConSesion(usuario), repositorio.sesion.first())
    }

    @Test
    fun noDejaCrearDosCuentasConElMismoEmail() = runTest {
        repositorio.crearCuenta("lucia@correo.com", "clave1234")

        assertEquals(MotivoCuenta.EMAIL_YA_REGISTRADO, repositorio.crearCuenta("lucia@correo.com", "otraclave1").motivo())
    }

    @Test
    fun rechazaEmailInvalidoYClaveDebil() = runTest {
        assertEquals(MotivoCuenta.EMAIL_INVALIDO, repositorio.crearCuenta("lucia", "clave1234").motivo())
        assertEquals(MotivoCuenta.CLAVE_DEBIL, repositorio.crearCuenta("lucia@correo.com", "corta").motivo())
    }

    @Test
    fun iniciaSesionConLasCredencialesCorrectasYFallaConLasIncorrectas() = runTest {
        repositorio.crearCuenta("lucia@correo.com", "clave1234")
        repositorio.cerrarSesion()

        assertEquals(MotivoCuenta.CREDENCIALES_INCORRECTAS, repositorio.iniciarSesion("lucia@correo.com", "mala").motivo())
        assertEquals(EstadoSesion.SinSesion, repositorio.sesion.first())
        assertTrue(repositorio.iniciarSesion("lucia@correo.com", "clave1234").isSuccess)
        assertTrue(repositorio.sesion.first() is EstadoSesion.ConSesion)
    }

    @Test
    fun sinConexionLasOperacionesFallanConEseMotivo() = runTest {
        repositorio.hayConexion = false

        assertEquals(MotivoCuenta.SIN_CONEXION, repositorio.crearCuenta("lucia@correo.com", "clave1234").motivo())
        assertEquals(MotivoCuenta.SIN_CONEXION, repositorio.iniciarSesion("lucia@correo.com", "clave1234").motivo())
    }

    @Test
    fun cambiarLaContrasenaExigeSesionYUnaClaveValida() = runTest {
        assertEquals(MotivoCuenta.SIN_SESION, repositorio.cambiarContrasena("nuevaclave1").motivo())
        repositorio.crearCuenta("lucia@correo.com", "clave1234")

        assertEquals(MotivoCuenta.CLAVE_DEBIL, repositorio.cambiarContrasena("corta").motivo())
        assertTrue(repositorio.cambiarContrasena("nuevaclave1").isSuccess)
        repositorio.cerrarSesion()
        assertTrue(repositorio.iniciarSesion("lucia@correo.com", "nuevaclave1").isSuccess)
    }

    @Test
    fun alEliminarLaCuentaYaNoSePuedeIniciarSesion() = runTest {
        repositorio.crearCuenta("lucia@correo.com", "clave1234")

        assertTrue(repositorio.eliminarCuenta().isSuccess)

        assertEquals(EstadoSesion.SinSesion, repositorio.sesion.first())
        assertEquals(MotivoCuenta.CREDENCIALES_INCORRECTAS, repositorio.iniciarSesion("lucia@correo.com", "clave1234").motivo())
    }

    @Test
    fun recuperarLaContrasenaNoRevelaSiElEmailTieneCuenta() = runTest {
        assertTrue(repositorio.recuperarContrasena("nadie@correo.com").isSuccess)
        assertEquals(MotivoCuenta.EMAIL_INVALIDO, repositorio.recuperarContrasena("nadie").motivo())
    }
}
