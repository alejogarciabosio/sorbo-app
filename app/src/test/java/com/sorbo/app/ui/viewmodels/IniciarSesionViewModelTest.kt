// Pruebas · ViewModel de Iniciar sesión: validaciones, errores de cada motivo y camino feliz con la sesión simulada.
package com.sorbo.app.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.sorbo.app.data.memoria.SesionRepositoryEnMemoria
import com.sorbo.app.domain.model.Usuario
import com.sorbo.app.domain.repository.MotivoCuenta
import com.sorbo.app.domain.usecase.ResultadoInicioSesion
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
class IniciarSesionViewModelTest {

    private val sesion = SesionRepositoryEnMemoria()
    private val entraron = mutableListOf<Usuario>()

    @Before
    fun antes() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun despues() = Dispatchers.resetMain()

    private suspend fun conCuentaCreada() {
        sesion.crearCuenta("ana@correo.com", "Clave-segura-1")
        sesion.cerrarSesion()
    }

    private fun crear(
        handle: SavedStateHandle = SavedStateHandle(),
        alEntrar: suspend (Usuario) -> ResultadoInicioSesion? = { entraron += it; null },
    ) = IniciarSesionViewModel(sesion, handle, alEntrar)

    private fun IniciarSesionViewModel.editando() = estado.value as EstadoIniciarSesion.Editando

    private fun IniciarSesionViewModel.completar(email: String, clave: String) {
        alCambiarEmail(email)
        alCambiarClave(clave)
    }

    @Test
    fun conUnaCuentaCorrectaAbreLaSesionYAvisa() = runTest {
        conCuentaCreada()
        val viewModel = crear()
        viewModel.completar("ana@correo.com", "Clave-segura-1")

        viewModel.alIniciarSesion()

        assertEquals(EstadoIniciarSesion.Listo(), viewModel.estado.value)
        assertEquals(listOf("ana@correo.com"), entraron.map { it.email })
    }

    @Test
    fun unEmailMalEscritoNoLlegaAlServidor() = runTest {
        conCuentaCreada()
        val viewModel = crear()
        viewModel.completar("ana-sin-arroba", "Clave-segura-1")

        viewModel.alIniciarSesion()

        assertEquals(ErrorDeCuenta.EmailInvalido, viewModel.editando().error)
        assertFalse(viewModel.editando().enviando)
        assertTrue(entraron.isEmpty())
    }

    @Test
    fun sinContrasenaPideEscribirla() = runTest {
        val viewModel = crear()
        viewModel.alCambiarEmail("ana@correo.com")

        viewModel.alIniciarSesion()

        assertEquals(ErrorDeCuenta.ClaveVacia, viewModel.editando().error)
    }

    @Test
    fun credencialesIncorrectasMuestranUnMensajeUnicoYDejanReintentar() = runTest {
        conCuentaCreada()
        val viewModel = crear()
        viewModel.completar("ana@correo.com", "otra-clave-1")

        viewModel.alIniciarSesion()

        val editando = viewModel.editando()
        assertEquals(ErrorDeCuenta.Servidor(MotivoCuenta.CREDENCIALES_INCORRECTAS), editando.error)
        assertFalse(editando.enviando)
        assertEquals("ana@correo.com", editando.email)
        assertTrue(entraron.isEmpty())
    }

    @Test
    fun sinConexionSeAvisaYNoSePierdeLoEscrito() = runTest {
        conCuentaCreada()
        sesion.hayConexion = false
        val viewModel = crear()
        viewModel.completar("ana@correo.com", "Clave-segura-1")

        viewModel.alIniciarSesion()

        assertEquals(ErrorDeCuenta.Servidor(MotivoCuenta.SIN_CONEXION), viewModel.editando().error)
        assertEquals("Clave-segura-1", viewModel.editando().clave)
    }

    @Test
    fun alEscribirDeNuevoDesapareceElError() = runTest {
        val viewModel = crear()
        viewModel.alIniciarSesion()
        assertEquals(ErrorDeCuenta.EmailInvalido, viewModel.editando().error)

        viewModel.alCambiarEmail("a")

        assertEquals(null, viewModel.editando().error)
    }

    @Test
    fun siFallaLoQueSigueALaSesionIgualSeEntra() = runTest {
        conCuentaCreada()
        val viewModel = crear(alEntrar = { throw IllegalStateException("falló el respaldo") })
        viewModel.completar("ana@correo.com", "Clave-segura-1")

        viewModel.alIniciarSesion()

        assertEquals(EstadoIniciarSesion.Listo(), viewModel.estado.value)
    }

    @Test
    fun siElSistemaCierraLaAppSeRecuperaElEmailPeroNuncaLaContrasena() = runTest {
        val handle = SavedStateHandle()
        crear(handle).completar("ana@correo.com", "Clave-segura-1")

        val retomado = crear(handle)

        assertEquals("ana@correo.com", retomado.editando().email)
        assertEquals("", retomado.editando().clave)
    }

    @Test
    fun siHayUnRespaldoEnLaNubeSeOfreceRestaurarlo() = runTest {
        conCuentaCreada()
        val viewModel = crear(alEntrar = { ResultadoInicioSesion(vinosParaRespaldar = 0, hayRespaldoParaRestaurar = true) })
        viewModel.completar("ana@correo.com", "Clave-segura-1")

        viewModel.alIniciarSesion()

        assertEquals(EstadoIniciarSesion.Listo(hayRespaldoParaRestaurar = true), viewModel.estado.value)
    }
}
