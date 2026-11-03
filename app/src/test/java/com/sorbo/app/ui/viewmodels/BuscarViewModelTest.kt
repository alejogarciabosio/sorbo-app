package com.sorbo.app.ui.viewmodels

import com.sorbo.app.data.memoria.VinoRepositoryEnMemoria
import com.sorbo.app.domain.AHORA
import com.sorbo.app.domain.degustacionDe
import com.sorbo.app.domain.usecase.BuscarEnDiario
import com.sorbo.app.domain.usecase.ResultadoBusqueda
import com.sorbo.app.domain.vinoDe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class BuscarViewModelTest {

    private val repositorio = VinoRepositoryEnMemoria(Clock.fixed(AHORA, ZoneOffset.UTC))
    private val viewModel = BuscarViewModel(BuscarEnDiario(repositorio))

    @Before
    fun antes() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun despues() = Dispatchers.resetMain()

    private suspend fun guardar(nombre: String, anio: Int? = null) {
        val vino = vinoDe(nombre, anio = anio)
        repositorio.guardarVinoNuevo(vino, degustacionDe(vino.id))
    }

    @Test
    fun sinTextoNoHayResultado() = runTest {
        assertEquals(EstadoBusqueda.SinConsulta, viewModel.estado.value)
    }

    @Test
    fun alEscribirMuestraLosVinosEncontrados() = runTest {
        guardar("Rutini Malbec")
        val job = launch { viewModel.estado.collect {} }

        viewModel.alCambiarConsulta("rutini")

        val estado = viewModel.estado.first { it is EstadoBusqueda.ConResultado } as EstadoBusqueda.ConResultado
        val encontrados = estado.resultado as ResultadoBusqueda.Encontrados
        assertEquals(listOf("Rutini Malbec"), encontrados.vinos.map { it.vino.nombre })
        job.cancel()
    }

    @Test
    fun siNoEstaEnElDiarioDiceQueNoSeEncontro() = runTest {
        guardar("Rutini Malbec")
        val job = launch { viewModel.estado.collect {} }

        viewModel.alCambiarConsulta("Cheval Blanc")

        val estado = viewModel.estado.first { it is EstadoBusqueda.ConResultado } as EstadoBusqueda.ConResultado
        assertTrue(estado.resultado is ResultadoBusqueda.NoEncontrado)
        job.cancel()
    }

    @Test
    fun alBorrarElTextoVuelveAlInicio() = runTest {
        guardar("Rutini Malbec")
        val job = launch { viewModel.estado.collect {} }

        viewModel.alCambiarConsulta("rutini")
        viewModel.alCambiarConsulta("")

        assertEquals(EstadoBusqueda.SinConsulta, viewModel.estado.first { it == EstadoBusqueda.SinConsulta })
        job.cancel()
    }
}
