// Pruebas · ViewModel de Buscar: búsquedas recientes (B1) y "Pendiente" solo con cuenta.
package com.sorbo.app.ui.viewmodels

import com.sorbo.app.data.memoria.PreferenciasRepositoryEnMemoria
import com.sorbo.app.data.memoria.SesionRepositoryEnMemoria
import com.sorbo.app.data.memoria.VinoRepositoryEnMemoria
import com.sorbo.app.domain.AHORA
import com.sorbo.app.domain.usecase.BuscarEnDiario
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
class BuscarRecientesViewModelTest {

    private val repositorio = VinoRepositoryEnMemoria(Clock.fixed(AHORA, ZoneOffset.UTC))
    private val preferencias = PreferenciasRepositoryEnMemoria()
    private val sesion = SesionRepositoryEnMemoria()

    @Before
    fun antes() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun despues() = Dispatchers.resetMain()

    private fun crear() = BuscarViewModel(BuscarEnDiario(repositorio), preferencias, sesion).also {
        val alcance = CoroutineScope(Dispatchers.Main)
        it.recientes.launchIn(alcance)
        it.conCuenta.launchIn(alcance)
    }

    @Test
    fun escribirNoGuardaLaBusquedaPeroConfirmarSi() = runTest {
        val viewModel = crear()

        viewModel.alCambiarConsulta("rutini")
        assertTrue(preferencias.busquedasRecientes.first().isEmpty())

        viewModel.alConfirmarBusqueda()

        assertEquals(listOf("rutini"), viewModel.recientes.value)
    }

    @Test
    fun unaBusquedaVaciaNoSeGuarda() = runTest {
        val viewModel = crear()
        viewModel.alCambiarConsulta("   ")

        viewModel.alConfirmarBusqueda()

        assertTrue(viewModel.recientes.value.isEmpty())
    }

    @Test
    fun laMasNuevaVaPrimeraYNoSeRepiten() = runTest {
        val viewModel = crear()
        listOf("rutini", "zuccardi", "RUTINI").forEach {
            viewModel.alCambiarConsulta(it)
            viewModel.alConfirmarBusqueda()
        }

        assertEquals(listOf("RUTINI", "zuccardi"), viewModel.recientes.value)
    }

    @Test
    fun alElegirUnaRecienteSeBuscaConEseTexto() = runTest {
        val viewModel = crear()
        viewModel.alCambiarConsulta("kaiken")
        viewModel.alConfirmarBusqueda()
        viewModel.alCambiarConsulta("")

        viewModel.alElegirReciente("kaiken")

        assertEquals("kaiken", viewModel.consulta.value)
    }

    @Test
    fun sePuedenBorrarTodas() = runTest {
        val viewModel = crear()
        viewModel.alCambiarConsulta("rutini")
        viewModel.alConfirmarBusqueda()

        viewModel.alBorrarRecientes()

        assertTrue(viewModel.recientes.value.isEmpty())
    }

    @Test
    fun elPendienteSoloSeMuestraConCuenta() = runTest {
        val viewModel = crear()
        assertFalse(viewModel.conCuenta.value)

        sesion.crearCuenta("ana@correo.com", "Clave-segura-1")

        assertTrue(viewModel.conCuenta.value)
    }
}
