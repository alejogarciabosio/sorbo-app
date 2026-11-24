package com.sorbo.app.domain.usecase

import com.sorbo.app.data.memoria.ProgramadorDeSincronizacionEnMemoria
import com.sorbo.app.data.memoria.RespaldoRemotoEnMemoria
import com.sorbo.app.data.memoria.VinoRepositoryEnMemoria
import com.sorbo.app.domain.AHORA
import com.sorbo.app.domain.degustacionDe
import com.sorbo.app.domain.model.Usuario
import com.sorbo.app.domain.repository.RespaldoException
import com.sorbo.app.domain.vinoDe
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.ZoneOffset

class AlIniciarSesionTest {

    private val vinos = VinoRepositoryEnMemoria(Clock.fixed(AHORA, ZoneOffset.UTC))
    private val respaldo = RespaldoRemotoEnMemoria()
    private val programador = ProgramadorDeSincronizacionEnMemoria()
    private val alIniciarSesion = AlIniciarSesion(vinos, respaldo, programador)
    private val ana = Usuario("u1", "ana@mail.com")

    @Test
    fun losVinosCargadosSinCuentaPasanASerDelUsuarioYSeRespaldan() = runTest {
        val a = vinoDe("Rutini Malbec")
        val b = vinoDe("El Esteco Torrontés")
        vinos.guardarVinoNuevo(a, degustacionDe(a.id))
        vinos.guardarVinoNuevo(b, degustacionDe(b.id))

        val resultado = alIniciarSesion(ana)

        assertEquals(2, resultado.vinosParaRespaldar)
        assertTrue(vinos.listarVinos().all { it.usuarioId == "u1" })
        assertEquals(1, programador.veces)
    }

    @Test
    fun losEliminadosYLosDeOtroUsuarioNoSeCuentan() = runTest {
        val borrado = vinoDe("Borrado")
        val deOtro = vinoDe("De otro").copy(usuarioId = "u2")
        vinos.guardarVinoNuevo(borrado, degustacionDe(borrado.id))
        vinos.guardarVinoNuevo(deOtro, degustacionDe(deOtro.id))
        vinos.eliminarVino(borrado.id)

        assertEquals(0, alIniciarSesion(ana).vinosParaRespaldar)
    }

    @Test
    fun avisaSiHayUnRespaldoParaRestaurar() = runTest {
        respaldo.guardarVinos(listOf(vinoDe("En la nube").copy(usuarioId = "u1")))

        assertTrue(alIniciarSesion(ana).hayRespaldoParaRestaurar)
    }

    @Test
    fun sinRespaldoEnLaNubeNoOfreceRestaurar() = runTest {
        assertFalse(alIniciarSesion(ana).hayRespaldoParaRestaurar)
    }

    @Test
    fun siLaNubeNoRespondeIgualAsignaYProgramaElRespaldo() = runTest {
        val a = vinoDe("Rutini Malbec")
        vinos.guardarVinoNuevo(a, degustacionDe(a.id))
        respaldo.falla = RespaldoException(reintentable = true, mensaje = "sin conexión")

        val resultado = alIniciarSesion(ana)

        assertFalse(resultado.hayRespaldoParaRestaurar)
        assertEquals(1, resultado.vinosParaRespaldar)
        assertEquals(1, programador.veces)
    }
}
