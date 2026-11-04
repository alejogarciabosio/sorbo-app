package com.sorbo.app.data.memoria

import com.sorbo.app.domain.AHORA
import com.sorbo.app.domain.repository.AnalisisException
import com.sorbo.app.domain.repository.MotivoAnalisis
import com.sorbo.app.domain.usecase.EstadisticasGustos
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Clock
import java.time.ZoneOffset

class AnalisisGustosRepositoryEnMemoriaTest {

    private val repositorio = AnalisisGustosRepositoryEnMemoria(Clock.fixed(AHORA, ZoneOffset.UTC))
    private val estadisticas = EstadisticasGustos(totalDegustaciones = 6, habilitado = true, faltan = 0, loQueMasDisfrutas = emptyList(), loQueNoConvence = emptyList())

    @Test
    fun alPrincipioNoHayResumenGuardado() = runTest {
        assertNull(repositorio.observarUltimo().first())
    }

    @Test
    fun alGenerarQuedaGuardadoConSuFechaYLaCantidadDeDegustaciones() = runTest {
        val analisis = repositorio.generar(estadisticas).getOrThrow()

        assertEquals(analisis, repositorio.observarUltimo().first())
        assertEquals(AHORA, analisis.generadoEn)
        assertEquals(6, analisis.cantidadDegustaciones)
    }

    @Test
    fun siFallaSeConservaElResumenAnteriorYSeInformaElMotivo() = runTest {
        val anterior = repositorio.generar(estadisticas).getOrThrow()
        repositorio.fallaConMotivo = MotivoAnalisis.SIN_CONEXION

        val resultado = repositorio.generar(estadisticas)

        assertEquals(MotivoAnalisis.SIN_CONEXION, (resultado.exceptionOrNull() as AnalisisException).motivo)
        assertEquals(anterior, repositorio.observarUltimo().first())
    }
}
