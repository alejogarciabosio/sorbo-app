// Pruebas · ViewModel de arranque: la bienvenida se muestra la primera vez, no vuelve y no parpadea mientras carga.
package com.sorbo.app.ui.viewmodels

import com.sorbo.app.data.memoria.PreferenciasRepositoryEnMemoria
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
class InicioViewModelTest {

    private val preferencias = PreferenciasRepositoryEnMemoria()

    @Before
    fun antes() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun despues() = Dispatchers.resetMain()

    private fun crear() = InicioViewModel(preferencias).also {
        val alcance = CoroutineScope(Dispatchers.Main)
        it.estado.launchIn(alcance)
        it.irAIniciarSesion.launchIn(alcance)
    }

    @Test
    fun laPrimeraVezSeMuestraLaBienvenida() = runTest {
        assertEquals(EstadoInicio.Bienvenida, crear().estado.value)
    }

    @Test
    fun siYaSeVioVaDirectoALaApp() = runTest {
        preferencias.marcarBienvenidaVista()

        assertEquals(EstadoInicio.Listo, crear().estado.value)
    }

    @Test
    fun alEmpezarLaBienvenidaNoVuelve() = runTest {
        val viewModel = crear()

        viewModel.alEmpezar()

        assertEquals(EstadoInicio.Listo, viewModel.estado.value)
        assertEquals(EstadoInicio.Listo, crear().estado.value)
        assertFalse(viewModel.irAIniciarSesion.value)
    }

    @Test
    fun yaTengoCuentaTambienCuentaComoVistaYPideIniciarSesion() = runTest {
        val viewModel = crear()

        viewModel.alTenerCuenta()

        assertEquals(EstadoInicio.Listo, viewModel.estado.value)
        assertTrue(viewModel.irAIniciarSesion.value)

        viewModel.alConsumirIrAIniciarSesion()

        assertFalse(viewModel.irAIniciarSesion.value)
    }
}
