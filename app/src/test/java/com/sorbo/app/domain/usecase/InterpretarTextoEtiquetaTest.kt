package com.sorbo.app.domain.usecase

import com.sorbo.app.domain.model.BloqueTexto
import com.sorbo.app.domain.model.TextoEtiqueta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InterpretarTextoEtiquetaTest {

    private val interpretar = InterpretarTextoEtiqueta()

    private fun etiqueta(vararg bloques: Pair<String, Float>) =
        TextoEtiqueta(bloques.map { BloqueTexto(it.first, it.second) })

    @Test
    fun leeNombreBodegaCepaYAnio() {
        val resultado = interpretar(
            etiqueta(
                "RUTINI" to 80f,
                "Bodegas Esmeralda" to 30f,
                "Malbec" to 40f,
                "Cosecha 2021" to 20f,
                "Alc. 14% vol - Producto argentino" to 12f,
            ),
        )
        assertEquals("RUTINI", resultado.nombre)
        assertEquals("Bodegas Esmeralda", resultado.bodega)
        assertEquals("Malbec", resultado.cepa)
        assertEquals(2021, resultado.anio)
    }

    @Test
    fun elNombreEsElTextoMasGrandeQueNoSeaCepaNiRuido() {
        val resultado = interpretar(
            etiqueta(
                "Cabernet Sauvignon" to 90f,
                "El Esteco" to 60f,
                "750 ml" to 50f,
                "2019" to 45f,
            ),
        )
        assertEquals("El Esteco", resultado.nombre)
        assertEquals("Cabernet Sauvignon", resultado.cepa)
        assertEquals(2019, resultado.anio)
    }

    @Test
    fun laCepaMasEspecificaGanaSobreLaGeneral() {
        assertEquals("Cabernet Franc", interpretar(etiqueta("Reserva Cabernet Franc" to 30f)).cepa)
    }

    @Test
    fun reconoceLaCepaConTildesYMayusculas() {
        assertEquals("Torrontés", interpretar(etiqueta("TORRONTES" to 30f, "Salta" to 20f)).cepa)
    }

    @Test
    fun separaLosRenglonesDeUnMismoBloque() {
        val resultado = interpretar(etiqueta("Zuccardi\nSerie A\nMalbec 2020" to 50f))
        assertEquals("Malbec", resultado.cepa)
        assertEquals(2020, resultado.anio)
    }

    @Test
    fun siNoHayNadaReconocibleLaSugerenciaQuedaVacia() {
        val resultado = interpretar(etiqueta("12" to 20f, "ml" to 10f, "  " to 5f))
        assertTrue(resultado.estaVacia)
        assertNull(resultado.nombre)
    }

    @Test
    fun sinTextoLaSugerenciaQuedaVacia() {
        assertTrue(interpretar(TextoEtiqueta(emptyList())).estaVacia)
    }
}
