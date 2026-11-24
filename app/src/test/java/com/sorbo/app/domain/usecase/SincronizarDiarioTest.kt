package com.sorbo.app.domain.usecase

import com.sorbo.app.data.memoria.RespaldoRemotoEnMemoria
import com.sorbo.app.data.memoria.SesionRepositoryEnMemoria
import com.sorbo.app.data.memoria.VinoRepositoryEnMemoria
import com.sorbo.app.domain.AHORA
import com.sorbo.app.domain.degustacionDe
import com.sorbo.app.domain.model.EstadoSesion
import com.sorbo.app.domain.model.EstadoSync
import com.sorbo.app.domain.model.Vino
import com.sorbo.app.domain.repository.RespaldoException
import com.sorbo.app.domain.vinoDe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.ZoneOffset

class SincronizarDiarioTest {

    private val vinos = VinoRepositoryEnMemoria(Clock.fixed(AHORA, ZoneOffset.UTC))
    private val respaldo = RespaldoRemotoEnMemoria()
    private val sesion = SesionRepositoryEnMemoria()
    private val sincronizar = SincronizarDiario(vinos, respaldo, sesion)

    private suspend fun conCuenta(): String {
        sesion.crearCuenta("ana@mail.com", "secreta123")
        return (sesion.sesion.first() as EstadoSesion.ConSesion).usuario.id
    }

    private suspend fun guardar(vino: Vino = vinoDe("Rutini Malbec")): Vino {
        vinos.guardarVinoNuevo(vino, degustacionDe(vino.id))
        return vino
    }

    @Test
    fun sinCuentaNoSubeNada() = runTest {
        guardar()

        assertEquals(ResultadoSincronizacion.SinSesion, sincronizar())
        assertEquals(0, respaldo.operaciones)
        assertEquals(2, vinos.observarCantidadPendientes().first())
    }

    @Test
    fun subeLoPendienteYLoMarcaSincronizado() = runTest {
        val usuario = conCuenta()
        val vino = guardar()

        val resultado = sincronizar()

        assertEquals(ResultadoSincronizacion.Sincronizado(vinos = 1, degustaciones = 1), resultado)
        assertEquals(usuario, respaldo.vinos.getValue(vino.id).usuarioId)
        assertEquals(1, respaldo.degustaciones.size)
        assertEquals(0, vinos.observarCantidadPendientes().first())
    }

    @Test
    fun conNadaPendienteNoLlamaALaNube() = runTest {
        conCuenta()
        guardar()
        sincronizar()
        val antes = respaldo.operaciones

        assertEquals(ResultadoSincronizacion.NadaPendiente, sincronizar())
        assertEquals(antes, respaldo.operaciones)
    }

    @Test
    fun subeLaFotoYGuardaSuDireccion() = runTest {
        val usuario = conCuenta()
        val vino = guardar(vinoDe("Rutini Malbec").copy(fotoPath = "/fotos/a.jpg"))

        sincronizar()

        val ruta = "$usuario/${vino.id}.jpg"
        assertEquals("/fotos/a.jpg", respaldo.fotos.getValue(ruta))
        assertEquals(ruta, respaldo.vinos.getValue(vino.id).fotoUrl)
        assertEquals(ruta, vinos.listarVinos().single().fotoUrl)
    }

    @Test
    fun unaFotoYaSubidaNoSeVuelveASubir() = runTest {
        conCuenta()
        guardar(vinoDe("Rutini Malbec").copy(fotoPath = "/fotos/a.jpg", fotoUrl = "ya/subida.jpg"))

        sincronizar()

        assertTrue(respaldo.fotos.isEmpty())
    }

    @Test
    fun siFallaQuedaPendienteYAlReintentarNoDuplica() = runTest {
        conCuenta()
        val vino = guardar()
        respaldo.falla = RespaldoException(reintentable = true, mensaje = "sin conexión")

        assertEquals(ResultadoSincronizacion.Fallo(reintentable = true), sincronizar())
        assertEquals(2, vinos.observarCantidadPendientes().first())

        assertTrue(sincronizar() is ResultadoSincronizacion.Sincronizado)
        assertEquals(listOf(vino.id), respaldo.vinos.keys.toList())
        assertEquals(1, respaldo.degustaciones.size)
        assertEquals(0, vinos.observarCantidadPendientes().first())
    }

    @Test
    fun unErrorQueNoSeArreglaReintentandoSeInforma() = runTest {
        conCuenta()
        guardar()
        respaldo.falla = RespaldoException(reintentable = false, mensaje = "dato rechazado")

        assertEquals(ResultadoSincronizacion.Fallo(reintentable = false), sincronizar())
    }

    @Test
    fun laEliminacionSePropaga() = runTest {
        conCuenta()
        val vino = guardar()
        sincronizar()

        vinos.eliminarVino(vino.id)
        sincronizar()

        assertTrue(respaldo.vinos.getValue(vino.id).eliminado)
        assertTrue(respaldo.degustaciones.values.single().eliminado)
        assertEquals(0, vinos.observarCantidadPendientes().first())
    }

    @Test
    fun laEdicionSePropaga() = runTest {
        conCuenta()
        val vino = guardar()
        sincronizar()

        vinos.actualizarVino(vinos.listarVinos().single().copy(nombre = "Rutini Malbec Reserva"))
        sincronizar()

        assertEquals("Rutini Malbec Reserva", respaldo.vinos.getValue(vino.id).nombre)
    }

    @Test
    fun noSubeLosVinosDeOtraCuentaDelMismoTelefono() = runTest {
        conCuenta()
        val ajeno = guardar(vinoDe("De otro").copy(usuarioId = "otra-cuenta"))
        val propio = guardar()

        sincronizar()

        assertEquals(listOf(propio.id), respaldo.vinos.keys.toList())
        assertEquals(EstadoSync.PENDIENTE, vinos.listarVinos().first { it.id == ajeno.id }.estadoSync)
    }

    @Test
    fun unaDegustacionNuevaDeUnVinoYaRespaldadoSeSube() = runTest {
        conCuenta()
        val vino = guardar()
        sincronizar()

        vinos.agregarDegustacion(degustacionDe(vino.id))

        assertEquals(ResultadoSincronizacion.Sincronizado(vinos = 0, degustaciones = 1), sincronizar())
        assertEquals(2, respaldo.degustaciones.size)
    }
}
