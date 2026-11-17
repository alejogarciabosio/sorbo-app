package com.sorbo.app.data.remote

import com.sorbo.app.domain.repository.CuentaException
import com.sorbo.app.domain.repository.MotivoCuenta
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class LectorEtiquetaNubeSupabaseTest {

    private class TokenFalso(var conSesion: Boolean = true) : ProveedorDeToken {
        override suspend fun <C> conToken(llamada: suspend (String) -> Response<C>): Response<C> {
            if (!conSesion) throw CuentaException(MotivoCuenta.SIN_SESION)
            return llamada("token-de-prueba")
        }
    }

    private val servidor = MockWebServer().apply { start() }
    private val api = ClienteSupabase.crearFuncionesApi(servidor.url("/").toString(), "clave")
    private val token = TokenFalso()
    private val foto = byteArrayOf(1, 2, 3, 4)
    private val lector = LectorEtiquetaNubeSupabase(api, token, leerArchivo = { foto })

    @After
    fun apagar() {
        servidor.shutdown()
    }

    private fun responder(codigo: Int, cuerpo: String) {
        servidor.enqueue(MockResponse().setResponseCode(codigo).setBody(cuerpo))
    }

    @Test
    fun enviaLaFotoEnBase64ConElTokenYDevuelveLaLectura() = runTest {
        responder(200, """{"nombre":"José Zuccardi","bodega":"José Zuccardi","cepa":"Malbec","anio":2017,"lineas":["JOSÉ","ZUCCARDI","2017"]}""")

        val lectura = lector.leer("foto.jpg").getOrThrow()

        assertEquals("José Zuccardi", lectura.nombre)
        assertEquals(2017, lectura.anio)
        assertEquals(listOf("JOSÉ", "ZUCCARDI", "2017"), lectura.lineas)
        val pedido = servidor.takeRequest()
        assertEquals("/functions/v1/leer-etiqueta", pedido.path)
        assertEquals("Bearer token-de-prueba", pedido.getHeader("Authorization"))
        assertEquals("clave", pedido.getHeader("apikey"))
        // Gson escribe el "=" del relleno como \u003d: es JSON válido y el servidor lo decodifica igual.
        val cuerpo = pedido.body.readUtf8().replace("\\u003d", "=")
        assertTrue(cuerpo.contains("\"imagen\":\"AQIDBA==\""))
    }

    @Test
    fun losDatosQueNoSeLeyeronVienenNull() = runTest {
        responder(200, """{"nombre":"Kaiken","bodega":null,"cepa":null,"anio":null}""")

        val lectura = lector.leer("foto.jpg").getOrThrow()

        assertEquals("Kaiken", lectura.nombre)
        assertEquals(null, lectura.bodega)
        assertEquals(emptyList<String>(), lectura.lineas)
    }

    @Test
    fun limiteDiarioEsUnaFalla() = runTest {
        responder(429, """{"error":"limite"}""")
        assertTrue(lector.leer("foto.jpg").isFailure)
    }

    @Test
    fun servicioCaidoEsUnaFalla() = runTest {
        responder(503, """{"error":"servicio"}""")
        assertTrue(lector.leer("foto.jpg").isFailure)
    }

    @Test
    fun sinConexionEsUnaFalla() = runTest {
        servidor.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))
        assertTrue(lector.leer("foto.jpg").isFailure)
    }

    @Test
    fun sinSesionEsUnaFallaYNoSeEnviaNada() = runTest {
        token.conSesion = false

        assertTrue(lector.leer("foto.jpg").isFailure)
        assertEquals(0, servidor.requestCount)
    }

    @Test
    fun unaFotoDemasiadoGrandeNoSeEnvia() = runTest {
        val grande = LectorEtiquetaNubeSupabase(api, token, leerArchivo = { ByteArray(LectorEtiquetaNubeSupabase.MAXIMO_BYTES + 1) })

        assertTrue(grande.leer("foto.jpg").isFailure)
        assertEquals(0, servidor.requestCount)
    }

    @Test
    fun siNoSePuedeAbrirLaFotoEsUnaFalla() = runTest {
        val roto = LectorEtiquetaNubeSupabase(api, token, leerArchivo = { throw java.io.FileNotFoundException() })

        assertTrue(roto.leer("foto.jpg").isFailure)
        assertEquals(0, servidor.requestCount)
    }
}
