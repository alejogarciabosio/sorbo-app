package com.sorbo.app.data.remote

import com.sorbo.app.domain.model.AnalisisGustos
import com.sorbo.app.domain.repository.AnalisisException
import com.sorbo.app.domain.repository.CuentaException
import com.sorbo.app.domain.repository.MotivoAnalisis
import com.sorbo.app.domain.repository.MotivoCuenta
import com.sorbo.app.domain.usecase.EstadisticasGustos
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.Response
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class AnalisisGustosRepositorySupabaseTest {

    private class AlmacenEnMemoria(var guardado: AnalisisGustos? = null) : AlmacenAnalisis {
        override fun leer() = guardado
        override fun guardar(analisis: AnalisisGustos) { guardado = analisis }
    }

    /** Sesión falsa: da siempre el mismo token, o falla como si no hubiera sesión. */
    private class TokenFalso(var conSesion: Boolean = true) : ProveedorDeToken {
        override suspend fun <C> conToken(llamada: suspend (String) -> Response<C>): Response<C> {
            if (!conSesion) throw CuentaException(MotivoCuenta.SIN_SESION)
            return llamada("token-de-prueba")
        }
    }

    private val ahora = Instant.parse("2026-11-17T22:00:00Z")
    private val servidor = MockWebServer().apply { start() }
    private val api = ClienteSupabase.crearFuncionesApi(servidor.url("/").toString(), "clave")
    private val almacen = AlmacenEnMemoria()
    private val token = TokenFalso()
    private val repositorio = AnalisisGustosRepositorySupabase(api, token, almacen, Clock.fixed(ahora, ZoneOffset.UTC))

    private val estadisticas = EstadisticasGustos(9, habilitado = true, faltan = 0, emptyList(), emptyList())
    private val anterior = AnalisisGustos("Resumen viejo", listOf("Algo"), Instant.parse("2026-11-01T00:00:00Z"), 5)

    @After
    fun apagar() {
        servidor.shutdown()
    }

    private fun responder(codigo: Int, cuerpo: String) {
        servidor.enqueue(MockResponse().setResponseCode(codigo).setBody(cuerpo))
    }

    private fun motivo(r: Result<*>) = (r.exceptionOrNull() as AnalisisException).motivo

    @Test
    fun generarGuardaYEmiteElResumen() = runTest {
        responder(200, """{"resumen":"Te gustan los tintos secos.","sugerencias":["Probá un Cabernet Franc","Un Malbec de altura"]}""")

        val analisis = repositorio.generar(estadisticas).getOrThrow()

        assertEquals("Te gustan los tintos secos.", analisis.resumen)
        assertEquals(2, analisis.sugerencias.size)
        assertEquals(ahora, analisis.generadoEn)
        assertEquals(9, analisis.cantidadDegustaciones)
        assertEquals(analisis, repositorio.observarUltimo().first())
        assertEquals(analisis, almacen.guardado)
        assertEquals("Bearer token-de-prueba", servidor.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun alAbrirMuestraElUltimoResumenGuardado() = runTest {
        val repo = AnalisisGustosRepositorySupabase(api, token, AlmacenEnMemoria(anterior))

        assertEquals(anterior, repo.observarUltimo().first())
    }

    @Test
    fun limiteDiario() = runTest {
        responder(429, """{"error":"limite"}""")
        assertEquals(MotivoAnalisis.LIMITE_ALCANZADO, motivo(repositorio.generar(estadisticas)))
    }

    @Test
    fun servicioCaido() = runTest {
        responder(503, """{"error":"servicio"}""")
        assertEquals(MotivoAnalisis.SERVICIO_NO_DISPONIBLE, motivo(repositorio.generar(estadisticas)))
    }

    @Test
    fun sinSesion() = runTest {
        token.conSesion = false
        assertEquals(MotivoAnalisis.SIN_SESION, motivo(repositorio.generar(estadisticas)))
        assertEquals(0, servidor.requestCount)
    }

    @Test
    fun sinConexion() = runTest {
        servidor.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))
        assertEquals(MotivoAnalisis.SIN_CONEXION, motivo(repositorio.generar(estadisticas)))
    }

    @Test
    fun pedidoInvalidoEsErrorDesconocido() = runTest {
        responder(422, """{"error":"pocas_degustaciones"}""")
        assertEquals(MotivoAnalisis.ERROR_DESCONOCIDO, motivo(repositorio.generar(estadisticas)))
    }

    @Test
    fun unaRespuestaIncompletaNoSeGuarda() = runTest {
        responder(200, """{"resumen":"  ","sugerencias":[]}""")

        assertEquals(MotivoAnalisis.ERROR_DESCONOCIDO, motivo(repositorio.generar(estadisticas)))
        assertNull(repositorio.observarUltimo().first())
    }

    @Test
    fun siFallaSeConservaElResumenAnterior() = runTest {
        val repo = AnalisisGustosRepositorySupabase(api, token, AlmacenEnMemoria(anterior))
        responder(503, """{"error":"servicio"}""")

        repo.generar(estadisticas)

        assertEquals(anterior, repo.observarUltimo().first())
    }
}
