package com.sorbo.app.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.sorbo.app.data.memoria.VinoRepositoryEnMemoria
import com.sorbo.app.domain.AHORA
import com.sorbo.app.domain.degustacionDe
import com.sorbo.app.domain.repository.AlmacenFotos
import com.sorbo.app.domain.model.Dulzor
import com.sorbo.app.domain.usecase.BuscarVinoParecido
import com.sorbo.app.domain.usecase.CampoObligatorio
import com.sorbo.app.domain.usecase.ObtenerDetalleVino
import com.sorbo.app.domain.usecase.RegistrarDegustacion
import com.sorbo.app.domain.vinoDe
import kotlinx.coroutines.CompletableDeferred
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
import java.io.IOException
import java.time.Clock
import java.time.ZoneOffset

/** Almacén que no toca el disco: anota qué fotos se guardaron y cuáles se borraron. */
private class AlmacenFotosFalso(var falla: Boolean = false) : AlmacenFotos {
    val guardadas = mutableListOf<String>()
    val borradas = mutableListOf<String>()

    /** Si tiene valor, [guardar] espera a que se complete: sirve para probar el momento "procesando la foto". */
    var puerta: CompletableDeferred<Unit>? = null

    override suspend fun guardar(origen: String): Result<String> {
        puerta?.await()
        if (falla) return Result.failure(IOException("no se pudo leer"))
        return Result.success("fotos/${guardadas.size + 1}.jpg").also { guardadas += it.getOrThrow() }
    }

