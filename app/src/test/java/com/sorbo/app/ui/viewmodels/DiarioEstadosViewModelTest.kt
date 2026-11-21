// Pruebas · ViewModel del Diario: aviso sin conexión, "Pendiente" solo con cuenta y el estado de error con reintento.
package com.sorbo.app.ui.viewmodels

import com.sorbo.app.data.memoria.ConectividadSimulada
import com.sorbo.app.data.memoria.SesionRepositoryEnMemoria
import com.sorbo.app.data.memoria.VinoRepositoryEnMemoria
import com.sorbo.app.domain.AHORA
import com.sorbo.app.domain.degustacionDe
import com.sorbo.app.domain.model.VinoConDegustaciones
import com.sorbo.app.domain.repository.VinoRepository
import com.sorbo.app.domain.vinoDe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
import java.io.IOException
import java.time.Clock
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class DiarioEstadosViewModelTest {

    private val repositorio = VinoRepositoryEnMemoria(Clock.fixed(AHORA, ZoneOffset.UTC))
    private val sesion = SesionRepositoryEnMemoria()
    private val conectividad = ConectividadSimulada()

    /** Un diario que falla mientras [roto] sea verdadero, como si la base local no se pudiera leer. */
    private class RepositorioQueFalla(private val base: VinoRepository) : VinoRepository by base {
        var roto = true
        override fun observarDiario(): Flow<List<VinoConDegustaciones>> =
            if (roto) flow { throw IOException("no se pudo leer") } else base.observarDiario()
    }

    @Before
    fun antes() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun despues() = Dispatchers.resetMain()

    private fun DiarioViewModel.observar() = apply {
        val alcance = CoroutineScope(Dispatchers.Main)
        estado.launchIn(alcance)
        sinConexion.launchIn(alcance)
        conCuenta.launchIn(alcance)
        pendientes.launchIn(alcance)
    }

    private suspend fun conUnVino() {
        val vino = vinoDe("Rutini")
        repositorio.guardarVinoNuevo(vino, degustacionDe(vino.id))
    }

    @Test
    fun sinConexionSeAvisaYElDiarioSigueMostrandoLosVinos() = runTest {
        conUnVino()
        val viewModel = DiarioViewModel(repositorio, sesion, conectividad).observar()

        conectividad.cambiar(false)

        assertTrue(viewModel.sinConexion.value)
        assertTrue(viewModel.estado.value is EstadoDiario.ConVinos)
    }

    @Test
    fun conConexionNoHayAviso() = runTest {
        assertFalse(DiarioViewModel(repositorio, sesion, conectividad).observar().sinConexion.value)
    }

    @Test
    fun sinCuentaNoSeMuestraNingunPendiente() = runTest {
        conUnVino()
        val viewModel = DiarioViewModel(repositorio, sesion, conectividad).observar()

        assertFalse(viewModel.conCuenta.value)
        assertEquals(0, viewModel.pendientes.value)
    }

    @Test
    fun conCuentaSeCuentanLosCambiosSinRespaldar() = runTest {
        conUnVino()
        sesion.crearCuenta("ana@correo.com", "Clave-segura-1")
        val viewModel = DiarioViewModel(repositorio, sesion, conectividad).observar()

        assertTrue(viewModel.conCuenta.value)
        assertEquals(2, viewModel.pendientes.value) // el vino y su degustación
    }

    @Test
    fun siNoSePuedeLeerElDiarioHayUnEstadoDeErrorYSePuedeReintentar() = runTest {
        val falla = RepositorioQueFalla(repositorio)
        val viewModel = DiarioViewModel(falla).observar()
        assertEquals(EstadoDiario.ConError, viewModel.estado.value)

        falla.roto = false
        viewModel.alReintentar()

        assertEquals(EstadoDiario.Vacio, viewModel.estado.value)
    }
}
