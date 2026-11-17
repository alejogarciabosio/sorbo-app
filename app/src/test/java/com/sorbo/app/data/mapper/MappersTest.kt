package com.sorbo.app.data.mapper

import com.sorbo.app.domain.AHORA
import com.sorbo.app.domain.HOY
import com.sorbo.app.domain.model.Comida
import com.sorbo.app.domain.model.Compania
import com.sorbo.app.domain.model.Degustacion
import com.sorbo.app.domain.model.Dulzor
import com.sorbo.app.domain.model.EstadoSync
import com.sorbo.app.domain.model.Ocasion
import com.sorbo.app.domain.model.Vino
import org.junit.Assert.assertEquals
import org.junit.Test

class MappersTest {

    private val vino = Vino(
        id = "v1",
        nombre = "Rutini Malbec",
        creadoEn = AHORA,
        actualizadoEn = AHORA.plusSeconds(60),
        usuarioId = "u1",
        bodega = "Rutini",
        cepa = "Malbec",
        anio = 2020,
        fotoPath = "/fotos/v1.jpg",
        fotoUrl = "https://ejemplo/v1.jpg",
        estadoSync = EstadoSync.SINCRONIZADO,
        eliminado = true,
    )

    private val degustacion = Degustacion(
        id = "d1",
        vinoId = "v1",
        fecha = HOY,
        meGusto = false,
        actualizadoEn = AHORA,
        dulzor = Dulzor.SECO,
        comida = Comida.CARNE,
        compania = Compania.AMIGOS,
        ocasion = Ocasion.ASADO,
        nota = "Muy tánico",
        estadoSync = EstadoSync.PENDIENTE,
        eliminado = false,
    )

    @Test
    fun unVinoVuelveIgualDespuesDePasarPorLaEntidad() {
        assertEquals(vino, vino.aEntidad().aDominio())
    }

    @Test
    fun laEntidadDelVinoGuardaElTextoDeBusquedaNormalizado() {
        assertEquals(vino.textoBusqueda, vino.aEntidad().textoBusqueda)
        assertEquals("rutini malbec rutini malbec 2020", vino.aEntidad().textoBusqueda)
    }

    @Test
    fun unaDegustacionVuelveIgualDespuesDePasarPorLaEntidad() {
        assertEquals(degustacion, degustacion.aEntidad().aDominio())
    }
}
