package com.sorbo.app.domain.usecase

import com.sorbo.app.domain.model.BloqueTexto
import com.sorbo.app.domain.model.TextoEtiqueta
import com.sorbo.app.domain.repository.LectorEtiqueta
import com.sorbo.app.domain.repository.LecturaEtiquetaException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SugerirDatosDesdeFotoTest {

    private fun lectorQue(respuesta: () -> TextoEtiqueta) = object : LectorEtiqueta {
        override suspend fun leer(fotoPath: String) = respuesta()
    }

    @Test
    fun conTextoReconocibleDevuelveLaSugerencia() = runTest {
        val sugerir = SugerirDatosDesdeFoto(lectorQue { TextoEtiqueta(listOf(BloqueTexto("Rutini", 50f), BloqueTexto("Malbec 2021", 30f))) })

        val resultado = sugerir("foto.jpg") as ResultadoSugerencia.Exito
        assertEquals("Malbec", resultado.sugerencia.cepa)
        assertEquals(2021, resultado.sugerencia.anio)
    }

    @Test
    fun sinNadaReconocibleAvisaQueNoHayDatos() = runTest {
        val sugerir = SugerirDatosDesdeFoto(lectorQue { TextoEtiqueta(emptyList()) })
        assertEquals(ResultadoSugerencia.SinDatos, sugerir("foto.jpg"))
    }

    @Test
    fun siLaLecturaFallaDevuelveErrorEnLugarDeRomper() = runTest {
        val sugerir = SugerirDatosDesdeFoto(lectorQue { throw LecturaEtiquetaException("imagen ilegible") })

        val resultado = sugerir("foto.jpg")
        assertTrue(resultado is ResultadoSugerencia.Error)
        assertEquals("imagen ilegible", (resultado as ResultadoSugerencia.Error).causa.message)
    }
}
