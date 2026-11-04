package com.sorbo.app.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.sorbo.app.data.memoria.VinoRepositoryEnMemoria
import com.sorbo.app.domain.AHORA
import com.sorbo.app.domain.HOY
import com.sorbo.app.domain.degustacionDe
import com.sorbo.app.domain.model.Compania
import com.sorbo.app.domain.model.Comida
import com.sorbo.app.domain.model.Dulzor
import com.sorbo.app.domain.model.Ocasion
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
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class VolverAProbarViewModelTest {

    private val reloj = Clock.fixed(AHORA, ZoneOffset.UTC)
    private val repositorio = VinoRepositoryEnMemoria(reloj)

    @Before
    fun antes() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun despues() = Dispatchers.resetMain()

    private suspend fun guardar(nombre: String = "Rutini Malbec 2021"): String {
        val vino = vinoDe(nombre, bodega = "Rutini", anio = 2021)
        repositorio.guardarVinoNuevo(
            vino,
            degustacionDe(
                vino.id, meGusto = true, fecha = LocalDate.of(2026, 10, 1),
                dulzor = Dulzor.SECO, comida = Comida.CARNE, compania = Compania.AMIGOS, ocasion = Ocasion.ASADO,
            ),
        )
        return vino.id
    }

    private fun crearViewModel(vinoId: String, savedStateHandle: SavedStateHandle = SavedStateHandle()) = VolverAProbarViewModel(
        vinoId = vinoId,
        obtenerDetalleVino = ObtenerDetalleVino(repositorio),
        registrarDegustacion = RegistrarDegustacion(repositorio, reloj),
        reloj = reloj,
        savedStateHandle = savedStateHandle,
    )

    private fun VolverAProbarViewModel.editando() = estado.value as EstadoVolverAProbar.Editando

    @Test
    fun arrancaConLoQueSeEligioLaUltimaVezPeroSinOpinion() = runTest {
        val viewModel = crearViewModel(guardar())

        val estado = viewModel.editando()
        assertEquals(Dulzor.SECO, estado.campos.dulzor)
        assertEquals(Comida.CARNE, estado.campos.comida)
        assertEquals(Compania.AMIGOS, estado.campos.compania)
        assertEquals(Ocasion.ASADO, estado.campos.ocasion)
        assertNull(estado.campos.meGusto)
        assertEquals(HOY, estado.campos.fecha)
        assertTrue(estado.precargado)
    }

    @Test
    fun siLaUltimaVezNoTuvoDatosNoHayNadaPrecargado() = runTest {
        val vino = vinoDe("Sin datos")
        repositorio.guardarVinoNuevo(vino, degustacionDe(vino.id))

        val estado = crearViewModel(vino.id).editando()

        assertEquals(false, estado.precargado)
        assertNull(estado.campos.comida)
    }

    @Test
    fun laUltimaVezEsLaDegustacionMasReciente() = runTest {
        val id = guardar()
        repositorio.agregarDegustacion(
            degustacionDe(id, fecha = LocalDate.of(2026, 10, 20), dulzor = Dulzor.DULCE, comida = Comida.POSTRE),
        )

        val estado = crearViewModel(id).editando()

        assertEquals(Dulzor.DULCE, estado.campos.dulzor)
        assertEquals(Comida.POSTRE, estado.campos.comida)
    }

    @Test
    fun unVinoInexistenteSeMuestraComoNoEncontrado() = runTest {
        assertEquals(EstadoVolverAProbar.NoEncontrado, crearViewModel("no-existe").estado.value)
    }

    @Test
    fun sinOpinionNoGuardaNadaYAvisa() = runTest {
        val id = guardar()
        val viewModel = crearViewModel(id)

        viewModel.alGuardar()

        assertEquals(true, viewModel.editando().faltaOpinion)
        assertEquals(1, repositorio.observarVino(id).first()!!.veces)
    }

    @Test
    fun alElegirLaOpinionDejaDeAvisar() = runTest {
        val viewModel = crearViewModel(guardar())
        viewModel.alGuardar()

        viewModel.alElegirMeGusto(false)

        assertEquals(false, viewModel.editando().faltaOpinion)
    }

    @Test
    fun alGuardarSumaUnaDegustacionAlMismoVinoSinDuplicarlo() = runTest {
        val id = guardar()
        val viewModel = crearViewModel(id)
        viewModel.alElegirMeGusto(false)
        viewModel.alCambiarNota("Esta vez estaba tapado")

        viewModel.alGuardar()

        val guardado = viewModel.estado.value as EstadoVolverAProbar.Guardado
        assertEquals("Rutini Malbec 2021", guardado.nombreDelVino)
        val vino = repositorio.observarVino(id).first()!!
        assertEquals(2, vino.veces)
        assertEquals(1, repositorio.listarVinos().size)
        val nueva = vino.degustaciones.single { it.id == guardado.degustacionId }
        assertEquals(false, nueva.meGusto)
        assertEquals("Esta vez estaba tapado", nueva.nota)
        assertEquals(Comida.CARNE, nueva.comida)
    }

    @Test
    fun laDegustacionSeGuardaConLaFechaElegida() = runTest {
        val id = guardar()
        val viewModel = crearViewModel(id)
        viewModel.alElegirMeGusto(true)
        viewModel.alCambiarFecha(LocalDate.of(2026, 10, 25))

        viewModel.alGuardar()

        val fechas = repositorio.observarVino(id).first()!!.degustaciones.map { it.fecha }
        assertTrue(LocalDate.of(2026, 10, 25) in fechas)
    }

    @Test
    fun unaFechaDelFuturoSeTomaComoHoy() = runTest {
        val viewModel = crearViewModel(guardar())

        viewModel.alCambiarFecha(HOY.plusDays(3))

        assertEquals(HOY, viewModel.editando().campos.fecha)
    }

    @Test
    fun tocarDeNuevoUnaOpcionPrecargadaLaDeselecciona() = runTest {
        val viewModel = crearViewModel(guardar())

        viewModel.alElegirComida(Comida.CARNE)

        assertNull(viewModel.editando().campos.comida)
    }

    @Test
    fun siElVinoSeEliminaMientrasSeCompletaAlGuardarAvisa() = runTest {
        val id = guardar()
        val viewModel = crearViewModel(id)
        viewModel.alElegirMeGusto(true)
        repositorio.eliminarVino(id)

        viewModel.alGuardar()

        assertEquals(EstadoVolverAProbar.NoEncontrado, viewModel.estado.value)
    }

    @Test
    fun siElSistemaMataLaAppSeRetomaLoQueEstabaCargado() = runTest {
        val id = guardar()
        val guardado = SavedStateHandle()
        val antes = crearViewModel(id, guardado)
        antes.alElegirMeGusto(false)
        antes.alElegirComida(Comida.PIZZA)
        antes.alCambiarNota("Con los chicos")
        antes.alCambiarFecha(LocalDate.of(2026, 10, 25))

        val despues = crearViewModel(id, guardado)

        val campos = despues.editando().campos
        assertEquals(false, campos.meGusto)
        assertEquals(Comida.PIZZA, campos.comida)
        assertEquals("Con los chicos", campos.nota)
        assertEquals(LocalDate.of(2026, 10, 25), campos.fecha)
    }

    @Test
    fun alGuardarNoSeRetomaNadaDelFormularioAnterior() = runTest {
        val id = guardar()
        val guardado = SavedStateHandle()
        val antes = crearViewModel(id, guardado)
        antes.alElegirMeGusto(true)
        antes.alGuardar()

        assertNull(guardado.get<CamposDegustacion>("campos_degustacion"))
        assertNull(crearViewModel(id, guardado).editando().campos.meGusto)
    }
}
