package com.sorbo.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class DistanciaTest {

    @Test
    fun textosIgualesTienenDistanciaCero() = assertEquals(0, distanciaEntre("malbec", "malbec"))

    @Test
    fun unaLetraDistintaSumaUno() = assertEquals(1, distanciaEntre("malbec", "malbek"))

    @Test
    fun cuentaAgregadosYQuitados() {
        assertEquals(2, distanciaEntre("rutini", "rutinii "))
        assertEquals(3, distanciaEntre("", "abc"))
        assertEquals(3, distanciaEntre("abc", ""))
    }

    @Test
    fun nombresDistintosTienenDistanciaGrande() {
        assertEquals(true, distanciaEntre("rutini malbec", "zuccardi serie a") > 2)
    }
}