    override fun borrar(ruta: String) {
        borradas += ruta
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class RegistroViewModelTest {

    private val reloj = Clock.fixed(AHORA, ZoneOffset.UTC)
    private val repositorio = VinoRepositoryEnMemoria(reloj)
    private val almacen = AlmacenFotosFalso()

    @Before
    fun antes() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun despues() = Dispatchers.resetMain()

    private fun crearViewModel(
        nombreInicial: String? = null,
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ) = RegistroViewModel(
        buscarVinoParecido = BuscarVinoParecido(repositorio),
        registrarDegustacion = RegistrarDegustacion(repositorio, reloj),
        obtenerDetalleVino = ObtenerDetalleVino(repositorio),
        almacenFotos = almacen,
        reloj = reloj,
        savedStateHandle = savedStateHandle,
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

    @Test
    fun alElegirUnaFotoLaGuardaYLaMuestra() = runTest {
        val viewModel = crearViewModel()

        viewModel.alElegirFoto("content://galeria/1")

        val campos = viewModel.camposActuales()
        assertEquals("fotos/1.jpg", campos.fotoPath)
        assertEquals(false, campos.procesandoFoto)
        assertEquals(false, campos.errorFoto)
    }

    @Test
    fun siLaFotoFallaAvisaYNoPierdeLoCargado() = runTest {
        almacen.falla = true
        val viewModel = crearViewModel()
        viewModel.alCambiarNombre("Cheval Blanc")

        viewModel.alElegirFoto("content://galeria/1")

        val campos = viewModel.camposActuales()
        assertEquals(true, campos.errorFoto)
        assertNull(campos.fotoPath)
        assertEquals("Cheval Blanc", campos.nombre)
    }

    @Test
    fun alCambiarLaFotoBorraLaAnterior() = runTest {
        val viewModel = crearViewModel()
        viewModel.alElegirFoto("content://galeria/1")

        viewModel.alElegirFoto("content://galeria/2")

        assertEquals("fotos/2.jpg", viewModel.camposActuales().fotoPath)
        assertEquals(listOf("fotos/1.jpg"), almacen.borradas)
    }

    @Test
    fun alQuitarLaFotoBorraElArchivo() = runTest {
        val viewModel = crearViewModel()
        viewModel.alElegirFoto("content://galeria/1")

        viewModel.alQuitarFoto()

        assertNull(viewModel.camposActuales().fotoPath)
        assertEquals(listOf("fotos/1.jpg"), almacen.borradas)
    }

    @Test
    fun unVinoNuevoQuedaConLaFotoElegida() = runTest {
        val viewModel = crearViewModel()
        viewModel.alCambiarNombre("Cheval Blanc")
        viewModel.alElegirMeGusto(true)
        viewModel.alElegirFoto("content://galeria/1")

        viewModel.alGuardar()

        assertEquals("fotos/1.jpg", repositorio.listarVinos().single().fotoPath)
        assertTrue(almacen.borradas.isEmpty())
    }

    @Test
    fun siEsElMismoVinoLaFotoDeEstaVezSeDescarta() = runTest {
        guardarEnDiario("Rutini Malbec 2021", anio = 2021)
        val viewModel = crearViewModel()
        viewModel.alCambiarNombre("Rutini Malbec 2021")
        viewModel.alElegirMeGusto(true)
        viewModel.alElegirFoto("content://galeria/1")

        viewModel.alGuardar()

        assertNull(repositorio.listarVinos().single().fotoPath)
        assertEquals(listOf("fotos/1.jpg"), almacen.borradas)
    }

    @Test
    fun mientrasProcesaLaFotoNoSePuedeGuardar() = runTest {
        val puerta = CompletableDeferred<Unit>()
        almacen.puerta = puerta
        val viewModel = crearViewModel()
        viewModel.alCambiarNombre("Cheval Blanc")
        viewModel.alElegirMeGusto(true)

        viewModel.alElegirFoto("content://galeria/1")
        assertEquals(true, viewModel.camposActuales().procesandoFoto)
        viewModel.alGuardar()

        assertTrue(viewModel.estado.value is EstadoRegistro.Editando)
        assertTrue(repositorio.listarVinos().isEmpty())

        puerta.complete(Unit)
        viewModel.alGuardar()

        assertEquals("fotos/1.jpg", repositorio.listarVinos().single().fotoPath)
    }

    @Test
    fun alCerrarElFormularioSinGuardarSeBorraLaFoto() = runTest {
        val viewModel = crearViewModel()
        viewModel.alElegirFoto("content://galeria/1")

        viewModel.alCerrar()

        assertEquals(listOf("fotos/1.jpg"), almacen.borradas)
    }

    @Test
    fun alCerrarDespuesDeGuardarNoBorraLaFotoDelVino() = runTest {
        val viewModel = crearViewModel()
        viewModel.alCambiarNombre("Cheval Blanc")
        viewModel.alElegirMeGusto(true)
        viewModel.alElegirFoto("content://galeria/1")
        viewModel.alGuardar()

        viewModel.alCerrar()

        assertTrue(almacen.borradas.isEmpty())
    }

    @Test
    fun siElSistemaMataLaAppSeRetomaLoQueEstabaCargado() = runTest {
        val guardado = SavedStateHandle()
        val antes = crearViewModel(savedStateHandle = guardado)
        antes.alCambiarNombre("Cheval Blanc")
        antes.alElegirMeGusto(true)
        antes.alElegirDulzor(Dulzor.SECO)
        antes.alElegirFoto("content://camara/1")

        val despues = crearViewModel(savedStateHandle = guardado)

        val campos = despues.camposActuales()
        assertEquals("Cheval Blanc", campos.nombre)
        assertEquals(true, campos.meGusto)
        assertEquals(Dulzor.SECO, campos.dulzor)
        assertEquals("fotos/1.jpg", campos.fotoPath)
    }

    @Test
    fun loGuardadoGanaSobreElNombreQueVeniaDeLaBusqueda() = runTest {
        val guardado = SavedStateHandle()
        crearViewModel(nombreInicial = "xyz", savedStateHandle = guardado).alCambiarNombre("Cheval Blanc")

        val despues = crearViewModel(nombreInicial = "xyz", savedStateHandle = guardado)

        assertEquals("Cheval Blanc", despues.camposActuales().nombre)
    }

    @Test
    fun unaFotoAMedioProcesarNoDejaTrabadoElFormularioAlRetomarlo() = runTest {
        val guardado = SavedStateHandle()
        val puerta = CompletableDeferred<Unit>()
        almacen.puerta = puerta
        val antes = crearViewModel(savedStateHandle = guardado)
        antes.alCambiarNombre("Cheval Blanc")
        antes.alElegirMeGusto(true)
        antes.alElegirFoto("content://camara/1")
        assertEquals(true, antes.camposActuales().procesandoFoto)

        val despues = crearViewModel(savedStateHandle = guardado)

        assertEquals(false, despues.camposActuales().procesandoFoto)
        despues.alGuardar()
        assertTrue(despues.estado.value is EstadoRegistro.Guardado)
    }

    @Test
    fun alGuardarElVinoNoSeRetomaNadaDelFormularioAnterior() = runTest {
        val guardado = SavedStateHandle()
        val antes = crearViewModel(savedStateHandle = guardado)
        antes.alCambiarNombre("Cheval Blanc")
        antes.alElegirMeGusto(true)
        antes.alGuardar()

        val despues = crearViewModel(savedStateHandle = guardado)

        assertEquals("", despues.camposActuales().nombre)
    }

    @Test
    fun alVolverAEditarTrasLaPreguntaSeRetomaLoCargado() = runTest {
        guardarEnDiario("Rutini Malbec 2021", anio = 2021)
        val guardado = SavedStateHandle()
        val antes = crearViewModel(savedStateHandle = guardado)
        antes.alCambiarNombre("Rutini Malbek 2021")
        antes.alElegirMeGusto(true)
        antes.alGuardar()
        assertTrue(antes.estado.value is EstadoRegistro.PreguntandoMismoVino)

        val despues = crearViewModel(savedStateHandle = guardado)

        assertEquals("Rutini Malbek 2021", despues.camposActuales().nombre)
        assertEquals(1, repositorio.listarVinos().size)
    }
}
