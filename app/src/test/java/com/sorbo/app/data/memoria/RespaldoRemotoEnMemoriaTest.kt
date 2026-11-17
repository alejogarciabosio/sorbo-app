package com.sorbo.app.data.memoria

import com.sorbo.app.domain.degustacionDe
import com.sorbo.app.domain.repository.RespaldoException
import com.sorbo.app.domain.vinoDe
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RespaldoRemotoEnMemoriaTest {

    private val respaldo = RespaldoRemotoEnMemoria()

    @Test
    fun guardarDosVecesElMismoVinoNoLoDuplica() = runTest {
        val vino = vinoDe("Rutini Malbec")
        respaldo.guardarVinos(listOf(vino))
        respaldo.guardarVinos(listOf(vino.copy(nombre = "Rutini Malbec Reserva")))

        assertEquals(1, respaldo.vinos.size)
        assertEquals("Rutini Malbec Reserva", respaldo.vinos.getValue(vino.id).nombre)
    }

    @Test
    fun guardaLasDegustaciones() = runTest {
        val vino = vinoDe("Rutini Malbec")
        respaldo.guardarDegustaciones(listOf(degustacionDe(vino.id), degustacionDe(vino.id)))

        assertEquals(2, respaldo.degustaciones.size)
    }

    @Test
    fun laFotoQuedaEnLaCarpetaDelUsuario() = runTest {
        val ruta = respaldo.subirFoto("u1", "v1", "/data/fotos/abc.jpg")

        assertEquals("u1/v1.jpg", ruta)
        assertEquals("/data/fotos/abc.jpg", respaldo.fotos.getValue(ruta))
    }

    @Test
    fun unaFallaSimuladaOcurreUnaSolaVezYNoGuardaNada() = runTest {
        respaldo.falla = RespaldoException(reintentable = true, mensaje = "sin conexión")

        val error = runCatching { respaldo.guardarVinos(listOf(vinoDe("Rutini Malbec"))) }.exceptionOrNull()

        assertTrue((error as RespaldoException).reintentable)
        assertTrue(respaldo.vinos.isEmpty())
        respaldo.guardarVinos(listOf(vinoDe("Rutini Malbec")))
        assertEquals(1, respaldo.vinos.size)
        assertEquals(2, respaldo.operaciones)
    }

    @Test
    fun unaFallaNoReintentableSeDistingue() = runTest {
        respaldo.falla = RespaldoException(reintentable = false, mensaje = "dato rechazado")

        val error = runCatching { respaldo.subirFoto("u1", "v1", "/x.jpg") }.exceptionOrNull() as RespaldoException

        assertFalse(error.reintentable)
    }
}
