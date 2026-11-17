package com.sorbo.app.data.remote

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClienteSupabaseTest {

    private val servidor = MockWebServer().apply { start() }
    private val api = ClienteSupabase.crearAuthApi(servidor.url("/").toString(), "clave-de-prueba")

    @After
    fun apagar() {
        servidor.shutdown()
    }

    @Test
    fun todosLosPedidosLlevanLaClaveAnonima() = runTest {
        servidor.enqueue(MockResponse().setBody("""{"access_token":"a","refresh_token":"r","user":{"id":"u1","email":"a@b.com"}}"""))

        api.iniciarSesion(CredencialesDto("a@b.com", "secreta123"))

        val pedido = servidor.takeRequest()
        assertEquals("clave-de-prueba", pedido.getHeader("apikey"))
        assertEquals("/auth/v1/token?grant_type=password", pedido.path)
        assertTrue(pedido.body.readUtf8().contains("\"password\":\"secreta123\""))
    }

    @Test
    fun laRespuestaDeSesionSeConvierteDesdeElJson() = runTest {
        servidor.enqueue(
            MockResponse().setBody("""{"access_token":"a","refresh_token":"r","expires_in":3600,"user":{"id":"u1","email":"a@b.com"}}"""),
        )

        val sesion = api.iniciarSesion(CredencialesDto("a@b.com", "secreta123")).body()!!

        assertEquals("a", sesion.accessToken)
        assertEquals("r", sesion.refreshToken)
        assertEquals(3600L, sesion.expiraEnSegundos)
        assertEquals("u1", sesion.user?.id)
    }

    @Test
    fun losPedidosDelUsuarioLlevanSuToken() = runTest {
        servidor.enqueue(MockResponse().setResponseCode(204))

        api.cerrarSesion("Bearer token-del-usuario")

        val pedido = servidor.takeRequest()
        assertEquals("Bearer token-del-usuario", pedido.getHeader("Authorization"))
        assertEquals("/auth/v1/logout", pedido.path)
    }
}
