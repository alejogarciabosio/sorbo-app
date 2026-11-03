package com.sorbo.app.ui.viewmodels

import com.sorbo.app.data.memoria.VinoRepositoryEnMemoria
import com.sorbo.app.domain.AHORA
import com.sorbo.app.domain.degustacionDe
import com.sorbo.app.domain.model.Dulzor
import com.sorbo.app.domain.usecase.BuscarVinoParecido
import com.sorbo.app.domain.usecase.CampoObligatorio
import com.sorbo.app.domain.usecase.ObtenerDetalleVino
import com.sorbo.app.domain.usecase.RegistrarDegustacion
import com.sorbo.app.domain.vinoDe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
class RegistroViewModelTest {

    private val reloj = Clock.fixed(AHORA, ZoneOffset.UTC)
    private val repositorio = VinoRepositoryEnMemoria(reloj)

    @Before
    fun antes() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun despues() = Dispatchers.resetMain()

    private fun crearViewModel(nombreInicial: String? = null) = RegistroViewModel(
        buscarVinoParecido = BuscarVinoParecido(repositorio),
        registrarDegustacion = RegistrarDegustacion(repositorio, reloj),
        obtenerDetalleVino = ObtenerDetalleVino(repositorio),
        reloj = reloj,
        nombreInicial = nombreInicial,
    )

    private suspend fun guardarEnDiario(nombre: String, anio: Int? = null, bodega: String? = null): String {
        val vino = vinoDe(nombre, bodega = bodega, anio = anio)
        repositorio.guardarVinoNuevo(vino, degustacionDe(vino.id))
        return vino.id
    }

    private fun RegistroViewModel.camposActuales() = (estado.value as EstadoRegistro.Editando).campos

    @Test
    fun arrancaConElNombreQueVeniaDeLaBusqueda() {
        val viewModel = crearViewModel(nombreInicial = "Cheval Blanc")

        assertEquals("Cheval Blanc", viewModel.camposActuales().nombre)
    }

    @Test
    fun sinNombreNiOpinionIndicaQueFaltaYNoGuardaNada() = runTest {
        val viewModel = crearViewModel()

        viewModel.alGuardar()

        val estado = viewModel.estado.value as EstadoRegistro.Editando
        assertEquals(setOf(CampoObligatorio.NOMBRE, CampoObligatorio.OPINION), estado.faltantes)
        assertTrue(repositorio.listarVinos().isEmpty())
    }

    @Test
    fun alCompletarUnCampoObligatorioDejaDeMarcarloComoFaltante() = runTest {
        val viewModel = crearViewModel()
        viewModel.alGuardar()

        viewModel.alCambiarNombre("Cheval Blanc")

        val estado = viewModel.estado.value as EstadoRegistro.Editando
        assertEquals(setOf(CampoObligatorio.OPINION), estado.faltantes)
        assertEquals("Cheval Blanc", estado.campos.nombre)
    }

    @Test
    fun unVinoSinParecidosSeCreaNuevo() = runTest {
        val viewModel = crearViewModel()
        viewModel.alCambiarNombre("Cheval Blanc")
        viewModel.alElegirMeGusto(true)

        viewModel.alGuardar()

        val estado = viewModel.estado.value as EstadoRegistro.Guardado
        assertTrue(estado.vinoNuevo)
        assertEquals(listOf("Cheval Blanc"), repositorio.listarVinos().map { it.nombre })
    }

    @Test
    fun elMismoVinoSumaUnaDegustacionSinDuplicarlo() = runTest {
        val vinoId = guardarEnDiario("Rutini Malbec 2021", anio = 2021)
        val viewModel = crearViewModel()
        viewModel.alCambiarNombre("Rutini Malbec 2021")
        viewModel.alElegirMeGusto(false)

        viewModel.alGuardar()

        val estado = viewModel.estado.value as EstadoRegistro.Guardado
        assertEquals(vinoId, estado.vinoId)
        assertEquals(false, estado.vinoNuevo)
        assertEquals(1, repositorio.listarVinos().size)
        assertEquals(2, repositorio.observarVino(vinoId).first()?.degustaciones?.size)
    }

