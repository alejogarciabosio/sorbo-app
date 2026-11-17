package com.sorbo.app.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class ConversoresTest {

    private val conversores = Conversores()

    @Test
    fun unInstanteVuelveIgualDespuesDeGuardarseComoMilisegundos() {
        val instante = Instant.parse("2026-11-17T15:30:45.123Z")
        assertEquals(instante, conversores.milisAInstant(conversores.instantAMilis(instante)))
    }

    @Test
    fun unaFechaSeGuardaComoTextoIso() {
        assertEquals("2026-03-05", conversores.fechaATexto(LocalDate.of(2026, 3, 5)))
        assertEquals(LocalDate.of(2026, 3, 5), conversores.textoAFecha("2026-03-05"))
    }

    @Test
    fun losValoresVaciosSiguenVacios() {
        assertNull(conversores.instantAMilis(null))
        assertNull(conversores.milisAInstant(null))
        assertNull(conversores.fechaATexto(null))
        assertNull(conversores.textoAFecha(null))
    }
}
