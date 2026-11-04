package com.sorbo.app.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.sorbo.app.data.memoria.VinoRepositoryEnMemoria
import com.sorbo.app.domain.AHORA
import com.sorbo.app.domain.degustacionDe
import com.sorbo.app.domain.usecase.EditarVino
import com.sorbo.app.domain.usecase.ObtenerDetalleVino
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
import java.time.Clock
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class EditarViewModelTest {

    private val reloj = Clock.fixed(AHORA, ZoneOffset.UTC)
    private val repositorio = VinoRepositoryEnMemoria(reloj)
    private val almacen = AlmacenFotosFalso()

    @Before
    fun antes() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun despues() = Dispatchers.resetMain()

    private suspend fun guardar(nombre: String = "Rutini Malbek", fotoPath: String? = null): String {
        val vino = vinoDe(nombre, bodega = "Rutini", anio = 2021, cepa = "Malbec").copy(fotoPath = fotoPath)
        repositorio.guardarVinoNuevo(vino, degustacionDe(vino.id))
        return vino.id
    }

    private fun crearViewModel(vinoId: String, savedStateHandle: SavedStateHandle = SavedStateHandle()) = EditarViewModel(
        vinoId = vinoId,
        obtenerDetalleVino = ObtenerDetalleVino(repositorio),
        editarVino = EditarVino(repositorio),
        almacenFotos = almacen,
        savedStateHandle = savedStateHandle,
    )

    private fun EditarViewModel.campos() = (estado.value as EstadoEdicion.Editando).campos

    @Test
    fun arrancaConLosDatosActualesDelVino() = runTest {
        val viewModel = crearViewModel(guardar("Rutini Malbek"))

        val campos = viewModel.campos()
        assertEquals("Rutini Malbek", campos.nombre)
        assertEquals("Rutini", campos.bodega)
        assertEquals("Malbec", campos.cepa)
        assertEquals("2021", campos.anio)
    }

    @Test
    fun unVinoInexistenteSeMuestraComoNoEncontrado() = runTest {
        assertEquals(EstadoEdicion.NoEncontrado, crearViewModel("no-existe").estado.value)
    }

    @Test
    fun alGuardarLaCorreccionCambiaElMismoVino() = runTest {
        val id = guardar("Rutini Malbek")
        val viewModel = crearViewModel(id)

        viewModel.alCambiarNombre("Rutini Malbec")
        viewModel.alGuardar()

        assertEquals(EstadoEdicion.Guardado, viewModel.estado.value)
        assertEquals("Rutini Malbec", repositorio.observarVino(id).first()!!.vino.nombre)
        assertEquals(1, repositorio.listarVinos().size)
    }

    @Test
    fun sinNombreAvisaYNoGuardaYAlEscribirDejaDeAvisar() = runTest {
        val id = guardar("Rutini Malbek")
        val viewModel = crearViewModel(id)
        viewModel.alCambiarNombre("")

        viewModel.alGuardar()

        assertEquals(true, (viewModel.estado.value as EstadoEdicion.Editando).nombreVacio)
        assertEquals("Rutini Malbek", repositorio.observarVino(id).first()!!.vino.nombre)

        viewModel.alCambiarNombre("R")
        assertEquals(false, (viewModel.estado.value as EstadoEdicion.Editando).nombreVacio)
    }

    @Test
    fun elAnioAceptaSoloCuatroDigitos() = runTest {
        val viewModel = crearViewModel(guardar())

        viewModel.alCambiarAnio("20a2b2c3d9")

        assertEquals("2022", viewModel.campos().anio)
    }

    @Test
    fun alElegirUnaFotoNuevaLaOriginalSeConservaHastaGuardar() = runTest {
        val id = guardar(fotoPath = "fotos/vieja.jpg")
        val viewModel = crearViewModel(id)

        viewModel.alElegirFoto("content://galeria/1")

        assertEquals("fotos/1.jpg", viewModel.campos().fotoPath)
        assertTrue(almacen.borradas.isEmpty())
        assertEquals("fotos/vieja.jpg", repositorio.observarVino(id).first()!!.vino.fotoPath)
    }

    @Test
    fun alGuardarConFotoNuevaSeBorraLaVieja() = runTest {
        val id = guardar(fotoPath = "fotos/vieja.jpg")
        val viewModel = crearViewModel(id)
        viewModel.alElegirFoto("content://galeria/1")

        viewModel.alGuardar()

        assertEquals(listOf("fotos/vieja.jpg"), almacen.borradas)
        assertEquals("fotos/1.jpg", repositorio.observarVino(id).first()!!.vino.fotoPath)
    }

    @Test
    fun alElegirOtraFotoNuevaSeBorraLaNuevaAnteriorPeroNoLaOriginal() = runTest {
        val viewModel = crearViewModel(guardar(fotoPath = "fotos/vieja.jpg"))
        viewModel.alElegirFoto("content://galeria/1")

        viewModel.alElegirFoto("content://galeria/2")

        assertEquals("fotos/2.jpg", viewModel.campos().fotoPath)
        assertEquals(listOf("fotos/1.jpg"), almacen.borradas)
    }

    @Test
    fun alCerrarSinGuardarSeDescartaLaFotoNuevaYSeConservaLaOriginal() = runTest {
        val id = guardar(fotoPath = "fotos/vieja.jpg")
        val viewModel = crearViewModel(id)
        viewModel.alElegirFoto("content://galeria/1")

        viewModel.alCerrar()

        assertEquals(listOf("fotos/1.jpg"), almacen.borradas)
        assertEquals("fotos/vieja.jpg", repositorio.observarVino(id).first()!!.vino.fotoPath)
    }

    @Test
    fun alCerrarSinCambiarLaFotoNoSeBorraNada() = runTest {
        val viewModel = crearViewModel(guardar(fotoPath = "fotos/vieja.jpg"))

        viewModel.alCerrar()

        assertTrue(almacen.borradas.isEmpty())
    }

    @Test
    fun alQuitarLaFotoOriginalSeBorraRecienAlGuardar() = runTest {
        val id = guardar(fotoPath = "fotos/vieja.jpg")
        val viewModel = crearViewModel(id)

        viewModel.alQuitarFoto()
        assertNull(viewModel.campos().fotoPath)
        assertTrue(almacen.borradas.isEmpty())

        viewModel.alGuardar()
        assertEquals(listOf("fotos/vieja.jpg"), almacen.borradas)
        assertNull(repositorio.observarVino(id).first()!!.vino.fotoPath)
    }

    @Test
    fun siLaFotoFallaAvisaYNoPierdeLoCargado() = runTest {
        almacen.falla = true
        val viewModel = crearViewModel(guardar("Rutini Malbek"))
        viewModel.alCambiarNombre("Rutini Malbec")

        viewModel.alElegirFoto("content://galeria/1")

        assertEquals(true, viewModel.campos().errorFoto)
        assertEquals("Rutini Malbec", viewModel.campos().nombre)
    }

    @Test
    fun mientrasProcesaLaFotoNoSePuedeGuardar() = runTest {
        val id = guardar("Rutini Malbek")
        val puerta = CompletableDeferred<Unit>()
        almacen.puerta = puerta
        val viewModel = crearViewModel(id)
        viewModel.alCambiarNombre("Rutini Malbec")

        viewModel.alElegirFoto("content://galeria/1")
        viewModel.alGuardar()
        assertTrue(viewModel.estado.value is EstadoEdicion.Editando)
        assertEquals("Rutini Malbek", repositorio.observarVino(id).first()!!.vino.nombre)

        puerta.complete(Unit)
        viewModel.alGuardar()
        assertEquals(EstadoEdicion.Guardado, viewModel.estado.value)
    }

    @Test
    fun siElVinoSeEliminaMientrasSeEditaAlGuardarAvisaYDescartaLaFoto() = runTest {
        val id = guardar()
        val viewModel = crearViewModel(id)
        viewModel.alElegirFoto("content://galeria/1")
        repositorio.eliminarVino(id)

        viewModel.alGuardar()

        assertEquals(EstadoEdicion.NoEncontrado, viewModel.estado.value)
        assertEquals(listOf("fotos/1.jpg"), almacen.borradas)
    }

    @Test
    fun siElSistemaMataLaAppSeRetomaLoQueEstabaCargado() = runTest {
        val id = guardar("Rutini Malbek", fotoPath = "fotos/vieja.jpg")
        val guardado = SavedStateHandle()
        val antes = crearViewModel(id, guardado)
        antes.alCambiarNombre("Rutini Malbec")
        antes.alElegirFoto("content://galeria/1")

        val despues = crearViewModel(id, guardado)

        val campos = despues.campos()
        assertEquals("Rutini Malbec", campos.nombre)
        assertEquals("fotos/1.jpg", campos.fotoPath)
        assertEquals("fotos/vieja.jpg", campos.fotoOriginal)
    }

    @Test
    fun alGuardarNoSeRetomaNadaDeLaEdicionAnterior() = runTest {
        val id = guardar("Rutini Malbek")
        val guardado = SavedStateHandle()
        val antes = crearViewModel(id, guardado)
        antes.alCambiarNombre("Rutini Malbec")
        antes.alGuardar()

        assertNull(guardado.get<CamposEdicion>("campos_edicion"))
        // Un formulario nuevo parte de lo que quedó guardado en el vino, no de la edición anterior.
        assertEquals("Rutini Malbec", crearViewModel(id, guardado).campos().nombre)
    }
}
