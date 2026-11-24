package com.sorbo.app.domain.usecase

import com.sorbo.app.data.memoria.VinoRepositoryEnMemoria
import com.sorbo.app.domain.degustacionDe
import com.sorbo.app.domain.vinoDe
import com.sorbo.app.ui.viewmodels.AlmacenFotosFalso
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LimpiarFotosHuerfanasTest {

    private val vinos = VinoRepositoryEnMemoria()
    private val fotos = AlmacenFotosFalso()
    private val limpiar = LimpiarFotosHuerfanas(vinos, fotos)

    private suspend fun fotoGuardada(): String = fotos.guardar("content://origen").getOrThrow()

    @Test
    fun borraLasFotosQueNingunVinoUsa() = runTest {
        val enUso = fotoGuardada()
        val huerfana = fotoGuardada()
        val vino = vinoDe("Rutini Malbec").copy(fotoPath = enUso)
        vinos.guardarVinoNuevo(vino, degustacionDe(vino.id))

        assertEquals(1, limpiar())

        assertEquals(listOf(huerfana), fotos.borradas)
        assertEquals(listOf(enUso), fotos.listar())
    }

    @Test
    fun sinFotosHuerfanasNoBorraNada() = runTest {
        val enUso = fotoGuardada()
        val vino = vinoDe("Rutini Malbec").copy(fotoPath = enUso)
        vinos.guardarVinoNuevo(vino, degustacionDe(vino.id))

        assertEquals(0, limpiar())
        assertEquals(emptyList<String>(), fotos.borradas)
    }

    @Test
    fun laFotoDeUnVinoEliminadoSeBorra() = runTest {
        val foto = fotoGuardada()
        val vino = vinoDe("Rutini Malbec").copy(fotoPath = foto)
        vinos.guardarVinoNuevo(vino, degustacionDe(vino.id))
        vinos.eliminarVino(vino.id)

        assertEquals(1, limpiar())
        assertEquals(listOf(foto), fotos.borradas)
    }

    @Test
    fun conElDiarioVacioBorraTodas() = runTest {
        fotoGuardada()
        fotoGuardada()

        assertEquals(2, limpiar())
        assertEquals(emptyList<String>(), fotos.listar())
    }
}
