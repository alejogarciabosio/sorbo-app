package com.sorbo.app.data.memoria

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PreferenciasRepositoryEnMemoriaTest {

    private val preferencias = PreferenciasRepositoryEnMemoria()

    @Test
    fun alPrincipioNadaEstaVistoNiAceptadoYLaIaEstaActivada() = runTest {
        assertFalse(preferencias.bienvenidaVista.first())
        assertFalse(preferencias.avisoIaAceptado.first())
        assertTrue(preferencias.iaActivada.first())
    }

    @Test
    fun guardaLoQueElUsuarioElige() = runTest {
        preferencias.marcarBienvenidaVista()
        preferencias.aceptarAvisoIa()
        preferencias.cambiarIaActivada(false)

        assertTrue(preferencias.bienvenidaVista.first())
        assertTrue(preferencias.avisoIaAceptado.first())
        assertFalse(preferencias.iaActivada.first())
    }

    @Test
    fun lasBusquedasRecientesSeAgreganYSeBorran() = runTest {
        preferencias.agregarBusquedaReciente("rutini")
        preferencias.agregarBusquedaReciente("malbec")
        assertEquals(listOf("malbec", "rutini"), preferencias.busquedasRecientes.first())

        preferencias.borrarBusquedasRecientes()
        assertEquals(emptyList<String>(), preferencias.busquedasRecientes.first())
    }
}
