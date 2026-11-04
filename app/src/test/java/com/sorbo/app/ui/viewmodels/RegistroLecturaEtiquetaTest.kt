// Pruebas · ViewModel de Registrar: lectura de la etiqueta (RF03) con un lector falso, sin ML Kit.
package com.sorbo.app.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.sorbo.app.data.memoria.VinoRepositoryEnMemoria
import com.sorbo.app.domain.AHORA
import com.sorbo.app.domain.usecase.BuscarVinoParecido
import com.sorbo.app.domain.usecase.ObtenerDetalleVino
import com.sorbo.app.domain.usecase.RegistrarDegustacion
import com.sorbo.app.domain.usecase.SugerirDatosDesdeFoto
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class RegistroLecturaEtiquetaTest {

    private val reloj = Clock.fixed(AHORA, ZoneOffset.UTC)
    private val repositorio = VinoRepositoryEnMemoria(reloj)
    private val almacen = AlmacenFotosFalso()
    private val lector = LectorEtiquetaFalso()

    @Before
    fun antes() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun despues() = Dispatchers.resetMain()

    private fun crearViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) = RegistroViewModel(
        buscarVinoParecido = BuscarVinoParecido(repositorio),
        registrarDegustacion = RegistrarDegustacion(repositorio, reloj),
        obtenerDetalleVino = ObtenerDetalleVino(repositorio),
        almacenFotos = almacen,
        sugerirDatosDesdeFoto = SugerirDatosDesdeFoto(lector),
        reloj = reloj,
        savedStateHandle = savedStateHandle,
    )

    private fun RegistroViewModel.campos() = (estado.value as EstadoRegistro.Editando).campos

    @Test
    fun alTenerLaFotoCompletaLosCuatroCamposComoSugeridos() {
        val viewModel = crearViewModel()

        viewModel.alElegirFoto("origen")

        val campos = viewModel.campos()
        assertEquals("RUTINI", campos.nombre)
        assertEquals("Rutini Wines", campos.bodega)
        assertEquals("Malbec", campos.cepa)
        assertEquals("2021", campos.anio)
        assertEquals(CampoSugerible.entries.toSet(), campos.sugeridos)
        assertEquals(LecturaEtiqueta.Inactiva, campos.lectura)
    }

    @Test
    fun mientrasLeeMuestraLeyendoYNoBloqueaElFormularioNiElGuardado() {
        val viewModel = crearViewModel()
        lector.puerta = CompletableDeferred()

        viewModel.alElegirFoto("origen")

        assertEquals(LecturaEtiqueta.Leyendo, viewModel.campos().lectura)
        assertTrue(!viewModel.campos().procesandoFoto)
        viewModel.alCambiarNota("escribo mientras lee")
        viewModel.alElegirMeGusto(true)
        viewModel.alCambiarNombre("Mi vino")
        viewModel.alGuardar()
        assertTrue(viewModel.estado.value is EstadoRegistro.Guardado)
    }

    @Test
    fun soloCompletaLosCamposVaciosYNoPisaLoEscrito() {
        val viewModel = crearViewModel()
        viewModel.alCambiarNombre("Mi Malbec favorito")
        viewModel.alCambiarCepa("Blend")

        viewModel.alElegirFoto("origen")

        val campos = viewModel.campos()
        assertEquals("Mi Malbec favorito", campos.nombre)
        assertEquals("Blend", campos.cepa)
        assertEquals("Rutini Wines", campos.bodega)
        assertEquals("2021", campos.anio)
        assertEquals(setOf(CampoSugerible.BODEGA, CampoSugerible.ANIO), campos.sugeridos)
    }

    @Test
    fun loQueEscribeElUsuarioMientrasLeeNoSePisa() {
        val viewModel = crearViewModel()
        val puerta = CompletableDeferred<Unit>()
        lector.puerta = puerta
        viewModel.alElegirFoto("origen")

        viewModel.alCambiarBodega("Mi bodega")
        puerta.complete(Unit)

        assertEquals("Mi bodega", viewModel.campos().bodega)
        assertEquals("RUTINI", viewModel.campos().nombre)
        assertTrue(CampoSugerible.BODEGA !in viewModel.campos().sugeridos)
    }

    @Test
    fun editarUnCampoSugeridoLeQuitaLaMarca() {
        val viewModel = crearViewModel()
        viewModel.alElegirFoto("origen")

        viewModel.alCambiarNombre("Rutini Malbec")

        assertEquals(setOf(CampoSugerible.BODEGA, CampoSugerible.CEPA, CampoSugerible.ANIO), viewModel.campos().sugeridos)
        viewModel.alCambiarAnio("2020")
        assertTrue(CampoSugerible.ANIO !in viewModel.campos().sugeridos)
    }

    @Test
    fun siLaLecturaFallaAvisaConservaLoCargadoYReintentaLuego() {
        val viewModel = crearViewModel()
        lector.falla = true
        viewModel.alCambiarNota("mi nota")

        viewModel.alElegirFoto("origen")

        assertEquals(LecturaEtiqueta.Fallo, viewModel.campos().lectura)
        assertEquals("mi nota", viewModel.campos().nota)
        assertEquals("", viewModel.campos().nombre)
        assertEquals("fotos/1.jpg", viewModel.campos().fotoPath)

        lector.falla = false
        viewModel.alReintentarLectura()

        assertEquals("RUTINI", viewModel.campos().nombre)
        assertEquals(LecturaEtiqueta.Inactiva, viewModel.campos().lectura)
        assertEquals("mi nota", viewModel.campos().nota)
    }

    @Test
    fun siNoEncuentraDatosLoDiceYPermiteReintentar() {
        val viewModel = crearViewModel()
        lector.texto = ETIQUETA_SIN_TEXTO

        viewModel.alElegirFoto("origen")

        assertEquals(LecturaEtiqueta.SinDatos, viewModel.campos().lectura)
        assertTrue(viewModel.campos().sugeridos.isEmpty())
        lector.texto = ETIQUETA_RUTINI
        viewModel.alReintentarLectura()
        assertEquals("RUTINI", viewModel.campos().nombre)
    }

    @Test
    fun siCambiaLaFotoMientrasLeeElResultadoViejoSeDescarta() {
        val viewModel = crearViewModel()
        val puertaVieja = CompletableDeferred<Unit>()
        lector.puerta = puertaVieja
        viewModel.alElegirFoto("origen-1")
        lector.puerta = null
        lector.texto = ETIQUETA_SIN_TEXTO

        viewModel.alElegirFoto("origen-2")
        puertaVieja.complete(Unit)

        assertEquals("fotos/2.jpg", viewModel.campos().fotoPath)
        assertEquals(LecturaEtiqueta.SinDatos, viewModel.campos().lectura)
        assertEquals("", viewModel.campos().nombre)
        assertTrue(viewModel.campos().sugeridos.isEmpty())
    }

    @Test
    fun cambiarLaFotoVuelveALeerYReemplazaLoQueSeguiaSiendoSugerido() {
        val viewModel = crearViewModel()
        viewModel.alElegirFoto("origen-1")
        viewModel.alCambiarBodega("Bodega escrita")
        lector.texto = ETIQUETA_OTRA

        viewModel.alElegirFoto("origen-2")

        val campos = viewModel.campos()
        assertEquals("ZUCCARDI", campos.nombre)
        assertEquals("Bodega escrita", campos.bodega)
        assertEquals("2019", campos.anio)
    }

    @Test
    fun quitarLaFotoDescartaLaLecturaEnCursoYLoSugerido() {
        val viewModel = crearViewModel()
        val puerta = CompletableDeferred<Unit>()
        lector.puerta = puerta
        viewModel.alElegirFoto("origen")

        viewModel.alQuitarFoto()
        puerta.complete(Unit)

        val campos = viewModel.campos()
        assertEquals(LecturaEtiqueta.Inactiva, campos.lectura)
        assertEquals("", campos.nombre)
        assertTrue(campos.sugeridos.isEmpty())
    }

    @Test
    fun quitarLaFotoVaciaLoSugeridoPeroConservaLoEditado() {
        val viewModel = crearViewModel()
        viewModel.alElegirFoto("origen")
        viewModel.alCambiarNombre("Mi nombre")

        viewModel.alQuitarFoto()

        assertEquals("Mi nombre", viewModel.campos().nombre)
        assertEquals("", viewModel.campos().bodega)
        assertEquals("", viewModel.campos().anio)
    }

    @Test
    fun noSeGuardaNadaSinTocarGuardar() = runTest {
        val viewModel = crearViewModel()

        viewModel.alElegirFoto("origen")

        assertTrue(viewModel.estado.value is EstadoRegistro.Editando)
        assertTrue(repositorio.listarVinos().isEmpty())
    }

    @Test
    fun trasProcesoMuertoConLoSugeridoSeRecuperanLasMarcas() {
        val estadoGuardado = SavedStateHandle()
        val original = crearViewModel(estadoGuardado)
        original.alElegirFoto("origen")
        original.alCambiarNombre("Editado")

        val restaurado = crearViewModel(estadoGuardado)

        assertEquals("Editado", restaurado.campos().nombre)
        assertEquals(setOf(CampoSugerible.BODEGA, CampoSugerible.CEPA, CampoSugerible.ANIO), restaurado.campos().sugeridos)
    }

    @Test
    fun trasProcesoMuertoDuranteLaLecturaLaRelanzaConLaMismaFoto() {
        val estadoGuardado = SavedStateHandle()
        lector.puerta = CompletableDeferred()
        crearViewModel(estadoGuardado).alElegirFoto("origen")
        lector.puerta = null

        val restaurado = crearViewModel(estadoGuardado)

        assertEquals(listOf("fotos/1.jpg", "fotos/1.jpg"), lector.leidas)
        assertEquals("RUTINI", restaurado.campos().nombre)
        assertEquals(LecturaEtiqueta.Inactiva, restaurado.campos().lectura)
    }
}
