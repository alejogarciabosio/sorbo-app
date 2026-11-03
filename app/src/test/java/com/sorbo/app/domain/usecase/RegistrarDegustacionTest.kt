package com.sorbo.app.domain.usecase

import com.sorbo.app.data.memoria.VinoRepositoryEnMemoria
import com.sorbo.app.domain.AHORA
import com.sorbo.app.domain.HOY
import com.sorbo.app.domain.model.Comida
import com.sorbo.app.domain.model.EstadoSync
import com.sorbo.app.domain.model.Dulzor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.ZoneOffset

class RegistrarDegustacionTest {

    private val reloj = Clock.fixed(AHORA, ZoneOffset.UTC)
    private val repositorio = VinoRepositoryEnMemoria(reloj)
    private var proximoId = 0
    private val registrar = RegistrarDegustacion(repositorio, reloj) { "id-${++proximoId}" }

    private fun formulario(nombre: String = "Rutini Malbec 2021", meGusto: Boolean? = true) =
        FormularioRegistro(nombre = nombre, meGusto = meGusto, fecha = HOY)

    @Test
    fun sinNombreNiOpinionIndicaQueFalta() = runTest {
        val resultado = registrar(formulario(nombre = "  ", meGusto = null), DestinoRegistro.VinoNuevo)
        assertEquals(
            ResultadoRegistro.Invalido(setOf(CampoObligatorio.NOMBRE, CampoObligatorio.OPINION)),
            resultado,
        )
        assertTrue(repositorio.listarVinos().isEmpty())
    }

    @Test
    fun conSoloLaOpinionFaltaElNombre() = runTest {
        val resultado = registrar(formulario(nombre = ""), DestinoRegistro.VinoNuevo)
        assertEquals(ResultadoRegistro.Invalido(setOf(CampoObligatorio.NOMBRE)), resultado)
    }

    @Test
    fun conSoloElNombreFaltaLaOpinion() = runTest {
        val resultado = registrar(formulario(meGusto = null), DestinoRegistro.VinoNuevo)
        assertEquals(ResultadoRegistro.Invalido(setOf(CampoObligatorio.OPINION)), resultado)
    }

    @Test
    fun unVinoNuevoQuedaPendienteDeRespaldoYSeVeEnElDiario() = runTest {
        val resultado = registrar(formulario(), DestinoRegistro.VinoNuevo) as ResultadoRegistro.Guardado
        assertTrue(resultado.vinoNuevo)

        val diario = repositorio.observarDiario().first()
        assertEquals(1, diario.size)
        assertEquals(EstadoSync.PENDIENTE, diario[0].vino.estadoSync)
        assertEquals(EstadoSync.PENDIENTE, diario[0].degustaciones.single().estadoSync)
    }

    @Test
    fun elAnioSeTomaDelNombreSiNoSeCargoAparte() = runTest {
        registrar(formulario(nombre = "Rutini Malbec 2021"), DestinoRegistro.VinoNuevo)
        assertEquals(2021, repositorio.listarVinos().single().anio)
    }

    @Test
    fun elAnioCargadoAparteTienePrioridad() = runTest {
        registrar(formulario().copy(anio = 2019), DestinoRegistro.VinoNuevo)
        assertEquals(2019, repositorio.listarVinos().single().anio)
    }

    @Test
    fun loCargadoSeLimpiaYLosCamposVaciosQuedanNulos() = runTest {
        val datos = formulario(nombre = "  Rutini  ").copy(bodega = "   ", nota = " Muy rico ", dulzor = Dulzor.SECO, comida = Comida.CARNE)
        registrar(datos, DestinoRegistro.VinoNuevo)

        val guardado = repositorio.observarDiario().first().single()
        assertEquals("Rutini", guardado.vino.nombre)
        assertNull(guardado.vino.bodega)
        assertEquals("Muy rico", guardado.degustaciones.single().nota)
        assertEquals(Dulzor.SECO, guardado.degustaciones.single().dulzor)
        assertEquals(Comida.CARNE, guardado.degustaciones.single().comida)
    }

    @Test
    fun agregarAUnVinoExistenteNoLoDuplica() = runTest {
        val primero = registrar(formulario(), DestinoRegistro.VinoNuevo) as ResultadoRegistro.Guardado
        val segundo = registrar(formulario(meGusto = false), DestinoRegistro.VinoExistente(primero.vinoId)) as ResultadoRegistro.Guardado

        assertEquals(false, segundo.vinoNuevo)
        val diario = repositorio.observarDiario().first()
        assertEquals(1, diario.size)
        assertEquals(2, diario[0].veces)
    }
}
