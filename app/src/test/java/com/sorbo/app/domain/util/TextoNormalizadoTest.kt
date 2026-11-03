package com.sorbo.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TextoNormalizadoTest {

    @Test
    fun ignoraMayusculasTildesYSignos() {
        assertEquals("el esteco torrontes", normalizar("El  Esteco, TORRONTÉS!"))
        assertEquals("anejo cabernet", normalizar("Añejo Cabernet"))
    }

    @Test
    fun unTextoSinLetrasQuedaVacio() {
        assertEquals("", normalizar("  ¿?  "))
    }

    @Test
    fun extraeElAnioDeCuatroCifras() {
        assertEquals(2021, extraerAnio("Rutini Malbec 2021"))
        assertEquals(1998, extraerAnio("Reserva 1998"))
        assertNull(extraerAnio("Malbec 750 ml"))
        assertNull(extraerAnio("Lote 12345"))
    }

    @Test
    fun normalizarSinAnioQuitaElAnio() {
        assertEquals("rutini malbec", normalizarSinAnio("Rutini Malbec 2021"))
        assertEquals("rutini malbec", normalizarSinAnio("2021 Rutini Malbec"))
    }

    @Test
    fun laConsultaCoincideSiEstanTodasSusPalabras() {
        val texto = normalizar("El Esteco Torrontés Salta")
        assertTrue(coincideConConsulta(texto, "torrontes"))
        assertTrue(coincideConConsulta(texto, "esteco tor"))
        assertFalse(coincideConConsulta(texto, "esteco malbec"))
        assertFalse(coincideConConsulta(texto, ""))
    }
}
