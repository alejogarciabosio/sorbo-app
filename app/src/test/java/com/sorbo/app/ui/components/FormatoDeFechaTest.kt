package com.sorbo.app.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class FormatoDeFechaTest {

    private val argentina = Locale.forLanguageTag("es-AR")
    private val hoy = LocalDate.of(2026, 11, 4)

    @Test
    fun enElAnioEnCursoNoRepiteElAnio() {
        assertEquals("12 de septiembre", textoDeFecha(LocalDate.of(2026, 9, 12), hoy, argentina))
    }

    @Test
    fun enOtroAnioLoAgrega() {
        assertEquals("28 de julio de 2025", textoDeFecha(LocalDate.of(2025, 7, 28), hoy, argentina))
    }

    @Test
    fun elDiaNoLlevaCeroALaIzquierda() {
        assertEquals("1 de noviembre", textoDeFecha(LocalDate.of(2026, 11, 1), hoy, argentina))
    }
}
