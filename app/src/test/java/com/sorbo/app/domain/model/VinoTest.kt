package com.sorbo.app.domain.model

import com.sorbo.app.domain.vinoDe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VinoTest {

    @Test
    fun elNombreNormalizadoNoTieneAnioNiTildes() {
        assertEquals("el esteco torrontes", vinoDe("El Esteco Torrontés 2022").nombreNormalizado)
    }

    @Test
    fun elTextoDeBusquedaJuntaNombreBodegaCepaYAnio() {
        val vino = vinoDe("Rutini", bodega = "Rutini Wines", cepa = "Malbec", anio = 2021)
        assertEquals("rutini rutini wines malbec 2021", vino.textoBusqueda)
    }

    @Test
    fun lasBodegasSoloSonDistintasSiEstanCargadasEnLosDos() {
        val a = vinoDe("Malbec", bodega = "Rutini")
        assertTrue(a.tieneOtraBodegaQue(vinoDe("Malbec", bodega = "Zuccardi")))
        assertFalse(a.tieneOtraBodegaQue(vinoDe("Malbec", bodega = "rutini")))
        assertFalse(a.tieneOtraBodegaQue(vinoDe("Malbec")))
    }

    @Test
    fun lasAnadasSoloSonDistintasSiEstanCargadasEnLosDos() {
        val a = vinoDe("Malbec", anio = 2020)
        assertTrue(a.tieneOtraAnadaQue(vinoDe("Malbec", anio = 2021)))
        assertFalse(a.tieneOtraAnadaQue(vinoDe("Malbec", anio = 2020)))
        assertFalse(a.tieneOtraAnadaQue(vinoDe("Malbec")))
    }

    @Test
    fun esOtraAnadaSiCoincideElNombreYLaBodegaPeroNoElAnio() {
        val dosMilVeinte = vinoDe("Rutini Malbec", bodega = "Rutini", anio = 2020)
        assertTrue(dosMilVeinte.esOtraAnadaDe(vinoDe("Rutini Malbec", bodega = "Rutini", anio = 2021)))
        assertFalse(dosMilVeinte.esOtraAnadaDe(vinoDe("Rutini Malbec", bodega = "Otra", anio = 2021)))
        assertFalse(dosMilVeinte.esOtraAnadaDe(vinoDe("Otro vino", bodega = "Rutini", anio = 2021)))
    }
}
