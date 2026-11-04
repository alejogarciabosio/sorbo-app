package com.sorbo.app.ui.viewmodels

import com.sorbo.app.data.memoria.VinoRepositoryEnMemoria
import com.sorbo.app.domain.AHORA
import com.sorbo.app.domain.degustacionDe
import com.sorbo.app.domain.usecase.EliminarVino
import com.sorbo.app.domain.usecase.ObtenerDetalleVino
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class DetalleViewModelTest {

    private val repositorio = VinoRepositoryEnMemoria(Clock.fixed(AHORA, ZoneOffset.UTC))

    @Before
    fun antes() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun despues() = Dispatchers.resetMain()

    private fun crearViewModel(vinoId: String) =
        DetalleViewModel(vinoId, ObtenerDetalleVino(repositorio), EliminarVino(repositorio))

    private suspend fun guardar(nombre: String, anio: Int? = null): String {
        val vino = vinoDe(nombre, bodega = "Rutini", anio = anio)
        repositorio.guardarVinoNuevo(vino, degustacionDe(vino.id))
        return vino.id
    }

    @Test
    fun muestraElVinoConSusDegustaciones() = runTest {
        val id = guardar("Rutini Malbec 2021", anio = 2021)
        val viewModel = crearViewModel(id)
        val job = launch { viewModel.estado.collect {} }

        val estado = viewModel.estado.first { it is EstadoDetalle.Contenido } as EstadoDetalle.Contenido

        assertEquals("Rutini Malbec 2021", estado.detalle.vino.nombre)
        assertEquals(1, estado.detalle.veces)
        job.cancel()
    }

    @Test
    fun unVinoInexistenteSeMuestraComoNoEncontrado() = runTest {
        val viewModel = crearViewModel("no-existe")
        val job = launch { viewModel.estado.collect {} }

        assertEquals(EstadoDetalle.NoEncontrado, viewModel.estado.first { it != EstadoDetalle.Cargando })
        job.cancel()
    }

    @Test
    fun alAgregarOtraDegustacionElDetalleSeActualizaSolo() = runTest {
        val id = guardar("Rutini Malbec 2021", anio = 2021)
        val viewModel = crearViewModel(id)
        val job = launch { viewModel.estado.collect {} }

        viewModel.estado.first { it is EstadoDetalle.Contenido }

        repositorio.agregarDegustacion(degustacionDe(id, meGusto = false))

        val estado = viewModel.estado.first { it is EstadoDetalle.Contenido && it.detalle.veces == 2 } as EstadoDetalle.Contenido
        assertEquals(2, estado.detalle.veces)
        assertEquals(1, estado.detalle.vecesQueGusto)
        job.cancel()
    }

    @Test
    fun alEliminarAvisaConElNombreYDejaDeMostrarElVino() = runTest {
        val id = guardar("Rutini Malbec 2021", anio = 2021)
        val viewModel = crearViewModel(id)
        val job = launch { viewModel.estado.collect {} }
        viewModel.estado.first { it is EstadoDetalle.Contenido }

        viewModel.alEliminar()

        assertEquals(VinoEliminado(id, "Rutini Malbec 2021"), viewModel.eliminado.value)
        assertEquals(EstadoDetalle.NoEncontrado, viewModel.estado.first { it == EstadoDetalle.NoEncontrado })
        assertTrue(repositorio.listarVinos().isEmpty())
        job.cancel()
    }

    @Test
    fun siNoSeEliminaNoHayAvisoDeEliminado() = runTest {
        val viewModel = crearViewModel(guardar("Rutini Malbec 2021"))

        assertNull(viewModel.eliminado.value)
    }

    @Test
    fun eliminarDosVecesSeguidasEliminaUnaSolaVez() = runTest {
        val id = guardar("Rutini Malbec 2021")
        val viewModel = crearViewModel(id)
        val job = launch { viewModel.estado.collect {} }
        viewModel.estado.first { it is EstadoDetalle.Contenido }

        viewModel.alEliminar()
        viewModel.alEliminar()

        assertEquals(VinoEliminado(id, "Rutini Malbec 2021"), viewModel.eliminado.value)
        job.cancel()
    }
}
