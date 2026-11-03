package com.sorbo.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FotoTest {

    @Test
    fun unaFotoChicaNoSeAgranda() {
        assertEquals(Medidas(800, 600), medidasParaLadoMaximo(800, 600, 1600))
    }

    @Test
    fun unaFotoHorizontalSeAchicaPorElLadoMayorSinDeformarse() {
        assertEquals(Medidas(1600, 1200), medidasParaLadoMaximo(4000, 3000, 1600))
    }

    @Test
    fun unaFotoVerticalSeAchicaPorElLadoMayorSinDeformarse() {
        assertEquals(Medidas(1200, 1600), medidasParaLadoMaximo(3000, 4000, 1600))
    }

    @Test
    fun nuncaDejaUnLadoEnCero() {
        assertEquals(Medidas(1, 1600), medidasParaLadoMaximo(1, 100_000, 1600))
    }

    @Test
    fun elegLaCalidadMasAltaQueEntraEnElPeso() {
        val pesoPorCalidad = mapOf(90 to 900, 80 to 700, 70 to 500, 60 to 300, 50 to 200, 40 to 100)
        val probadas = mutableListOf<Int>()

        val resultado = comprimirHastaTamano(maximoBytes = 500) { calidad ->
            probadas += calidad
            ByteArray(pesoPorCalidad.getValue(calidad))
        }

        assertEquals(500, resultado?.size)
        assertEquals(listOf(90, 80, 70), probadas)
    }

    @Test
    fun siNingunaCalidadEntraDevuelveNull() {
        val resultado = comprimirHastaTamano(maximoBytes = 100) { ByteArray(1_000) }

        assertNull(resultado)
    }

    @Test
    fun elPesoMaximoPorDefectoEsDe500Kb() {
        assertEquals(512_000, TAMANO_MAXIMO_FOTO_BYTES)
        assertTrue(LADOS_MAXIMOS_FOTO_PX == LADOS_MAXIMOS_FOTO_PX.sortedDescending())
    }
}
