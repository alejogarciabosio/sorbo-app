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

    // --- Renovar la sesión cuando vence el token ---

    private val tokenVencido = """{"code":401,"error_code":"bad_jwt","msg":"JWT expired"}"""
    private val sesionRenovada = """{"access_token":"acceso-nuevo","refresh_token":"renovar-nuevo","user":{"id":"u1","email":"ana@mail.com"}}"""

    private fun conSesionIniciada(): SesionRepositorySupabase {
        almacen.guardada = SesionGuardada("acceso-viejo", "renovar-viejo", "u1", "ana@mail.com")
        return SesionRepositorySupabase(api, almacen)
    }

    @Test
    fun siElTokenVencioSeRenuevaYSeReintentaUnaVez() = runTest {
        val repo = conSesionIniciada()
        responder(401, tokenVencido)
        responder(200, sesionRenovada)
        responder(200, """{"id":"u1","email":"ana@mail.com"}""")

        repo.cambiarContrasena("nueva12345").getOrThrow()

        assertEquals("Bearer acceso-viejo", servidor.takeRequest().getHeader("Authorization"))
        val renovar = servidor.takeRequest()
        assertEquals("/auth/v1/token?grant_type=refresh_token", renovar.path)
        assertTrue(renovar.body.readUtf8().contains("\"refresh_token\":\"renovar-viejo\""))
        assertEquals("Bearer acceso-nuevo", servidor.takeRequest().getHeader("Authorization"))
        assertEquals("renovar-nuevo", almacen.guardada?.refreshToken)
    }

    @Test
    fun tambienSeRenuevaConElFormatoRealDeSupabaseAuth403BadJwt() = runTest {
        val repo = conSesionIniciada()
        responder(403, """{"code":403,"error_code":"bad_jwt","msg":"invalid JWT: unable to parse or verify signature, token has invalid claims: token is expired"}""")
        responder(200, sesionRenovada)
        responder(200, """{"id":"u1","email":"ana@mail.com"}""")

        repo.cambiarContrasena("nueva12345").getOrThrow()

        assertEquals(3, servidor.requestCount)
        assertEquals("acceso-nuevo", almacen.guardada?.accessToken)
    }

    @Test
    fun unOtro403NoRenuevaYSeTraduceIgual() = runTest {
        val repo = conSesionIniciada()
        responder(403, """{"code":403,"error_code":"weak_password","msg":"Password is too weak"}""")

        assertEquals(MotivoCuenta.CLAVE_DEBIL, motivo(repo.cambiarContrasena("nueva12345")))
        assertEquals(1, servidor.requestCount)
    }

    @Test
    fun siLaRenovacionEsRechazadaSeCierraLaSesion() = runTest {
        val repo = conSesionIniciada()
        responder(401, tokenVencido)
        responder(400, """{"code":400,"error_code":"refresh_token_not_found","msg":"Invalid Refresh Token"}""")

        val resultado = repo.cambiarContrasena("nueva12345")

        assertEquals(MotivoCuenta.SIN_SESION, motivo(resultado))
        assertNull(almacen.guardada)
        assertEquals(EstadoSesion.SinSesion, repo.sesion.first())
        assertEquals(2, servidor.requestCount)
    }

    @Test
    fun siNoHayRedAlRenovarLaSesionSeConserva() = runTest {
        val repo = conSesionIniciada()
        responder(401, tokenVencido)
        servidor.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        val resultado = repo.cambiarContrasena("nueva12345")

        assertEquals(MotivoCuenta.SIN_CONEXION, motivo(resultado))
        assertEquals("renovar-viejo", almacen.guardada?.refreshToken)
        assertTrue(repo.sesion.first() is EstadoSesion.ConSesion)
    }

    @Test
    fun siElServidorFallaAlRenovarLaSesionSeConserva() = runTest {
        val repo = conSesionIniciada()
        responder(401, tokenVencido)
        responder(503, "Service Unavailable")

        assertEquals(MotivoCuenta.ERROR_DESCONOCIDO, motivo(repo.cambiarContrasena("nueva12345")))
        assertTrue(repo.sesion.first() is EstadoSesion.ConSesion)
    }

    // --- Eliminar la cuenta (función eliminar-cuenta de la nube) ---

    @Test
    fun eliminarCuentaLlamaALaFuncionConElTokenYCierraLaSesion() = runTest {
        val repo = conSesionIniciada()
        responder(200, """{"ok":true}""")

        repo.eliminarCuenta().getOrThrow()

        val pedido = servidor.takeRequest()
        assertEquals("POST", pedido.method)
        assertEquals("/functions/v1/eliminar-cuenta", pedido.path)
        assertEquals("Bearer acceso-viejo", pedido.getHeader("Authorization"))
        assertNull(almacen.guardada)
        assertEquals(EstadoSesion.SinSesion, repo.sesion.first())
    }

    @Test
    fun eliminarCuentaSinSesionNoLlamaALaNube() = runTest {
        assertEquals(MotivoCuenta.SIN_SESION, motivo(repositorio.eliminarCuenta()))
        assertEquals(0, servidor.requestCount)
    }

    @Test
    fun siLaFuncionFallaLaSesionSigue() = runTest {
        val repo = conSesionIniciada()
        responder(500, """{"error":"servicio"}""")

        assertEquals(MotivoCuenta.ERROR_DESCONOCIDO, motivo(repo.eliminarCuenta()))
        assertTrue(repo.sesion.first() is EstadoSesion.ConSesion)
    }

    @Test
    fun eliminarCuentaSinRedFallaConSinConexion() = runTest {
        val repo = conSesionIniciada()
        servidor.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        assertEquals(MotivoCuenta.SIN_CONEXION, motivo(repo.eliminarCuenta()))
        assertTrue(repo.sesion.first() is EstadoSesion.ConSesion)
    }

    @Test
    fun eliminarCuentaConTokenVencidoLoRenuevaPrimero() = runTest {
        val repo = conSesionIniciada()
        responder(401, """{"error":"sin_sesion"}""")
        responder(200, sesionRenovada)
        responder(200, """{"ok":true}""")

        repo.eliminarCuenta().getOrThrow()

        assertEquals(3, servidor.requestCount)
        assertEquals(EstadoSesion.SinSesion, repo.sesion.first())
    }

    // --- Sesión desde el enlace del mail ---

    @Test
    fun elEnlaceValidaElTokenContraSupabaseYGuardaLaSesion() = runTest {
        responder(200, """{"id":"u9","email":"ana@mail.com"}""")

        val usuario = repositorio.iniciarSesionConEnlace("acceso-del-enlace", "renovar-del-enlace").getOrThrow()

        val pedido = servidor.takeRequest()
        assertEquals("GET", pedido.method)
        assertEquals("/auth/v1/user", pedido.path)
        assertEquals("Bearer acceso-del-enlace", pedido.getHeader("Authorization"))
        assertEquals("u9", usuario.id)
        assertEquals(SesionGuardada("acceso-del-enlace", "renovar-del-enlace", "u9", "ana@mail.com"), almacen.guardada)
        assertEquals(EstadoSesion.ConSesion(usuario), repositorio.sesion.first())
    }

    @Test
    fun unEnlaceVencidoFallaSinSesionYNoGuardaNada() = runTest {
        responder(403, """{"code":403,"error_code":"bad_jwt","msg":"invalid JWT: token is expired"}""")

        assertEquals(MotivoCuenta.SIN_SESION, motivo(repositorio.iniciarSesionConEnlace("vencido", "r")))
        assertNull(almacen.guardada)
        assertEquals(EstadoSesion.SinSesion, repositorio.sesion.first())
    }
}
