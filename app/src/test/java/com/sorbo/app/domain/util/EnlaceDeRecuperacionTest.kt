package com.sorbo.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EnlaceDeRecuperacionTest {

    @Test
    fun leeLosDosTokensDelEnlaceDeRecuperacion() {
        val tokens = tokensDelEnlace("access_token=aaa.bbb.ccc&expires_in=3600&refresh_token=rrr&token_type=bearer&type=recovery")

        assertEquals(TokensDeEnlace("aaa.bbb.ccc", "rrr"), tokens)
    }

    @Test
    fun siFaltaUnTokenNoSirve() {
        assertNull(tokensDelEnlace("access_token=aaa&type=recovery"))
        assertNull(tokensDelEnlace("refresh_token=rrr&type=recovery"))
    }

    @Test
    fun unEnlaceVencidoNoSirve() {
        assertNull(tokensDelEnlace("error=access_denied&error_code=otp_expired&error_description=Email+link+is+invalid+or+has+expired"))
    }

    @Test
    fun soloSirveElEnlaceDeRecuperacion() {
        assertNull(tokensDelEnlace("access_token=aaa&refresh_token=rrr&type=signup"))
    }

    @Test
    fun sinFragmentoNoHayNada() {
        assertNull(tokensDelEnlace(null))
        assertNull(tokensDelEnlace(""))
    }
}
