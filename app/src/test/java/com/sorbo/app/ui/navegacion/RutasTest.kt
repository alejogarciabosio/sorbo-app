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
    fun registrarSinNombreVaALaRutaSimple() {
        assertEquals("registrar", Rutas.registrar())
        assertEquals("registrar", Rutas.registrar("   "))
    }

    @Test
    fun registrarConNombreLoCodificaParaLaRuta() {
        assertEquals("registrar?nombre=xyz", Rutas.registrar("xyz"))
        assertEquals("registrar?nombre=Rutini%20Malbec", Rutas.registrar("  Rutini Malbec "))
        assertEquals("registrar?nombre=Torront%C3%A9s%20%2F%20%26%20Cia", Rutas.registrar("Torrontés / & Cia"))
    }

    @Test
    fun cadaDestinoDeLaBarraTieneUnaRutaPrincipal() {
        assertEquals(Rutas.principales, DestinoPrincipal.entries.map { it.ruta })
    }
}
