package com.sorbo.app.data

import com.sorbo.app.domain.AHORA
import com.sorbo.app.domain.degustacionDe
import com.sorbo.app.domain.model.CambiosPendientes
import com.sorbo.app.domain.model.EstadoSync
import com.sorbo.app.domain.repository.VinoRepository
import com.sorbo.app.domain.vinoDe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Batería de contrato de [VinoRepository]: los mismos tests corren contra cada implementación
 * (en memoria con JUnit común; Room como test instrumentado). Si las dos pasan, cambiar una por otra es seguro.
 */
abstract class VinoRepositoryContratoTest {

    /** Un momento posterior a [AHORA], para comprobar que cada escritura actualiza `actualizadoEn`. */
    protected val despues: Instant = AHORA.plusSeconds(3600)
    protected val reloj: Clock = Clock.fixed(despues, ZoneOffset.UTC)

    protected lateinit var repositorio: VinoRepository

    abstract fun crearRepositorio(reloj: Clock): VinoRepository

    @Before
    fun prepararRepositorio() {
        repositorio = crearRepositorio(reloj)
    }

    @Test
    fun guardarUnVinoNuevoLoMuestraEnElDiario() = runTest {
        val vino = vinoDe("Rutini Malbec")
        repositorio.guardarVinoNuevo(vino, degustacionDe(vino.id))

        val diario = repositorio.observarDiario().first()
        assertEquals(1, diario.size)
        assertEquals(1, diario[0].veces)
        assertEquals(vino, diario[0].vino)
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
    fun siSeProbaronElMismoDiaVaPrimeroElCreadoMasReciente() = runTest {
        val primero = vinoDe("Primero").copy(creadoEn = AHORA)
        val segundo = vinoDe("Segundo").copy(creadoEn = AHORA.plusSeconds(60))
        repositorio.guardarVinoNuevo(segundo, degustacionDe(segundo.id))
        repositorio.guardarVinoNuevo(primero, degustacionDe(primero.id))

        assertEquals(listOf("Segundo", "Primero"), repositorio.observarDiario().first().map { it.vino.nombre })
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
    fun unaDegustacionNuevaMueveElVinoAlPrincipioDelDiario() = runTest {
        val a = vinoDe("A")
        val b = vinoDe("B")
        repositorio.guardarVinoNuevo(a, degustacionDe(a.id, fecha = LocalDate.of(2026, 1, 1)))
        repositorio.guardarVinoNuevo(b, degustacionDe(b.id, fecha = LocalDate.of(2026, 5, 1)))

        repositorio.agregarDegustacion(degustacionDe(a.id, fecha = LocalDate.of(2026, 9, 1)))

        assertEquals(listOf("A", "B"), repositorio.observarDiario().first().map { it.vino.nombre })
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

    @Test(expected = IllegalArgumentException::class)
    fun agregarUnaDegustacionAUnVinoEliminadoFalla() = runTest {
        val vino = vinoDe("Rutini Malbec")
        repositorio.guardarVinoNuevo(vino, degustacionDe(vino.id))
        repositorio.eliminarVino(vino.id)

        repositorio.agregarDegustacion(degustacionDe(vino.id))
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
    fun buscarExigeTodasLasPalabrasYNoMuestraEliminados() = runTest {
        val rutini = vinoDe("Rutini Malbec", bodega = "Rutini", anio = 2020)
        val otro = vinoDe("Alamos Malbec", anio = 2021)
        val eliminado = vinoDe("Rutini Cabernet")
        repositorio.guardarVinoNuevo(rutini, degustacionDe(rutini.id))
        repositorio.guardarVinoNuevo(otro, degustacionDe(otro.id))
        repositorio.guardarVinoNuevo(eliminado, degustacionDe(eliminado.id))
        repositorio.eliminarVino(eliminado.id)

        assertEquals(listOf(rutini.id), repositorio.buscar("malbec 2020").map { it.vino.id })
        assertEquals(listOf(rutini.id), repositorio.buscar("rutini").map { it.vino.id })
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
    fun observarUnVinoInexistenteDevuelveNull() = runTest {
        assertNull(repositorio.observarVino("no-existe").first())
    }

    @Test
    fun cualquierCambioDejaElVinoPendienteDeRespaldo() = runTest {
        val vino = vinoDe("Rutini Malbec").copy(estadoSync = EstadoSync.SINCRONIZADO)
        repositorio.guardarVinoNuevo(vino, degustacionDe(vino.id))

        repositorio.actualizarVino(vino.copy(nombre = "Rutini Malbec Reserva"))

        val guardado = repositorio.listarVinos().single()
        assertEquals("Rutini Malbec Reserva", guardado.nombre)
        assertEquals(EstadoSync.PENDIENTE, guardado.estadoSync)
        assertEquals(despues, guardado.actualizadoEn)
    }

    @Test
    fun editarUnVinoCambiaLoQueEncuentraLaBusqueda() = runTest {
        val vino = vinoDe("Rutini Malbec")
        repositorio.guardarVinoNuevo(vino, degustacionDe(vino.id))

        repositorio.actualizarVino(vino.copy(nombre = "Rutini Cabernet"))

        assertTrue(repositorio.buscar("malbec").isEmpty())
        assertEquals(1, repositorio.buscar("cabernet").size)
    }

    @Test
    fun eliminarYRestaurarDejanVinoYDegustacionesPendientesConLaHoraDelCambio() = runTest {
        val vino = vinoDe("Rutini Malbec").copy(estadoSync = EstadoSync.SINCRONIZADO)
        val degustacion = degustacionDe(vino.id).copy(estadoSync = EstadoSync.SINCRONIZADO)
        repositorio.guardarVinoNuevo(vino, degustacion)

        repositorio.eliminarVino(vino.id)
        repositorio.restaurarVino(vino.id)

        val guardado = repositorio.observarVino(vino.id).first()!!
        assertEquals(EstadoSync.PENDIENTE, guardado.vino.estadoSync)
        assertEquals(despues, guardado.vino.actualizadoEn)
        assertEquals(EstadoSync.PENDIENTE, guardado.degustaciones.single().estadoSync)
        assertEquals(despues, guardado.degustaciones.single().actualizadoEn)
    }

    // --- Respaldo en la nube ---

    @Test
    fun loNuevoQuedaPendienteYSeCuenta() = runTest {
        val vino = vinoDe("Rutini Malbec")
        val degustacion = degustacionDe(vino.id)
        repositorio.guardarVinoNuevo(vino, degustacion)

        val pendientes = repositorio.listarPendientes()

        assertEquals(listOf(vino.id), pendientes.vinos.map { it.id })
        assertEquals(listOf(degustacion.id), pendientes.degustaciones.map { it.id })
        assertEquals(2, repositorio.observarCantidadPendientes().first())
    }

    @Test
    fun marcarSincronizadoSacaLoSubidoDePendientesYGuardaLaFotoUrl() = runTest {
        val vino = vinoDe("Rutini Malbec")
        repositorio.guardarVinoNuevo(vino, degustacionDe(vino.id))
        val pendientes = repositorio.listarPendientes()

        val conFoto = pendientes.copy(vinos = pendientes.vinos.map { it.copy(fotoUrl = "u1/${it.id}.jpg") })
        repositorio.marcarSincronizado(conFoto)

        assertTrue(repositorio.listarPendientes().estaVacio)
        assertEquals(0, repositorio.observarCantidadPendientes().first())
        val guardado = repositorio.listarVinos().single()
        assertEquals(EstadoSync.SINCRONIZADO, guardado.estadoSync)
        assertEquals("u1/${vino.id}.jpg", guardado.fotoUrl)
    }

    @Test
    fun siCambioDuranteLaSubidaSiguePendiente() = runTest {
        val vino = vinoDe("Rutini Malbec")
        repositorio.guardarVinoNuevo(vino, degustacionDe(vino.id))
        val subidos = repositorio.listarPendientes()

        // El usuario edita el vino mientras se sube la versión anterior.
        repositorio.actualizarVino(vino.copy(nombre = "Rutini Malbec Reserva"))
        repositorio.marcarSincronizado(subidos)

        val pendientes = repositorio.listarPendientes()
        assertEquals(listOf(vino.id), pendientes.vinos.map { it.id })
        assertTrue(pendientes.degustaciones.isEmpty())
        assertEquals("Rutini Malbec Reserva", pendientes.vinos.single().nombre)
    }

    @Test
    fun loEliminadoTambienQuedaPendienteDeSubir() = runTest {
        val vino = vinoDe("Rutini Malbec")
        val degustacion = degustacionDe(vino.id)
        repositorio.guardarVinoNuevo(vino, degustacion)
        repositorio.marcarSincronizado(repositorio.listarPendientes())

        repositorio.eliminarVino(vino.id)

        val pendientes = repositorio.listarPendientes()
        assertTrue(pendientes.vinos.single().eliminado)
        assertTrue(pendientes.degustaciones.single().eliminado)
        assertEquals(2, repositorio.observarCantidadPendientes().first())
    }

    @Test
    fun marcarAlgoQueNoExisteNoHaceNada() = runTest {
        val vino = vinoDe("Rutini Malbec")
        repositorio.guardarVinoNuevo(vino, degustacionDe(vino.id))

        repositorio.marcarSincronizado(CambiosPendientes(listOf(vinoDe("Otro")), emptyList()))

        assertEquals(2, repositorio.observarCantidadPendientes().first())
    }

    // --- Asignar dueño al iniciar sesión ---

    @Test
    fun asignarUsuarioLeDaDuenoALosVinosSinCuentaYLosDejaPendientes() = runTest {
        val sinDueno = vinoDe("Rutini Malbec")
        repositorio.guardarVinoNuevo(sinDueno, degustacionDe(sinDueno.id))
        repositorio.marcarSincronizado(repositorio.listarPendientes())

        repositorio.asignarUsuario("u1")

        val guardado = repositorio.listarVinos().single()
        assertEquals("u1", guardado.usuarioId)
        assertEquals(EstadoSync.PENDIENTE, guardado.estadoSync)
        assertEquals(despues, guardado.actualizadoEn)
    }

    @Test
    fun asignarUsuarioNoTocaLosVinosDeOtroUsuario() = runTest {
        val deOtro = vinoDe("Otro").copy(usuarioId = "u2")
        val sinDueno = vinoDe("Mío")
        repositorio.guardarVinoNuevo(deOtro, degustacionDe(deOtro.id))
        repositorio.guardarVinoNuevo(sinDueno, degustacionDe(sinDueno.id))

        repositorio.asignarUsuario("u1")

        val porNombre = repositorio.listarVinos().associateBy { it.nombre }
        assertEquals("u2", porNombre.getValue("Otro").usuarioId)
        assertEquals("u1", porNombre.getValue("Mío").usuarioId)
    }
}
