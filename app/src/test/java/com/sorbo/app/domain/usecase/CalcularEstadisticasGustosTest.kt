package com.sorbo.app.domain.usecase

import com.sorbo.app.domain.degustacionDe
import com.sorbo.app.domain.model.Comida
import com.sorbo.app.domain.model.Dulzor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalcularEstadisticasGustosTest {

    private val calcular = CalcularEstadisticasGustos()

    @Test
    fun conMenosDeCincoDegustacionesDiceCuantasFaltan() {
        val resultado = calcular(List(3) { degustacionDe("v") })
        assertFalse(resultado.habilitado)
        assertEquals(2, resultado.faltan)
        assertTrue(resultado.loQueMasDisfrutas.isEmpty())
    }

    @Test
    fun conCincoOMasSeHabilitaElAnalisis() {
        val resultado = calcular(List(5) { degustacionDe("v") })
        assertTrue(resultado.habilitado)
        assertEquals(0, resultado.faltan)
    }

    @Test
    fun cuentaCuantasDeLasQueSonSecasGustaron() {
        val degustaciones = List(8) { degustacionDe("v", meGusto = true, dulzor = Dulzor.SECO) } +
            degustacionDe("v", meGusto = false, dulzor = Dulzor.SECO)
        val fila = calcular(degustaciones).loQueMasDisfrutas.single { it.categoria == CategoriaGusto.PorDulzor(Dulzor.SECO) }
        assertEquals(8, fila.favorables)
        assertEquals(9, fila.total)
    }

    @Test
    fun separaLoQueGustaDeLoQueNoConvence() {
        val degustaciones = List(4) { degustacionDe("v", meGusto = true, comida = Comida.CARNE) } +
            List(3) { degustacionDe("v", meGusto = false, dulzor = Dulzor.DULCE) } +
            degustacionDe("v", meGusto = true, dulzor = Dulzor.DULCE)
        val resultado = calcular(degustaciones)

        assertTrue(resultado.loQueMasDisfrutas.any { it.categoria == CategoriaGusto.PorComida(Comida.CARNE) })
        assertEquals(listOf(CategoriaGusto.PorDulzor(Dulzor.DULCE)), resultado.loQueNoConvence.map { it.categoria })
    }

    @Test
    fun ignoraLasCategoriasConUnaSolaDegustacionYLasEliminadas() {
        val degustaciones = List(5) { degustacionDe("v", meGusto = true, comida = Comida.PASTA) } +
            degustacionDe("v", meGusto = true, dulzor = Dulzor.SECO) +
            degustacionDe("v", meGusto = true, dulzor = Dulzor.SECO).copy(eliminado = true)
        val resultado = calcular(degustaciones)

        assertEquals(6, resultado.totalDegustaciones)
        assertFalse(resultado.loQueMasDisfrutas.any { it.categoria == CategoriaGusto.PorDulzor(Dulzor.SECO) })
    }

    @Test
    fun ordenaLoQueMasGustaPrimero() {
        val degustaciones = List(2) { degustacionDe("v", true, comida = Comida.PASTA) } +
            List(3) { degustacionDe("v", true, comida = Comida.CARNE) } +
            degustacionDe("v", false, comida = Comida.PASTA)
        val categorias = calcular(degustaciones).loQueMasDisfrutas.map { it.categoria }
        assertEquals(CategoriaGusto.PorComida(Comida.CARNE), categorias.first())
    }
}
