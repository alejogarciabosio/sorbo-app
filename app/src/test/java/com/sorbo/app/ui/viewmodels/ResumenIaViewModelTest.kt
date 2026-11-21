// Pruebas · ViewModel del resumen con IA: cuándo se ofrece, el consentimiento de la primera vez y cada error, con repositorios simulados.
package com.sorbo.app.ui.viewmodels

import com.sorbo.app.data.memoria.AnalisisGustosRepositoryEnMemoria
import com.sorbo.app.data.memoria.ConectividadSimulada
import com.sorbo.app.data.memoria.PreferenciasRepositoryEnMemoria
import com.sorbo.app.data.memoria.SesionRepositoryEnMemoria
import com.sorbo.app.data.memoria.VinoRepositoryEnMemoria
import com.sorbo.app.domain.AHORA
import com.sorbo.app.domain.degustacionDe
import com.sorbo.app.domain.repository.MotivoAnalisis
import com.sorbo.app.domain.usecase.CalcularEstadisticasGustos
import com.sorbo.app.domain.vinoDe
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class ResumenIaViewModelTest {

    private val reloj = Clock.fixed(AHORA, ZoneOffset.UTC)
    private val repositorio = VinoRepositoryEnMemoria(reloj)
    private val analisis = AnalisisGustosRepositoryEnMemoria(reloj)
    private val preferencias = PreferenciasRepositoryEnMemoria()
    private val sesion = SesionRepositoryEnMemoria()
    private val conectividad = ConectividadSimulada()

    @Before
    fun antes() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun despues() = Dispatchers.resetMain()

    private fun crear() = ResumenIaViewModel(repositorio, CalcularEstadisticasGustos(), analisis, preferencias, sesion, conectividad)
        .also { viewModel -> viewModel.estado.launchIn(kotlinx.coroutines.CoroutineScope(Dispatchers.Main)) }

    /** Carga [cantidad] degustaciones en el diario, con un vino por cada una. */
    private suspend fun conDegustaciones(cantidad: Int) {
        repeat(cantidad) {
            val vino = vinoDe("Vino ${java.util.UUID.randomUUID()}")
            repositorio.guardarVinoNuevo(vino, degustacionDe(vino.id))
        }
    }

    private suspend fun conCuenta() {
        sesion.crearCuenta("ana@correo.com", "Clave-segura-1")
    }

    @Test
    fun conMenosDeCincoDegustacionesNoSeOfreceNada() = runTest {
        conDegustaciones(4)
        conCuenta()

        assertEquals(EstadoResumenIa.Oculto, crear().estado.value)
    }

    @Test
    fun sinCuentaSeExplicaQueHaceFaltaUna() = runTest {
        conDegustaciones(6)

        assertEquals(EstadoResumenIa.RequiereCuenta, crear().estado.value)
    }

    @Test
    fun conLaIaDesactivadaEnAjustesNoSeOfreceNada() = runTest {
        conDegustaciones(6)
        conCuenta()
        preferencias.cambiarIaActivada(false)

        assertEquals(EstadoResumenIa.Oculto, crear().estado.value)
    }

    @Test
    fun conTodoListoSeOfreceGenerarSinResumenPrevio() = runTest {
        conDegustaciones(6)
        conCuenta()

        val estado = crear().estado.value as EstadoResumenIa.Disponible

        assertNull(estado.ultimo)
        assertTrue(estado.hayConexion)
        assertFalse(estado.pideAviso)
    }

    @Test
    fun laPrimeraVezPideElConsentimientoAntesDeMandarNada() = runTest {
        conDegustaciones(6)
        conCuenta()
        val viewModel = crear()

        viewModel.alGenerar()

        assertTrue((viewModel.estado.value as EstadoResumenIa.Disponible).pideAviso)
        assertNull(analisis.observarUltimo().first())
    }

    @Test
    fun alAceptarElAvisoGeneraYNoVuelveAPreguntar() = runTest {
        conDegustaciones(6)
        conCuenta()
        val viewModel = crear()
        viewModel.alGenerar()

        viewModel.alAceptarAviso()

        val estado = viewModel.estado.value as EstadoResumenIa.Disponible
        assertNotNull(estado.ultimo)
        assertTrue(preferencias.avisoIaAceptado.first())
        viewModel.alGenerar()
        assertFalse((viewModel.estado.value as EstadoResumenIa.Disponible).pideAviso)
    }

    @Test
    fun alRechazarElAvisoNoSeMandaNada() = runTest {
        conDegustaciones(6)
        conCuenta()
        val viewModel = crear()
        viewModel.alGenerar()

        viewModel.alRechazarAviso()

        assertFalse((viewModel.estado.value as EstadoResumenIa.Disponible).pideAviso)
        assertNull(analisis.observarUltimo().first())
        assertFalse(preferencias.avisoIaAceptado.first())
    }

    @Test
    fun conElAvisoAceptadoGeneraDirecto() = runTest {
        conDegustaciones(6)
        conCuenta()
        preferencias.aceptarAvisoIa()
        val viewModel = crear()

        viewModel.alGenerar()

        assertNotNull((viewModel.estado.value as EstadoResumenIa.Disponible).ultimo)
    }

    @Test
    fun lasDegustacionesNuevasDespuesDelResumenSeCuentan() = runTest {
        conDegustaciones(6)
        conCuenta()
        preferencias.aceptarAvisoIa()
        val viewModel = crear()
        viewModel.alGenerar()

        conDegustaciones(2)

        assertEquals(2, (viewModel.estado.value as EstadoResumenIa.Disponible).nuevasDesdeElUltimo)
    }

    @Test
    fun cadaErrorTieneSuEstadoYConservaElResumenAnterior() = runTest {
        conDegustaciones(6)
        conCuenta()
        preferencias.aceptarAvisoIa()
        val viewModel = crear()
        viewModel.alGenerar()
        val anterior = (viewModel.estado.value as EstadoResumenIa.Disponible).ultimo

        MotivoAnalisis.entries.forEach { motivo ->
            analisis.fallaConMotivo = motivo
            viewModel.alGenerar()

            assertEquals(EstadoResumenIa.ConError(motivo, anterior), viewModel.estado.value)
            viewModel.alDescartarError()
            assertTrue(viewModel.estado.value is EstadoResumenIa.Disponible)
        }
    }

    @Test
    fun despuesDeUnErrorSePuedeReintentar() = runTest {
        conDegustaciones(6)
        conCuenta()
        preferencias.aceptarAvisoIa()
        val viewModel = crear()
        analisis.fallaConMotivo = MotivoAnalisis.SERVICIO_NO_DISPONIBLE
        viewModel.alGenerar()
        analisis.fallaConMotivo = null

        viewModel.alGenerar()

        assertNotNull((viewModel.estado.value as EstadoResumenIa.Disponible).ultimo)
    }

    @Test
    fun sinConexionSeIndicaYSeSigueViendoElUltimoResumen() = runTest {
        conDegustaciones(6)
        conCuenta()
        preferencias.aceptarAvisoIa()
        val viewModel = crear()
        viewModel.alGenerar()

        conectividad.cambiar(false)

        val estado = viewModel.estado.value as EstadoResumenIa.Disponible
        assertFalse(estado.hayConexion)
        assertNotNull(estado.ultimo)
    }
}
