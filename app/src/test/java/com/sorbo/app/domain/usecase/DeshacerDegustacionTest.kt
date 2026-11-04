package com.sorbo.app.domain.usecase

import com.sorbo.app.data.memoria.VinoRepositoryEnMemoria
import com.sorbo.app.domain.AHORA
import com.sorbo.app.domain.degustacionDe
import com.sorbo.app.domain.vinoDe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.ZoneOffset

class DeshacerDegustacionTest {

    private val reloj = Clock.fixed(AHORA, ZoneOffset.UTC)
    private val repositorio = VinoRepositoryEnMemoria(reloj)

    @Test
    fun alDeshacerSeQuitaSoloLaDegustacionDeEstaVez() = runTest {
        val vino = vinoDe("Rutini Malbec 2021", anio = 2021)
        repositorio.guardarVinoNuevo(vino, degustacionDe(vino.id))
        val registrar = RegistrarDegustacion(repositorio, reloj)
        val resultado = registrar(
            FormularioRegistro(nombre = vino.nombre, meGusto = true, fecha = AHORA.atOffset(ZoneOffset.UTC).toLocalDate()),
            DestinoRegistro.VinoExistente(vino.id),
        ) as ResultadoRegistro.Guardado
        assertEquals(2, repositorio.observarVino(vino.id).first()!!.veces)

        DeshacerDegustacion(repositorio)(resultado.degustacionId)

        assertEquals(1, repositorio.observarVino(vino.id).first()!!.veces)
        assertEquals(1, repositorio.listarVinos().size)
    }
}
