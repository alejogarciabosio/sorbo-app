package com.sorbo.app.data.remote

import com.sorbo.app.domain.model.EstadoSesion
import com.sorbo.app.domain.repository.CuentaException
import com.sorbo.app.domain.repository.MotivoCuenta
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Prueba el repositorio contra un servidor HTTP falso que responde como Supabase Auth. */
class SesionRepositorySupabaseTest {

    private class AlmacenEnMemoria(var guardada: SesionGuardada? = null) : AlmacenSesion {
        override fun leer() = guardada
        override fun guardar(sesion: SesionGuardada) { guardada = sesion }
        override fun borrar() { guardada = null }
    }

    private val servidor = MockWebServer().apply { start() }
    private val api = ClienteSupabase.crearAuthApi(servidor.url("/").toString(), "clave")
    private val almacen = AlmacenEnMemoria()
    private val repositorio = SesionRepositorySupabase(api, almacen)

    private val sesionOk = """{"access_token":"acceso","refresh_token":"renovar","expires_in":3600,"user":{"id":"u1","email":"ana@mail.com"}}"""

    @After
    fun apagar() {
        servidor.shutdown()
    }

    private fun responder(codigo: Int, cuerpo: String = "") {
        servidor.enqueue(MockResponse().setResponseCode(codigo).setBody(cuerpo))
    }

    private fun motivo(resultado: Result<*>): MotivoCuenta = (resultado.exceptionOrNull() as CuentaException).motivo

    @Test
    fun iniciarSesionGuardaLosTokensYAbreLaSesion() = runTest {
        responder(200, sesionOk)

        val usuario = repositorio.iniciarSesion("  Ana@Mail.com ", "secreta123").getOrThrow()

        assertEquals("u1", usuario.id)
        assertEquals(EstadoSesion.ConSesion(usuario), repositorio.sesion.first())
        assertEquals("acceso", almacen.guardada?.accessToken)
        assertTrue(servidor.takeRequest().body.readUtf8().contains("\"email\":\"ana@mail.com\""))
    }

    @Test
    fun credencialesIncorrectas() = runTest {
        responder(400, """{"code":400,"error_code":"invalid_credentials","msg":"Invalid login credentials"}""")

        val resultado = repositorio.iniciarSesion("ana@mail.com", "mala12345")

        assertEquals(MotivoCuenta.CREDENCIALES_INCORRECTAS, motivo(resultado))
        assertEquals(EstadoSesion.SinSesion, repositorio.sesion.first())
    }

    @Test
    fun credencialesIncorrectasConElFormatoViejoDelServidor() = runTest {
        responder(400, """{"error":"invalid_grant","error_description":"Invalid login credentials"}""")

        assertEquals(MotivoCuenta.CREDENCIALES_INCORRECTAS, motivo(repositorio.iniciarSesion("ana@mail.com", "mala12345")))
    }

    @Test
    fun sinRedFallaConSinConexion() = runTest {
        servidor.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        assertEquals(MotivoCuenta.SIN_CONEXION, motivo(repositorio.iniciarSesion("ana@mail.com", "secreta123")))
    }

    @Test
    fun crearCuentaConEmailYaRegistrado() = runTest {
        responder(422, """{"code":422,"error_code":"user_already_exists","msg":"User already registered"}""")

        assertEquals(MotivoCuenta.EMAIL_YA_REGISTRADO, motivo(repositorio.crearCuenta("ana@mail.com", "secreta123")))
    }

    @Test
    fun crearCuentaConClaveDebilQueRechazaElServidor() = runTest {
        responder(422, """{"code":422,"error_code":"weak_password","msg":"Password is too weak"}""")

        assertEquals(MotivoCuenta.CLAVE_DEBIL, motivo(repositorio.crearCuenta("ana@mail.com", "12345678")))
    }

    @Test
    fun crearCuentaValidaAntesDeLlamarALaRed() = runTest {
        assertEquals(MotivoCuenta.EMAIL_INVALIDO, motivo(repositorio.crearCuenta("ana", "secreta123")))
        assertEquals(MotivoCuenta.CLAVE_DEBIL, motivo(repositorio.crearCuenta("ana@mail.com", "corta")))
        assertEquals(0, servidor.requestCount)
    }

    @Test
    fun crearCuentaAbreLaSesionCuandoNoHaceFaltaConfirmarElEmail() = runTest {
        responder(200, sesionOk)

        repositorio.crearCuenta("ana@mail.com", "secreta123").getOrThrow()

        assertTrue(repositorio.sesion.first() is EstadoSesion.ConSesion)
    }

    @Test
    fun crearCuentaSinTokensNoAbreLaSesion() = runTest {
        responder(200, """{"id":"u1","email":"ana@mail.com","confirmation_sent_at":"2026-11-17T18:00:00Z"}""")

        val usuario = repositorio.crearCuenta("ana@mail.com", "secreta123").getOrThrow()

        assertEquals("u1", usuario.id)
        assertEquals(EstadoSesion.SinSesion, repositorio.sesion.first())
    }

    @Test
    fun recuperarContrasenaMandaElEnlaceDeLaApp() = runTest {
        responder(200, "{}")

        repositorio.recuperarContrasena("ana@mail.com").getOrThrow()

        assertEquals("/auth/v1/recover?redirect_to=sorbo%3A%2F%2Frecuperar", servidor.takeRequest().path)
    }

    @Test
    fun cambiarContrasenaSinSesionFalla() = runTest {
        assertEquals(MotivoCuenta.SIN_SESION, motivo(repositorio.cambiarContrasena("nueva12345")))
        assertEquals(0, servidor.requestCount)
    }

    @Test
    fun cambiarContrasenaUsaElTokenDelUsuario() = runTest {
        almacen.guardada = SesionGuardada("acceso", "renovar", "u1", "ana@mail.com")
        responder(200, """{"id":"u1","email":"ana@mail.com"}""")

        repositorio.cambiarContrasena("nueva12345").getOrThrow()

        assertEquals("Bearer acceso", servidor.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun laSesionGuardadaSeRecuperaAlCrearElRepositorio() = runTest {
        val guardada = AlmacenEnMemoria(SesionGuardada("acceso", "renovar", "u1", "ana@mail.com"))

        val reabierto = SesionRepositorySupabase(api, guardada)

        assertEquals("u1", (reabierto.sesion.first() as EstadoSesion.ConSesion).usuario.id)
    }

    @Test
    fun cerrarSesionBorraLosTokensAunqueNoHayaRed() = runTest {
        almacen.guardada = SesionGuardada("acceso", "renovar", "u1", "ana@mail.com")
        servidor.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))
        val repo = SesionRepositorySupabase(api, almacen)

        repo.cerrarSesion()

        assertNull(almacen.guardada)
        assertEquals(EstadoSesion.SinSesion, repo.sesion.first())
    }

    @Test
    fun unErrorDesconocidoDelServidor() = runTest {
        responder(500, "Internal Server Error")

        assertEquals(MotivoCuenta.ERROR_DESCONOCIDO, motivo(repositorio.iniciarSesion("ana@mail.com", "secreta123")))
    }
}
