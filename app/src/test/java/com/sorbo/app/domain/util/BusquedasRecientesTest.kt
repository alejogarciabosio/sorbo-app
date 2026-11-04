package com.sorbo.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class BusquedasRecientesTest {

    @Test
    fun laBusquedaNuevaQuedaPrimera() {
        assertEquals(listOf("malbec", "rutini"), agregarBusquedaReciente(listOf("rutini"), "malbec"))
    }

    @Test
    fun noAgregaTextoVacioNiSoloEspacios() {
        val actuales = listOf("rutini")
        assertEquals(actuales, agregarBusquedaReciente(actuales, "   "))
    }

    @Test
    fun unaBusquedaRepetidaSubeAlPrincipioSinDuplicarse() {
        val resultado = agregarBusquedaReciente(listOf("malbec", "Torrontés", "rutini"), "torrontes")
        assertEquals(listOf("torrontes", "malbec", "rutini"), resultado)
    }

    @Test
    fun conservaSoloLasUltimasCinco() {
        var recientes = emptyList<String>()
        listOf("uno", "dos", "tres", "cuatro", "cinco", "seis").forEach { recientes = agregarBusquedaReciente(recientes, it) }
        assertEquals(listOf("seis", "cinco", "cuatro", "tres", "dos"), recientes)
    }
}
