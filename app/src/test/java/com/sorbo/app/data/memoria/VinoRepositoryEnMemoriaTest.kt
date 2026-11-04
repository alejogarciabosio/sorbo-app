package com.sorbo.app.data.memoria

import com.sorbo.app.domain.AHORA
import com.sorbo.app.domain.degustacionDe
import com.sorbo.app.domain.model.EstadoSync
import com.sorbo.app.domain.vinoDe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

class VinoRepositoryEnMemoriaTest {

    private val reloj = Clock.fixed(AHORA, ZoneOffset.UTC)
    private val repositorio = VinoRepositoryEnMemoria(reloj)

    @Test
    fun guardarUnVinoNuevoLoMuestraEnElDiario() = runTest {
        val vino = vinoDe("Rutini Malbec")
        repositorio.guardarVinoNuevo(vino, degustacionDe(vino.id))

        val diario = repositorio.observarDiario().first()
        assertEquals(1, diario.size)
        assertEquals(1, diario[0].veces)
    }

    @Test
    fun elDiarioMuestraPrimeroElProbadoMasReciente() = runTest {
        val viejo = vinoDe("Viejo")
        val nuevo = vinoDe("Nuevo")
        repositorio.guardarVinoNuevo(viejo, degustacionDe(viejo.id, fecha = LocalDate.of(2026, 1, 1)))
        repositorio.guardarVinoNuevo(nuevo, degustacionDe(nuevo.id, fecha = LocalDate.of(2026, 10, 1)))

        assertEquals(listOf("Nuevo", "Viejo"), repositorio.observarDiario().first().map { it.vino.nombre })
    }

    @Test
    fun agregarUnaDegustacionNoDuplicaElVino() = runTest {
        val vino = vinoDe("Rutini Malbec")
        repositorio.guardarVinoNuevo(vino, degustacionDe(vino.id))
        repositorio.agregarDegustacion(degustacionDe(vino.id, meGusto = false))

        val diario = repositorio.observarDiario().first()
        assertEquals(1, diario.size)
        assertEquals(2, diario[0].veces)
    }

    @Test
    fun eliminarUnaDegustacionDejaElVinoYLasDemasDegustaciones() = runTest {
        val vino = vinoDe("Rutini Malbec")
        val primera = degustacionDe(vino.id, fecha = LocalDate.of(2026, 10, 1))
        val segunda = degustacionDe(vino.id, fecha = LocalDate.of(2026, 11, 3))
        repositorio.guardarVinoNuevo(vino, primera)
        repositorio.agregarDegustacion(segunda)

        repositorio.eliminarDegustacion(segunda.id)

        val guardado = repositorio.observarVino(vino.id).first()!!
        assertEquals(listOf(primera.id), guardado.degustaciones.map { it.id })
        assertEquals(1, repositorio.listarVinos().size)
    }

    @Test
    fun eliminarUnaDegustacionInexistenteNoHaceNada() = runTest {
        val vino = vinoDe("Rutini Malbec")
        repositorio.guardarVinoNuevo(vino, degustacionDe(vino.id))

        repositorio.eliminarDegustacion("no-existe")

        assertEquals(1, repositorio.observarVino(vino.id).first()!!.veces)
    }

    @Test(expected = IllegalArgumentException::class)
    fun agregarUnaDegustacionAUnVinoInexistenteFalla() = runTest {
        repositorio.agregarDegustacion(degustacionDe("no-existe"))
    }

    @Test
    fun buscarIgnoraTildesYMayusculas() = runTest {
        val vino = vinoDe("El Esteco Torrontés")
        repositorio.guardarVinoNuevo(vino, degustacionDe(vino.id))

        assertEquals(1, repositorio.buscar("torrontes").size)
        assertEquals(1, repositorio.buscar("esteco").size)
        assertTrue(repositorio.buscar("malbec").isEmpty())
    }

    @Test
    fun eliminarOcultaElVinoYRestaurarLoDevuelve() = runTest {
        val vino = vinoDe("Rutini Malbec")
        repositorio.guardarVinoNuevo(vino, degustacionDe(vino.id))

        repositorio.eliminarVino(vino.id)
        assertTrue(repositorio.observarDiario().first().isEmpty())
        assertNull(repositorio.observarVino(vino.id).first())
        assertTrue(repositorio.listarVinos().isEmpty())

        repositorio.restaurarVino(vino.id)
        assertEquals(1, repositorio.observarDiario().first().size)
        assertEquals(1, repositorio.observarVino(vino.id).first()?.veces)
    }

    @Test
    fun cualquierCambioDejaElVinoPendienteDeRespaldo() = runTest {
        val vino = vinoDe("Rutini Malbec").copy(estadoSync = EstadoSync.SINCRONIZADO)
        repositorio.guardarVinoNuevo(vino, degustacionDe(vino.id))

        repositorio.actualizarVino(vino.copy(nombre = "Rutini Malbec Reserva"))

        val guardado = repositorio.listarVinos().single()
        assertEquals("Rutini Malbec Reserva", guardado.nombre)
        assertEquals(EstadoSync.PENDIENTE, guardado.estadoSync)
    }
}
