package com.sorbo.app.ui.viewmodels

import com.sorbo.app.data.memoria.VinoRepositoryEnMemoria
import com.sorbo.app.domain.AHORA
import com.sorbo.app.domain.degustacionDe
import com.sorbo.app.domain.model.Dulzor
import com.sorbo.app.domain.usecase.CalcularEstadisticasGustos
import com.sorbo.app.domain.usecase.CategoriaGusto
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
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class GustosViewModelTest {

    private val repositorio = VinoRepositoryEnMemoria(Clock.fixed(AHORA, ZoneOffset.UTC))

    @Before
    fun antes() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun despues() = Dispatchers.resetMain()

    private fun crearViewModel() = GustosViewModel(repositorio, CalcularEstadisticasGustos())

    /** Guarda un vino nuevo con [cantidad] degustaciones secas que gustaron. Devuelve el id del vino. */
    private suspend fun guardarVinoSeco(nombre: String, cantidad: Int): String {
        val vino = vinoDe(nombre)
        repositorio.guardarVinoNuevo(vino, degustacionDe(vino.id, dulzor = Dulzor.SECO))
        repeat(cantidad - 1) { repositorio.agregarDegustacion(degustacionDe(vino.id, dulzor = Dulzor.SECO)) }
        return vino.id
    }

    @Test
    fun conElDiarioVacioElEstadoEsVacio() = runTest {
        val viewModel = crearViewModel()
        val job = launch { viewModel.estado.collect {} }

        assertEquals(EstadoGustos.Vacio, viewModel.estado.first { it != EstadoGustos.Cargando })
        job.cancel()
    }

    @Test
    fun conMenosDeCincoDegustacionesDiceCuantasFaltan() = runTest {
        guardarVinoSeco("Rutini Malbec", cantidad = 3)
        val viewModel = crearViewModel()
        val job = launch { viewModel.estado.collect {} }

        val estado = viewModel.estado.first { it != EstadoGustos.Cargando }
        assertEquals(EstadoGustos.PocasDegustaciones(registradas = 3, faltan = 2, necesarias = 5), estado)
        job.cancel()
    }

    @Test
    fun conCuatroDegustacionesFaltaUna() = runTest {
        guardarVinoSeco("Rutini Malbec", cantidad = 4)
        val viewModel = crearViewModel()
        val job = launch { viewModel.estado.collect {} }

        val estado = viewModel.estado.first { it != EstadoGustos.Cargando } as EstadoGustos.PocasDegustaciones
        assertEquals(1, estado.faltan)
        job.cancel()
    }

    @Test
    fun conExactamenteCincoDegustacionesSeHabilitanLasEstadisticas() = runTest {
        guardarVinoSeco("Rutini Malbec", cantidad = 5)
        val viewModel = crearViewModel()
        val job = launch { viewModel.estado.collect {} }

        val estado = viewModel.estado.first { it != EstadoGustos.Cargando } as EstadoGustos.ConEstadisticas
        assertEquals(5, estado.estadisticas.totalDegustaciones)
        assertEquals(
            listOf(CategoriaGusto.PorDulzor(Dulzor.SECO)),
            estado.estadisticas.loQueMasDisfrutas.map { it.categoria },
        )
        job.cancel()
    }

    @Test
    fun conMasDeCincoDegustacionesDeVariosVinosJuntaTodas() = runTest {
        guardarVinoSeco("Rutini Malbec", cantidad = 4)
        guardarVinoSeco("Trapiche Cabernet", cantidad = 4)
        val viewModel = crearViewModel()
        val job = launch { viewModel.estado.collect {} }

        val estado = viewModel.estado.first { it != EstadoGustos.Cargando } as EstadoGustos.ConEstadisticas
        val seco = estado.estadisticas.loQueMasDisfrutas.single()
        assertEquals(8, estado.estadisticas.totalDegustaciones)
        assertEquals(8, seco.total)
        assertEquals(8, seco.favorables)
        job.cancel()
    }

    @Test
    fun alAgregarYEliminarDegustacionesElEstadoSeActualiza() = runTest {
        val vinoId = guardarVinoSeco("Rutini Malbec", cantidad = 4)
        val viewModel = crearViewModel()
        val job = launch { viewModel.estado.collect {} }
        assertEquals(1, (viewModel.estado.first { it != EstadoGustos.Cargando } as EstadoGustos.PocasDegustaciones).faltan)

        val quinta = degustacionDe(vinoId, dulzor = Dulzor.SECO)
        repositorio.agregarDegustacion(quinta)
        assertEquals(5, (viewModel.estado.first { it is EstadoGustos.ConEstadisticas } as EstadoGustos.ConEstadisticas).estadisticas.totalDegustaciones)

        repositorio.eliminarDegustacion(quinta.id)
        assertEquals(1, (viewModel.estado.first { it is EstadoGustos.PocasDegustaciones } as EstadoGustos.PocasDegustaciones).faltan)
        job.cancel()
    }

    @Test
    fun alEliminarElUnicoVinoVuelveAlEstadoVacio() = runTest {
        val vinoId = guardarVinoSeco("Rutini Malbec", cantidad = 2)
        val viewModel = crearViewModel()
        val job = launch { viewModel.estado.collect {} }
        viewModel.estado.first { it is EstadoGustos.PocasDegustaciones }

        repositorio.eliminarVino(vinoId)

        assertEquals(EstadoGustos.Vacio, viewModel.estado.first { it == EstadoGustos.Vacio })
        job.cancel()
    }
}
