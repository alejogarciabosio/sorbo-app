package com.sorbo.app.domain.usecase

import com.sorbo.app.data.memoria.RespaldoRemotoEnMemoria
import com.sorbo.app.data.memoria.SesionRepositoryEnMemoria
import com.sorbo.app.data.memoria.VinoRepositoryEnMemoria
import com.sorbo.app.domain.degustacionDe
import com.sorbo.app.domain.model.EstadoRestauracion
import com.sorbo.app.domain.model.EstadoSesion
import com.sorbo.app.domain.model.EstadoSync
import com.sorbo.app.domain.model.MotivoRestauracion
import com.sorbo.app.domain.repository.CuentaException
import com.sorbo.app.domain.repository.MotivoCuenta
import com.sorbo.app.domain.repository.RespaldoException
import com.sorbo.app.domain.vinoDe
import com.sorbo.app.ui.viewmodels.AlmacenFotosFalso
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RestaurarDiarioTest {

    private val vinos = VinoRepositoryEnMemoria()
    private val nube = RespaldoRemotoEnMemoria()
    private val sesion = SesionRepositoryEnMemoria()
    private val fotos = AlmacenFotosFalso()
    private val restaurar = RestaurarDiario(vinos, nube, sesion, fotos, tamanoPagina = 2)

    private suspend fun conCuenta(): String {
        sesion.crearCuenta("ana@mail.com", "secreta123")
        return (sesion.sesion.first() as EstadoSesion.ConSesion).usuario.id
    }

    /** Carga [cantidad] vinos (con una degustación cada uno) en la "nube" del usuario. */
    private suspend fun enLaNube(usuario: String, cantidad: Int) {
        val lista = (1..cantidad).map { vinoDe("Vino $it", id = "v$it").copy(usuarioId = usuario, estadoSync = EstadoSync.SINCRONIZADO) }
        nube.guardarVinos(lista)
        nube.guardarDegustaciones(lista.map { degustacionDe(it.id) })
    }

    @Test
    fun traeTodoElDiarioPorPaginasYTermina() = runTest {
        val usuario = conCuenta()
        enLaNube(usuario, 5)

        restaurar()

        assertEquals(EstadoRestauracion.Terminada(vinos = 5), restaurar.estado.value)
        val diario = vinos.observarDiario().first()
        assertEquals(5, diario.size)
        assertTrue(diario.all { it.veces == 1 && it.vino.estadoSync == EstadoSync.SINCRONIZADO })
        assertEquals(0, vinos.observarCantidadPendientes().first())
    }

    @Test
    fun sinNadaEnLaNubeAvisaQueNoHayRespaldo() = runTest {
        conCuenta()

        restaurar()

        assertEquals(EstadoRestauracion.SinRespaldo, restaurar.estado.value)
    }

    @Test
    fun sinSesionEsUnError() = runTest {
        restaurar()

        assertEquals(EstadoRestauracion.Error(MotivoRestauracion.SIN_SESION), restaurar.estado.value)
    }

    @Test
    fun restaurarDosVecesNoDuplica() = runTest {
        val usuario = conCuenta()
        enLaNube(usuario, 3)

        restaurar()
        restaurar()

        assertEquals(3, vinos.listarVinos().size)
        assertTrue(vinos.observarDiario().first().all { it.veces == 1 })
    }

    @Test
    fun siSeCortaSePuedeReanudarSinDuplicar() = runTest {
        val usuario = conCuenta()
        enLaNube(usuario, 5)
        // La segunda página de vinos falla: la conexión se corta a mitad de camino.
        val cortada = RestaurarDiario(vinos, CortaEnElPedido(nube, numero = 3), sesion, fotos, tamanoPagina = 2)

        cortada()
        assertEquals(EstadoRestauracion.Error(MotivoRestauracion.SIN_CONEXION), cortada.estado.value)
        assertEquals(2, vinos.listarVinos().size)

        restaurar()
        assertEquals(EstadoRestauracion.Terminada(vinos = 5), restaurar.estado.value)
        assertEquals(5, vinos.listarVinos().size)
    }

    @Test
    fun bajaLasFotosYNoLasVuelveABajarAlReanudar() = runTest {
        val usuario = conCuenta()
        val ruta = nube.subirFoto(usuario, "v1", "/original.jpg")
        nube.guardarVinos(listOf(vinoDe("Con foto", id = "v1").copy(usuarioId = usuario, fotoUrl = ruta)))

        restaurar()
        restaurar()

        val vino = vinos.listarVinos().single()
        assertEquals("fotos/1.jpg", vino.fotoPath)
        assertEquals(ruta, vino.fotoUrl)
        assertEquals(1, fotos.guardadas.size)
    }

    @Test
    fun siUnaFotoNoSePuedeBajarElVinoLlegaIgual() = runTest {
        val usuario = conCuenta()
        nube.guardarVinos(listOf(vinoDe("Foto perdida", id = "v1").copy(usuarioId = usuario, fotoUrl = "$usuario/no-existe.jpg")))

        restaurar()

        assertEquals(EstadoRestauracion.Terminada(vinos = 1), restaurar.estado.value)
        assertEquals(null, vinos.listarVinos().single().fotoPath)
    }

    @Test
    fun loEliminadoEnLaNubeNoApareceEnElDiario() = runTest {
        val usuario = conCuenta()
        nube.guardarVinos(listOf(vinoDe("Borrado", id = "v1").copy(usuarioId = usuario, eliminado = true)))

        restaurar()

        assertTrue(vinos.observarDiario().first().isEmpty())
    }

    @Test
    fun siLaSesionSePerdioDuranteLaRestauracionEsSinSesion() = runTest {
        conCuenta()
        nube.falla = RespaldoException(reintentable = false, mensaje = "Sin sesión", causa = CuentaException(MotivoCuenta.SIN_SESION))

        restaurar()

        assertEquals(EstadoRestauracion.Error(MotivoRestauracion.SIN_SESION), restaurar.estado.value)
    }

    @Test
    fun publicaElProgresoMientrasBaja() = runTest {
        val usuario = conCuenta()
        enLaNube(usuario, 3)
        val vistos = mutableListOf<EstadoRestauracion>()
        lateinit var observando: RestaurarDiario
        observando = RestaurarDiario(vinos, Observado(nube) { vistos += observando.estado.value }, sesion, fotos, tamanoPagina = 2)

        observando()

        assertTrue(EstadoRestauracion.Restaurando(bajados = 0, total = 3) in vistos)
        assertTrue(EstadoRestauracion.Restaurando(bajados = 2, total = 3) in vistos)
    }
}

/** "Nube" que falla en el pedido número [numero], como si se cortara la conexión ahí. */
private class CortaEnElPedido(private val nube: RespaldoRemotoEnMemoria, private val numero: Int) :
    com.sorbo.app.domain.repository.RespaldoRemoto by nube {
    private var pedidos = 0
    private fun contar() {
        if (++pedidos == numero) throw RespaldoException(reintentable = true, mensaje = "sin conexión")
    }
    override suspend fun contarVinos(usuarioId: String): Int { contar(); return nube.contarVinos(usuarioId) }
    override suspend fun bajarVinos(usuarioId: String, desde: Int, cantidad: Int) = run { contar(); nube.bajarVinos(usuarioId, desde, cantidad) }
}

/** "Nube" que avisa cada vez que se le pide una página de vinos: así el test ve el progreso intermedio. */
private class Observado(private val nube: RespaldoRemotoEnMemoria, private val alPedirPagina: () -> Unit) :
    com.sorbo.app.domain.repository.RespaldoRemoto by nube {
    override suspend fun bajarVinos(usuarioId: String, desde: Int, cantidad: Int) =
        run { alPedirPagina(); nube.bajarVinos(usuarioId, desde, cantidad) }
}
