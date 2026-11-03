package com.sorbo.app.ui.navegacion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RutasTest {

    @Test
    fun laBarraInferiorSoloApareceEnLasPantallasPrincipales() {
        assertTrue(Rutas.mostrarBarraInferior(Rutas.DIARIO))
        assertTrue(Rutas.mostrarBarraInferior(Rutas.GUSTOS))
        assertTrue(Rutas.mostrarBarraInferior(Rutas.AJUSTES))
        assertFalse(Rutas.mostrarBarraInferior(Rutas.REGISTRAR))
        assertFalse(Rutas.mostrarBarraInferior(Rutas.DETALLE))
        assertFalse(Rutas.mostrarBarraInferior(null))
    }

    @Test
    fun lasRutasConArgumentoSeArmanConElIdDelVino() {
        assertEquals("detalle/abc", Rutas.detalle("abc"))
        assertEquals("editar/abc", Rutas.editar("abc"))
        assertEquals("volver_a_probar/abc", Rutas.volverAProbar("abc"))
    }

    @Test
    fun cadaDestinoDeLaBarraTieneUnaRutaPrincipal() {
        assertEquals(Rutas.principales, DestinoPrincipal.entries.map { it.ruta })
    }
}
