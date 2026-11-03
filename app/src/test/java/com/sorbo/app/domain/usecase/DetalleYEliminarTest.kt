package com.sorbo.app.domain.usecase

import com.sorbo.app.data.memoria.VinoRepositoryEnMemoria
import com.sorbo.app.domain.degustacionDe
import com.sorbo.app.domain.vinoDe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class DetalleYEliminarTest {

    private val repositorio = VinoRepositoryEnMemoria()

    @Test
    fun elDetalleOrdenaLasDegustacionesDeLaMasRecienteALaMasAntigua() = runTest {
        val vino = vinoDe("Rutini Malbec", bodega = "Rutini", anio = 2021)
        repositorio.guardarVinoNuevo(vino, degustacionDe(vino.id, fecha = LocalDate.of(2026, 7, 28)))
        repositorio.agregarDegustacion(degustacionDe(vino.id, fecha = LocalDate.of(2026, 9, 14)))

        val detalle = ObtenerDetalleVino(repositorio)(vino.id).first()!!
        assertEquals(
            listOf(LocalDate.of(2026, 9, 14), LocalDate.of(2026, 7, 28)),
            detalle.degustaciones.map { it.fecha },
        )
    }

    @Test
    fun elDetalleListaLasOtrasAnadasDelMismoVino() = runTest {
        val dosMilVeinte = vinoDe("Rutini Malbec", bodega = "Rutini", anio = 2020)
        val dosMilVeintiuno = vinoDe("Rutini Malbec", bodega = "Rutini", anio = 2021)
        val otroVino = vinoDe("Zuccardi Serie A", bodega = "Zuccardi", anio = 2021)
        listOf(dosMilVeinte, dosMilVeintiuno, otroVino).forEach { repositorio.guardarVinoNuevo(it, degustacionDe(it.id)) }

        val detalle = ObtenerDetalleVino(repositorio)(dosMilVeintiuno.id).first()!!
        assertEquals(listOf(dosMilVeinte.id), detalle.otrasAnadas.map { it.id })
    }

    @Test
    fun unVinoInexistenteNoTieneDetalle() = runTest {
        assertNull(ObtenerDetalleVino(repositorio)("no-existe").first())
    }

    @Test
    fun eliminarYDeshacerDevuelveElVinoConSusDegustaciones() = runTest {
        val vino = vinoDe("Rutini Malbec")
        repositorio.guardarVinoNuevo(vino, degustacionDe(vino.id))
        val eliminar = EliminarVino(repositorio)

        eliminar(vino.id)
        assertNull(ObtenerDetalleVino(repositorio)(vino.id).first())

        eliminar.deshacer(vino.id)
        assertEquals(1, ObtenerDetalleVino(repositorio)(vino.id).first()!!.degustaciones.size)
    }
}
