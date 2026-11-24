package com.sorbo.app.data.remote

import com.sorbo.app.domain.AHORA
import com.sorbo.app.domain.HOY
import com.sorbo.app.domain.model.Comida
import com.sorbo.app.domain.model.Degustacion
import com.sorbo.app.domain.model.Dulzor
import com.sorbo.app.domain.repository.CuentaException
import com.sorbo.app.domain.repository.MotivoCuenta
import com.sorbo.app.domain.repository.RespaldoException
import com.sorbo.app.domain.vinoDe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import retrofit2.Response

class RespaldoRemotoSupabaseTest {

    private class TokenFalso(var conSesion: Boolean = true) : ProveedorDeToken {
        override suspend fun <C> conToken(llamada: suspend (String) -> Response<C>): Response<C> {
            if (!conSesion) throw CuentaException(MotivoCuenta.SIN_SESION)
            return llamada("tok")
        }
    }

    @get:Rule
    val carpeta = TemporaryFolder()

    private val servidor = MockWebServer().apply { start() }
    private val token = TokenFalso()
    private val respaldo = RespaldoRemotoSupabase(
        ClienteSupabase.crearRestApi(servidor.url("/").toString(), "clave"),
        token,
        Dispatchers.Unconfined,
    )

    @After
    fun apagar() {
        servidor.shutdown()
    }

    private fun responder(codigo: Int, cuerpo: String = "") {
        servidor.enqueue(MockResponse().setResponseCode(codigo).setBody(cuerpo))
    }

    private suspend fun error(bloque: suspend () -> Unit): RespaldoException =
        runCatching { bloque() }.exceptionOrNull() as RespaldoException

    @Test
    fun guardarVinosHaceUnUpsertConLasColumnasDeLaNube() = runTest {
        responder(201)
        val vino = vinoDe("Rutini Malbec", bodega = null, anio = 2020).copy(usuarioId = "u1", fotoPath = "/local.jpg")

        respaldo.guardarVinos(listOf(vino))

        val pedido = servidor.takeRequest()
        assertEquals("POST", pedido.method)
        assertEquals("/rest/v1/vinos", pedido.path)
        assertEquals("Bearer tok", pedido.getHeader("Authorization"))
        assertEquals("resolution=merge-duplicates,return=minimal", pedido.getHeader("Prefer"))
        val json = pedido.body.readUtf8()
        assertTrue(json.contains("\"usuario_id\":\"u1\""))
        assertTrue(json.contains("\"actualizado_en\":\"$AHORA\""))
        // Los vacíos viajan como null, para que el upsert los borre también en la nube.
        assertTrue(json.contains("\"bodega\":null"))
        // Lo que es solo del teléfono no viaja.
        assertFalse(json.contains("foto_path") || json.contains("fotoPath") || json.contains("estado"))
    }

    @Test
    fun guardarDegustacionesMandaLasEnumeracionesPorNombreYSinUsuario() = runTest {
        responder(201)
        val degustacion = Degustacion(
            id = "d1", vinoId = "v1", fecha = HOY, meGusto = true, actualizadoEn = AHORA,
            dulzor = Dulzor.SECO, comida = Comida.CARNE,
        )

        respaldo.guardarDegustaciones(listOf(degustacion))

        val pedido = servidor.takeRequest()
        assertEquals("/rest/v1/degustaciones", pedido.path)
        val json = pedido.body.readUtf8()
        assertTrue(json.contains("\"vino_id\":\"v1\""))
        assertTrue(json.contains("\"fecha\":\"$HOY\""))
        assertTrue(json.contains("\"me_gusto\":true"))
        assertTrue(json.contains("\"dulzor\":\"SECO\""))
        assertTrue(json.contains("\"comida\":\"CARNE\""))
        assertFalse(json.contains("usuario_id"))
    }

    @Test
    fun subirFotoVaALaCarpetaDelUsuarioYReemplazaLaAnterior() = runTest {
        responder(200, """{"Key":"etiquetas/u1/v1.jpg"}""")
        val foto = carpeta.newFile("a.jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }

        val ruta = respaldo.subirFoto("u1", "v1", foto.absolutePath)

        assertEquals("u1/v1.jpg", ruta)
        val pedido = servidor.takeRequest()
        assertEquals("/storage/v1/object/etiquetas/u1/v1.jpg", pedido.path)
        assertEquals("true", pedido.getHeader("x-upsert"))
        assertEquals("image/jpeg", pedido.getHeader("Content-Type"))
        assertEquals(3L, pedido.bodySize)
    }

    @Test
    fun unaFotoQueYaNoExisteNoSeReintenta() = runTest {
        val e = error { respaldo.subirFoto("u1", "v1", carpeta.root.absolutePath + "/no-existe.jpg") }

        assertFalse(e.reintentable)
        assertEquals(0, servidor.requestCount)
    }

    @Test
    fun hayRespaldoPreguntaSoloPorVinosNoEliminadosDelUsuario() = runTest {
        responder(200, """[{"id":"v1"}]""")

        assertTrue(respaldo.hayRespaldo("u1"))
        assertEquals(
            "/rest/v1/vinos?usuario_id=eq.u1&eliminado=eq.false&select=id&limit=1",
            servidor.takeRequest().path,
        )
    }

    @Test
    fun sinVinosEnLaNubeNoHayRespaldo() = runTest {
        responder(200, "[]")
        assertFalse(respaldo.hayRespaldo("u1"))
    }

    @Test
    fun sinRedSeReintenta() = runTest {
        servidor.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))
        assertTrue(error { respaldo.guardarVinos(listOf(vinoDe("A"))) }.reintentable)
    }

    @Test
    fun servidorCaidoYDemasiadosPedidosSeReintentan() = runTest {
        responder(503)
        assertTrue(error { respaldo.guardarVinos(listOf(vinoDe("A"))) }.reintentable)
        responder(429)
        assertTrue(error { respaldo.guardarVinos(listOf(vinoDe("A"))) }.reintentable)
    }

    @Test
    fun unDatoRechazadoNoSeReintenta() = runTest {
        responder(400, """{"code":"23514","message":"violates check constraint"}""")
        assertFalse(error { respaldo.guardarVinos(listOf(vinoDe("A"))) }.reintentable)
    }

    @Test
    fun sinSesionNoSeReintenta() = runTest {
        token.conSesion = false
        assertFalse(error { respaldo.guardarVinos(listOf(vinoDe("A"))) }.reintentable)
        assertEquals(0, servidor.requestCount)
    }
}
