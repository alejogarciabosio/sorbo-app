package com.sorbo.app.ui.diario

import com.sorbo.app.data.memoria.VinoRepositoryEnMemoria
import com.sorbo.app.domain.AHORA
import com.sorbo.app.domain.degustacionDe
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
class DiarioViewModelTest {

    private val repositorio = VinoRepositoryEnMemoria(Clock.fixed(AHORA, ZoneOffset.UTC))

    @Before
    fun antes() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun despues() = Dispatchers.resetMain()

    @Test
    fun sinVinosElEstadoEsVacio() = runTest {
        val viewModel = DiarioViewModel(repositorio)
        val job = launch { viewModel.estado.collect {} }

        assertEquals(EstadoDiario.Vacio, viewModel.estado.first { it != EstadoDiario.Cargando })
        job.cancel()
    }

    @Test
    fun alGuardarUnVinoApareceEnElDiario() = runTest {
        val viewModel = DiarioViewModel(repositorio)
        val job = launch { viewModel.estado.collect {} }

        val vino = vinoDe("Rutini Malbec")
        repositorio.guardarVinoNuevo(vino, degustacionDe(vino.id))

        val estado = viewModel.estado.first { it is EstadoDiario.ConVinos } as EstadoDiario.ConVinos
        assertEquals(listOf("Rutini Malbec"), estado.vinos.map { it.vino.nombre })
        assertTrue(estado.vinos.single().gustoEnGeneral)
        job.cancel()
    }
}
