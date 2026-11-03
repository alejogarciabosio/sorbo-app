package com.sorbo.app.domain.usecase

import com.sorbo.app.data.memoria.VinoRepositoryEnMemoria
import com.sorbo.app.domain.degustacionDe
import com.sorbo.app.domain.model.Vino
import com.sorbo.app.domain.vinoDe
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuscarEnDiarioTest {

    private val repositorio = VinoRepositoryEnMemoria()
    private val buscar = BuscarEnDiario(repositorio)

    private suspend fun enElDiario(vararg vinos: Vino) =
        vinos.forEach { repositorio.guardarVinoNuevo(it, degustacionDe(it.id)) }

    private fun ResultadoBusqueda.nombres(): List<String> = when (this) {
        is ResultadoBusqueda.Encontrados -> vinos.map { it.vino.nombre }
        is ResultadoBusqueda.SoloOtraAnada -> vinos.map { it.vino.nombre }
        is ResultadoBusqueda.NoEncontrado -> emptyList()
    }

    @Test
    fun encuentraPorTextoParcialSinTildesNiMayusculas() = runTest {
        enElDiario(vinoDe("El Esteco Torrontés"))
        assertEquals(listOf("El Esteco Torrontés"), buscar("torrontes").nombres())
        assertEquals(listOf("El Esteco Torrontés"), buscar("ESTECO").nombres())
        assertEquals(listOf("El Esteco Torrontés"), buscar("torr").nombres())
    }

    @Test
    fun encuentraPorBodegaOCepa() = runTest {
        enElDiario(vinoDe("Reserva", bodega = "Zuccardi", cepa = "Malbec"))
        assertEquals(1, buscar("zuccardi").nombres().size)
        assertEquals(1, buscar("malbec").nombres().size)
    }

    @Test
    fun laConsultaVaciaNoDevuelveNada() = runTest {
        enElDiario(vinoDe("Rutini Malbec"))
        assertEquals(ResultadoBusqueda.Encontrados(emptyList()), buscar("   "))
    }

    @Test
    fun sinCoincidenciasDiceQueNoSeEncontro() = runTest {
        enElDiario(vinoDe("Rutini Malbec"))
        assertEquals(ResultadoBusqueda.NoEncontrado("zuccardi q"), buscar(" zuccardi q "))
    }

    @Test
    fun siNoEstaEsaAnadaPeroSiOtraLoAvisa() = runTest {
        enElDiario(vinoDe("Rutini Malbec", anio = 2021))

        val resultado = buscar("rutini malbec 2022")
        assertTrue(resultado is ResultadoBusqueda.SoloOtraAnada)
        resultado as ResultadoBusqueda.SoloOtraAnada
        assertEquals(2022, resultado.anioBuscado)
        assertEquals(listOf("Rutini Malbec"), resultado.nombres())
    }

    @Test
    fun siEstaEsaAnadaNoAvisaDeOtras() = runTest {
        enElDiario(vinoDe("Rutini Malbec", anio = 2021))
        assertTrue(buscar("rutini malbec 2021") is ResultadoBusqueda.Encontrados)
    }

    @Test
    fun unVinoEliminadoNoApareceEnLaBusqueda() = runTest {
        val vino = vinoDe("Rutini Malbec")
        enElDiario(vino)
        repositorio.eliminarVino(vino.id)
        assertTrue(buscar("rutini") is ResultadoBusqueda.NoEncontrado)
    }
}