    @Test
    fun otraAnadaEsUnVinoNuevoYNoPregunta() = runTest {
        guardarEnDiario("Rutini Malbec 2021", anio = 2021)
        val viewModel = crearViewModel()
        viewModel.alCambiarNombre("Rutini Malbec 2022")
        viewModel.alElegirMeGusto(true)

        viewModel.alGuardar()

        val estado = viewModel.estado.value as EstadoRegistro.Guardado
        assertTrue(estado.vinoNuevo)
        assertEquals(2, repositorio.listarVinos().size)
    }

    @Test
    fun conUnNombreParecidoPreguntaAntesDeGuardar() = runTest {
        guardarEnDiario("Rutini Malbec 2021", anio = 2021)
        val viewModel = crearViewModel()
        viewModel.alCambiarNombre("Rutini Malbek 2021")
        viewModel.alElegirMeGusto(true)

        viewModel.alGuardar()

        val estado = viewModel.estado.value as EstadoRegistro.PreguntandoMismoVino
        assertEquals("Rutini Malbec 2021", estado.parecido.vino.nombre)
        assertEquals(1, repositorio.listarVinos().size)
    }

    @Test
    fun alConfirmarQueEsElMismoVinoSumaLaDegustacionAEseVino() = runTest {
        val vinoId = guardarEnDiario("Rutini Malbec 2021", anio = 2021)
        val viewModel = crearViewModel()
        viewModel.alCambiarNombre("Rutini Malbek 2021")
        viewModel.alElegirMeGusto(true)
        viewModel.alGuardar()

        viewModel.alConfirmarMismoVino()

        val estado = viewModel.estado.value as EstadoRegistro.Guardado
        assertEquals(vinoId, estado.vinoId)
        assertEquals(1, repositorio.listarVinos().size)
        assertEquals(2, repositorio.observarVino(vinoId).first()?.degustaciones?.size)
    }

    @Test
    fun alElegirQueEsOtroVinoCreaUnoNuevo() = runTest {
        guardarEnDiario("Rutini Malbec 2021", anio = 2021)
        val viewModel = crearViewModel()
        viewModel.alCambiarNombre("Rutini Malbek 2021")
        viewModel.alElegirMeGusto(true)
        viewModel.alGuardar()

        viewModel.alElegirOtroVino()

        val estado = viewModel.estado.value as EstadoRegistro.Guardado
        assertTrue(estado.vinoNuevo)
        assertEquals(2, repositorio.listarVinos().size)
    }

    @Test
    fun alVolverAEditarConservaLoCargado() = runTest {
        guardarEnDiario("Rutini Malbec 2021", anio = 2021)
        val viewModel = crearViewModel()
        viewModel.alCambiarNombre("Rutini Malbek 2021")
        viewModel.alElegirMeGusto(true)
        viewModel.alElegirDulzor(Dulzor.SECO)
        viewModel.alGuardar()

        viewModel.alVolverAEditar()

        val campos = viewModel.camposActuales()
        assertEquals("Rutini Malbek 2021", campos.nombre)
        assertEquals(Dulzor.SECO, campos.dulzor)
        assertEquals(1, repositorio.listarVinos().size)
    }

    @Test
    fun tocarDeNuevoUnaOpcionLaDeselecciona() {
        val viewModel = crearViewModel()

        viewModel.alElegirDulzor(Dulzor.DULCE)
        viewModel.alElegirDulzor(Dulzor.SECO)
        assertEquals(Dulzor.SECO, viewModel.camposActuales().dulzor)

        viewModel.alElegirDulzor(Dulzor.SECO)
        assertNull(viewModel.camposActuales().dulzor)
    }

    @Test
    fun elAnioAceptaSoloCuatroDigitos() {
        val viewModel = crearViewModel()

        viewModel.alCambiarAnio("20a2b1c9")

        assertEquals("2021", viewModel.camposActuales().anio)
    }
}
