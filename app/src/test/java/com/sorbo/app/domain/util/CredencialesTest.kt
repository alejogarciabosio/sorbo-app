package com.sorbo.app.domain.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CredencialesTest {

    @Test
    fun aceptaEmailsConFormatoNormal() {
        assertTrue(esEmailValido("lucia@correo.com"))
        assertTrue(esEmailValido("  lucia.perez@mail.com.ar "))
    }

    @Test
    fun rechazaEmailsSinArrobaSinDominioOConEspacios() {
        assertFalse(esEmailValido("lucia"))
        assertFalse(esEmailValido("lucia@correo"))
        assertFalse(esEmailValido("lu cia@correo.com"))
        assertFalse(esEmailValido(""))
    }

    @Test
    fun laClaveNecesitaAlMenosOchoCaracteres() {
        assertFalse(esClaveValida("1234567"))
        assertTrue(esClaveValida("12345678"))
    }
}
