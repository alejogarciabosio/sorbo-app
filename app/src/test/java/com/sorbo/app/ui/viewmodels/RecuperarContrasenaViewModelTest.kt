// Pruebas · ViewModel de Recuperar contraseña: pedir el enlace, ver "Revisá tu email" y reenviarlo.
package com.sorbo.app.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.sorbo.app.data.memoria.SesionRepositoryEnMemoria
import com.sorbo.app.domain.repository.MotivoCuenta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecuperarContrasenaViewModelTest {

    private val sesion = SesionRepositoryEnMemoria()

    @Before
    fun antes() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun despues() = Dispatchers.resetMain()

    private fun crear(handle: SavedStateHandle = SavedStateHandle()) = RecuperarContrasenaViewModel(sesion, handle)

    @Test
    fun conUnEmailValidPasaARevisaTuEmail() = runTest {
        val viewModel = crear()
        viewModel.alCambiarEmail("  ana@correo.com ")

        viewModel.alEnviarEnlace()

        assertEquals(EstadoRecuperarContrasena.Enviado("ana@correo.com"), viewModel.estado.value)
    }

    @Test
    fun unEmailInvalidoNoSeEnvia() = runTest {
        val viewModel = crear()
        viewModel.alCambiarEmail("ana")

        viewModel.alEnviarEnlace()

        val estado = viewModel.estado.value as EstadoRecuperarContrasena.Editando
        assertEquals(ErrorDeCuenta.EmailInvalido, estado.error)
    }

    @Test
    fun sinConexionSeAvisaYSeSigueEnLaMismaPantalla() = runTest {
        sesion.hayConexion = false
        val viewModel = crear()
        viewModel.alCambiarEmail("ana@correo.com")

        viewModel.alEnviarEnlace()

        val estado = viewModel.estado.value as EstadoRecuperarContrasena.Editando
        assertEquals(ErrorDeCuenta.Servidor(MotivoCuenta.SIN_CONEXION), estado.error)
        assertFalse(estado.enviando)
    }

    @Test
    fun sePuedeReenviarElEnlace() = runTest {
        val viewModel = crear()
        viewModel.alCambiarEmail("ana@correo.com")
        viewModel.alEnviarEnlace()

        viewModel.alReenviarEnlace()

        val estado = viewModel.estado.value as EstadoRecuperarContrasena.Enviado
        assertTrue(estado.reenviado)
        assertFalse(estado.reenviando)
        assertEquals(null, estado.error)
    }

    @Test
    fun siFallaElReenvioSeAvisaSinSalirDeLaPantalla() = runTest {
        val viewModel = crear()
        viewModel.alCambiarEmail("ana@correo.com")
        viewModel.alEnviarEnlace()
        sesion.hayConexion = false

        viewModel.alReenviarEnlace()

        val estado = viewModel.estado.value as EstadoRecuperarContrasena.Enviado
        assertEquals(ErrorDeCuenta.Servidor(MotivoCuenta.SIN_CONEXION), estado.error)
        assertFalse(estado.reenviado)
    }

    @Test
    fun siElSistemaCierraLaAppSeRecuperaElEmail() = runTest {
        val handle = SavedStateHandle()
        crear(handle).alCambiarEmail("ana@correo.com")

        val retomado = crear(handle).estado.value as EstadoRecuperarContrasena.Editando

        assertEquals("ana@correo.com", retomado.email)
    }
}
