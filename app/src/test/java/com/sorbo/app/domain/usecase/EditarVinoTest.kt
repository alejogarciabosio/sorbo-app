package com.sorbo.app.domain.usecase

import com.sorbo.app.data.memoria.VinoRepositoryEnMemoria
import com.sorbo.app.domain.AHORA
import com.sorbo.app.domain.degustacionDe
import com.sorbo.app.domain.model.EstadoSync
import com.sorbo.app.domain.vinoDe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class EditarVinoTest {

    private val reloj = Clock.fixed(AHORA.plusSeconds(60), ZoneOffset.UTC)
    private val repositorio = VinoRepositoryEnMemoria(reloj)
    private val editar = EditarVino(repositorio)

    private suspend fun guardar(nombre: String = "Rutini Malbek", fotoPath: String? = null): String {
        val vino = vinoDe(nombre, bodega = "Rutini", anio = 2021, cepa = "Malbec").copy(fotoPath = fotoPath, estadoSync = EstadoSync.SINCRONIZADO)
        repositorio.guardarVinoNuevo(vino, degustacionDe(vino.id))
        return vino.id
    }

    private suspend fun vinoGuardado(id: String) = repositorio.observarVino(id).first()!!

    @Test
    fun corregirElNombreCambiaElMismoVino() = runTest {
        val id = guardar("Rutini Malbek")

        val resultado = editar(id, DatosDelVino(nombre = "  Rutini Malbec  ", bodega = "Rutini", cepa = "Malbec", anio = 2021))

        assertEquals(ResultadoEdicion.Guardado(fotoAnterior = null), resultado)
        assertEquals("Rutini Malbec", vinoGuardado(id).vino.nombre)
        assertEquals(1, repositorio.listarVinos().size)
    }

    @Test
    fun sinNombreNoGuardaNadaYAvisa() = runTest {
        val id = guardar("Rutini Malbek")

        val resultado = editar(id, DatosDelVino(nombre = "   "))

        assertEquals(ResultadoEdicion.NombreVacio, resultado)
        assertEquals("Rutini Malbek", vinoGuardado(id).vino.nombre)
    }

    @Test
    fun losCamposVaciosQuedanSinCargar() = runTest {
        val id = guardar()

        editar(id, DatosDelVino(nombre = "Rutini Malbec", bodega = "  ", cepa = "", anio = null))

        val vino = vinoGuardado(id).vino
        assertNull(vino.bodega)
        assertNull(vino.cepa)
        assertNull(vino.anio)
    }

    @Test
    fun siElNombreTraeElAnioYNoSeEscribioUnoSeTomaDelNombre() = runTest {
        val id = guardar()

        editar(id, DatosDelVino(nombre = "Rutini Malbec 2022", anio = null))

        assertEquals(2022, vinoGuardado(id).vino.anio)
    }

    @Test
    fun editarNoTocaLasDegustaciones() = runTest {
        val id = guardar()
        repositorio.agregarDegustacion(degustacionDe(id, meGusto = false))

        editar(id, DatosDelVino(nombre = "Rutini Malbec"))

        assertEquals(2, vinoGuardado(id).veces)
    }

    @Test
    fun alEditarQuedaPendienteDeRespaldoYConLaFechaDeLaEdicion() = runTest {
        val id = guardar()

        editar(id, DatosDelVino(nombre = "Rutini Malbec"))

        val vino = vinoGuardado(id).vino
        assertEquals(EstadoSync.PENDIENTE, vino.estadoSync)
        assertEquals(Instant.parse("2026-11-03T12:01:00Z"), vino.actualizadoEn)
    }

    @Test
    fun alCambiarLaFotoDevuelveLaAnteriorParaQueSeBorre() = runTest {
        val id = guardar(fotoPath = "fotos/vieja.jpg")

        val resultado = editar(id, DatosDelVino(nombre = "Rutini Malbek", fotoPath = "fotos/nueva.jpg"))

        assertEquals(ResultadoEdicion.Guardado(fotoAnterior = "fotos/vieja.jpg"), resultado)
        assertEquals("fotos/nueva.jpg", vinoGuardado(id).vino.fotoPath)
    }

    @Test
    fun siLaFotoNoCambiaNoHayNadaParaBorrar() = runTest {
        val id = guardar(fotoPath = "fotos/vieja.jpg")

        val resultado = editar(id, DatosDelVino(nombre = "Rutini Malbek", fotoPath = "fotos/vieja.jpg"))

        assertEquals(ResultadoEdicion.Guardado(fotoAnterior = null), resultado)
    }

    @Test
    fun alQuitarLaFotoSeDevuelveLaAnteriorParaBorrarla() = runTest {
        val id = guardar(fotoPath = "fotos/vieja.jpg")

        val resultado = editar(id, DatosDelVino(nombre = "Rutini Malbek", fotoPath = null))

        assertEquals(ResultadoEdicion.Guardado(fotoAnterior = "fotos/vieja.jpg"), resultado)
        assertNull(vinoGuardado(id).vino.fotoPath)
    }

    @Test
    fun unVinoEliminadoNoSePuedeEditar() = runTest {
        val id = guardar()
        repositorio.eliminarVino(id)

        assertEquals(ResultadoEdicion.NoExiste, editar(id, DatosDelVino(nombre = "Rutini Malbec")))
    }
}
