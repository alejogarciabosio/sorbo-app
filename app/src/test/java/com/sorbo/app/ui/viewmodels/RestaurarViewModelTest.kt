// Pruebas · ViewModel de Restaurar diario (F8): la oferta, el progreso, el final, "no hay nada" y el reintento tras un error.
package com.sorbo.app.ui.viewmodels

import com.sorbo.app.data.memoria.RespaldoRemotoEnMemoria
import com.sorbo.app.data.memoria.SesionRepositoryEnMemoria
import com.sorbo.app.data.memoria.VinoRepositoryEnMemoria
import com.sorbo.app.domain.degustacionDe
import com.sorbo.app.domain.model.EstadoRestauracion
import com.sorbo.app.domain.model.EstadoSesion
import com.sorbo.app.domain.model.EstadoSync
import com.sorbo.app.domain.model.MotivoRestauracion
import com.sorbo.app.domain.repository.RespaldoException
import com.sorbo.app.domain.usecase.RestaurarDiario
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RestaurarViewModelTest {

    private val vinos = VinoRepositoryEnMemoria()
    private val nube = RespaldoRemotoEnMemoria()
    private val sesion = SesionRepositoryEnMemoria()
    private val restaurar = RestaurarDiario(vinos, nube, sesion, AlmacenFotosFalso(), tamanoPagina = 2)

    @Before
    fun antes() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun despues() = Dispatchers.resetMain()

    private fun crear() = RestaurarViewModel(restaurar).also { it.estado.launchIn(CoroutineScope(Dispatchers.Main)) }

    private suspend fun conCuentaYVinosEnLaNube(cantidad: Int) {
        sesion.crearCuenta("ana@correo.com", "Clave-segura-1")
        val usuario = (sesion.sesion.first() as EstadoSesion.ConSesion).usuario.id
        val lista = (1..cantidad).map { vinoDe("Vino $it", id = "v$it").copy(usuarioId = usuario, estadoSync = EstadoSync.SINCRONIZADO) }
        nube.guardarVinos(lista)
        nube.guardarDegustaciones(lista.map { degustacionDe(it.id) })
    }

    @Test
    fun alAbrirSoloSeOfreceRestaurarYNoSeBajaNada() = runTest {
        conCuentaYVinosEnLaNube(3)

        val viewModel = crear()

        assertEquals(EstadoRestauracion.SinIniciar, viewModel.estado.value)
        assertEquals(0, vinos.observarDiario().first().size)
    }

    @Test
    fun alRestaurarTraeTodoYTermina() = runTest {
        conCuentaYVinosEnLaNube(3)
        val viewModel = crear()

        viewModel.alRestaurar()

        assertEquals(EstadoRestauracion.Terminada(vinos = 3), viewModel.estado.value)
        assertEquals(3, vinos.observarDiario().first().size)
    }

    @Test
    fun sinNadaEnLaNubeAvisaQueNoHayRespaldo() = runTest {
        sesion.crearCuenta("ana@correo.com", "Clave-segura-1")
        val viewModel = crear()

        viewModel.alRestaurar()

        assertEquals(EstadoRestauracion.SinRespaldo, viewModel.estado.value)
    }

    @Test
    fun siSeCortaSePuedeReintentarYNoDuplica() = runTest {
        conCuentaYVinosEnLaNube(3)
        val viewModel = crear()
        nube.falla = RespaldoException(reintentable = true, mensaje = "sin red")
        viewModel.alRestaurar()
        assertEquals(EstadoRestauracion.Error(MotivoRestauracion.SIN_CONEXION), viewModel.estado.value)

        nube.falla = null
        viewModel.alRestaurar()

        assertEquals(EstadoRestauracion.Terminada(vinos = 3), viewModel.estado.value)
        assertEquals(3, vinos.observarDiario().first().size)
    }

    @Test
    fun unaPantallaNuevaNoHeredaElResultadoDeLaVezAnterior() = runTest {
        conCuentaYVinosEnLaNube(2)
        crear().alRestaurar()

        val otraVez = crear()

        assertEquals(EstadoRestauracion.SinIniciar, otraVez.estado.value)
    }
}
