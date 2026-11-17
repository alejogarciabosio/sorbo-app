package com.sorbo.app.data.remote

import com.google.gson.Gson
import com.sorbo.app.domain.model.Comida
import com.sorbo.app.domain.model.Compania
import com.sorbo.app.domain.model.Dulzor
import com.sorbo.app.domain.model.Ocasion
import com.sorbo.app.domain.usecase.CategoriaGusto
import com.sorbo.app.domain.usecase.EstadisticasGustos
import com.sorbo.app.domain.usecase.FilaGusto
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PedidoAnalisisTest {

    private fun estadisticas(disfrutas: List<FilaGusto>, noConvence: List<FilaGusto> = emptyList()) =
        EstadisticasGustos(9, habilitado = true, faltan = 0, loQueMasDisfrutas = disfrutas, loQueNoConvence = noConvence)

    @Test
    fun cadaCategoriaSeEnviaConSuTipoYElNombreDelEnum() {
        val pedido = estadisticas(
            disfrutas = listOf(
                FilaGusto(CategoriaGusto.PorDulzor(Dulzor.SECO), 8, 9),
                FilaGusto(CategoriaGusto.PorComida(Comida.CARNE), 4, 4),
                FilaGusto(CategoriaGusto.PorCompania(Compania.AMIGOS), 3, 3),
                FilaGusto(CategoriaGusto.PorOcasion(Ocasion.ASADO), 2, 2),
            ),
            noConvence = listOf(FilaGusto(CategoriaGusto.PorDulzor(Dulzor.DULCE), 0, 3)),
        ).aPedido()

        assertEquals(9, pedido.totalDegustaciones)
        assertEquals(
            listOf(
                FilaGustoDto("dulzor", "SECO", 8, 9),
                FilaGustoDto("comida", "CARNE", 4, 4),
                FilaGustoDto("compania", "AMIGOS", 3, 3),
                FilaGustoDto("ocasion", "ASADO", 2, 2),
            ),
            pedido.disfrutas,
        )
        assertEquals(listOf(FilaGustoDto("dulzor", "DULCE", 0, 3)), pedido.noConvence)
    }

    @Test
    fun otraYSinComidaNoSeEnvian() {
        val pedido = estadisticas(
            disfrutas = listOf(
                FilaGusto(CategoriaGusto.PorComida(Comida.OTRA), 3, 3),
                FilaGusto(CategoriaGusto.PorComida(Comida.SIN_COMIDA), 3, 3),
                FilaGusto(CategoriaGusto.PorCompania(Compania.OTRA), 3, 3),
                FilaGusto(CategoriaGusto.PorOcasion(Ocasion.OTRA), 3, 3),
                FilaGusto(CategoriaGusto.PorComida(Comida.PASTA), 3, 3),
            ),
        ).aPedido()

        assertEquals(listOf(FilaGustoDto("comida", "PASTA", 3, 3)), pedido.disfrutas)
    }

    @Test
    fun seEnvianComoMaximoSeisFilasPorLista() {
        val filas = Comida.entries.filter { it != Comida.OTRA && it != Comida.SIN_COMIDA }
            .map { FilaGusto(CategoriaGusto.PorComida(it), 2, 2) }

        assertEquals(7, filas.size)
        assertEquals(MAXIMO_FILAS_POR_LISTA, estadisticas(filas).aPedido().disfrutas.size)
    }

    @Test
    fun elJsonTieneLasClavesQueEsperaLaFuncion() {
        val json = Gson().toJson(estadisticas(listOf(FilaGusto(CategoriaGusto.PorDulzor(Dulzor.SECO), 8, 9))).aPedido())

        assertEquals(
            """{"totalDegustaciones":9,"disfrutas":[{"tipo":"dulzor","valor":"SECO","favorables":8,"total":9}],"noConvence":[]}""",
            json,
        )
    }

    @Test
    fun elPedidoVaALaFuncionConElTokenDelUsuario() = runTest {
        val servidor = MockWebServer().apply { start() }
        servidor.enqueue(MockResponse().setBody("""{"resumen":"Te gustan los secos.","sugerencias":["Probá un Cabernet"]}"""))
        val api = ClienteSupabase.crearFuncionesApi(servidor.url("/").toString(), "clave")

        val respuesta = api.analizarGustos("Bearer token", estadisticas(emptyList()).aPedido()).body()!!

        val pedido = servidor.takeRequest()
        assertEquals("/functions/v1/analizar-gustos", pedido.path)
        assertEquals("Bearer token", pedido.getHeader("Authorization"))
        assertEquals("clave", pedido.getHeader("apikey"))
        assertTrue(pedido.body.readUtf8().contains("\"totalDegustaciones\":9"))
        assertEquals("Te gustan los secos.", respuesta.resumen)
        assertEquals(listOf("Probá un Cabernet"), respuesta.sugerencias)
        servidor.shutdown()
    }
}
