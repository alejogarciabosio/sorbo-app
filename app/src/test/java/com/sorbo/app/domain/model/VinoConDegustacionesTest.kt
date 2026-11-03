package com.sorbo.app.domain.model

import com.sorbo.app.domain.degustacionDe
import com.sorbo.app.domain.vinoDe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class VinoConDegustacionesTest {

    private val vino = vinoDe("Rutini Malbec")

    @Test
    fun cuentaLasVecesYBuscaLaUltimaFecha() {
        val conDegustaciones = VinoConDegustaciones(
            vino,
            listOf(
                degustacionDe(vino.id, fecha = LocalDate.of(2026, 7, 28)),
                degustacionDe(vino.id, fecha = LocalDate.of(2026, 9, 14)),
            ),
        )
        assertEquals(2, conDegustaciones.veces)
        assertEquals(LocalDate.of(2026, 9, 14), conDegustaciones.ultimaFecha)
    }

    @Test
    fun sinDegustacionesNoHayUltimaFecha() {
        assertNull(VinoConDegustaciones(vino, emptyList()).ultimaFecha)
    }

    @Test
    fun gustaEnGeneralSiGustoEnLaMitadOMas() {
        val mitad = VinoConDegustaciones(vino, listOf(degustacionDe(vino.id, true), degustacionDe(vino.id, false)))
        val minoria = VinoConDegustaciones(vino, listOf(degustacionDe(vino.id, true), degustacionDe(vino.id, false), degustacionDe(vino.id, false)))
        assertTrue(mitad.gustoEnGeneral)
        assertFalse(minoria.gustoEnGeneral)
    }
}
