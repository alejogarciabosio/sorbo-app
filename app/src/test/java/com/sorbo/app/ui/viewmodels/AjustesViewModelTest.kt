// Pruebas · ViewModel de Ajustes: con y sin sesión, respaldo pendiente, interruptor de la IA, cerrar sesión y eliminar cuenta.
package com.sorbo.app.ui.viewmodels

import com.sorbo.app.data.memoria.PreferenciasRepositoryEnMemoria
import com.sorbo.app.data.memoria.SesionRepositoryEnMemoria
import com.sorbo.app.data.memoria.VinoRepositoryEnMemoria
import com.sorbo.app.domain.AHORA
import com.sorbo.app.domain.degustacionDe
import com.sorbo.app.domain.model.EstadoSesion
import com.sorbo.app.domain.repository.MotivoCuenta
import com.sorbo.app.domain.vinoDe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
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
import java.time.Clock
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class AjustesViewModelTest {

    private val reloj = Clock.fixed(AHORA, ZoneOffset.UTC)
    private val repositorio = VinoRepositoryEnMemoria(reloj)
    private val sesion = SesionRepositoryEnMemoria()
    private val preferencias = PreferenciasRepositoryEnMemoria()

    @Before
    fun antes() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun despues() = Dispatchers.resetMain()

    private fun crear() = AjustesViewModel(sesion, preferencias, repositorio)
        .also { it.estado.launchIn(CoroutineScope(Dispatchers.Main)) }

    private suspend fun conCuenta() {
        sesion.crearCuenta("ana@correo.com", "Clave-segura-1")
    }

    private suspend fun conUnVino() {
        val vino = vinoDe("Rutini")
        repositorio.guardarVinoNuevo(vino, degustacionDe(vino.id))
    }

    @Test
    fun sinSesionSeMuestraLaInvitacion() = runTest {
        assertEquals(EstadoAjustes.SinSesion, crear().estado.value)
    }

    @Test
    fun conSesionMuestraElEmailYElRespaldoPendiente() = runTest {
        conCuenta()
        conUnVino()

        val estado = crear().estado.value as EstadoAjustes.ConSesion

        assertEquals("ana@correo.com", estado.email)
        assertEquals(2, estado.pendientes) // el vino y su degustación
        assertTrue(estado.iaActivada)
    }

    @Test
    fun elInterruptorDeLaIaSeGuarda() = runTest {
        conCuenta()
        val viewModel = crear()

        viewModel.alCambiarIa(false)

        assertFalse(preferencias.iaActivada.first())
        assertFalse((viewModel.estado.value as EstadoAjustes.ConSesion).iaActivada)
    }

    @Test
    fun cerrarSesionSinPendientesNoPideConfirmacion() = runTest {
        conCuenta()
        val viewModel = crear()

        viewModel.alPedirCerrarSesion()

        assertEquals(DialogoAjustes.Ninguno, viewModel.dialogo.value)
        assertEquals(EstadoSesion.SinSesion, sesion.sesion.first())
    }

    @Test
    fun cerrarSesionConPendientesAvisaYEsperaLaRespuesta() = runTest {
        conCuenta()
        conUnVino()
        val viewModel = crear()

        viewModel.alPedirCerrarSesion()

        assertEquals(DialogoAjustes.ConfirmarCerrarSesion(2), viewModel.dialogo.value)
        assertTrue(sesion.sesion.first() is EstadoSesion.ConSesion)

        viewModel.alConfirmarCerrarSesion()

        assertEquals(EstadoSesion.SinSesion, sesion.sesion.first())
        assertEquals(DialogoAjustes.Ninguno, viewModel.dialogo.value)
    }

    @Test
    fun alCancelarNoSeCierraLaSesion() = runTest {
        conCuenta()
        conUnVino()
        val viewModel = crear()
        viewModel.alPedirCerrarSesion()

        viewModel.alCerrarDialogo()

        assertEquals(DialogoAjustes.Ninguno, viewModel.dialogo.value)
        assertTrue(sesion.sesion.first() is EstadoSesion.ConSesion)
    }

    @Test
    fun eliminarLaCuentaPideConfirmacionYDespuesLaBorra() = runTest {
        conCuenta()
        conUnVino()
        val viewModel = crear()

        viewModel.alPedirEliminarCuenta()
        assertEquals(DialogoAjustes.ConfirmarEliminarCuenta, viewModel.dialogo.value)
        assertTrue(sesion.sesion.first() is EstadoSesion.ConSesion)

        viewModel.alConfirmarEliminarCuenta()

        assertEquals(DialogoAjustes.Ninguno, viewModel.dialogo.value)
        assertEquals(EstadoSesion.SinSesion, sesion.sesion.first())
        assertEquals(1, repositorio.observarDiario().first().size) // los vinos del teléfono no se tocan
    }

    @Test
    fun siFallaEliminarLaCuentaSeAvisaYSeConservaLaSesion() = runTest {
        conCuenta()
        sesion.hayConexion = false
        val viewModel = crear()
        viewModel.alPedirEliminarCuenta()

        viewModel.alConfirmarEliminarCuenta()

        assertEquals(DialogoAjustes.FalloAlEliminar(MotivoCuenta.SIN_CONEXION), viewModel.dialogo.value)
        assertTrue(sesion.sesion.first() is EstadoSesion.ConSesion)
    }

    @Test
    fun sinSesionNoSePuedeEliminarNada() = runTest {
        val viewModel = crear()

        viewModel.alPedirEliminarCuenta()

        assertEquals(DialogoAjustes.Ninguno, viewModel.dialogo.value)
    }
}
