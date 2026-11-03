package com.sorbo.app.domain.usecase

import com.sorbo.app.data.memoria.VinoRepositoryEnMemoria
import com.sorbo.app.domain.degustacionDe
import com.sorbo.app.domain.model.Vino
import com.sorbo.app.domain.vinoDe
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BuscarVinoParecidoTest {

    private val repositorio = VinoRepositoryEnMemoria()
    private val buscar = BuscarVinoParecido(repositorio)

    private suspend fun enElDiario(vararg vinos: Vino) =
        vinos.forEach { repositorio.guardarVinoNuevo(it, degustacionDe(it.id)) }

    @Test
    fun conElDiarioVacioEsUnVinoNuevo() = runTest {
        val resultado = buscar(vinoDe("Rutini Malbec", anio = 2021))
        assertEquals(TipoCoincidencia.NUEVO, resultado.tipo)
        assertNull(resultado.vino)
    }

    @Test
    fun mismoNombreYMismaAnadaEsElMismoVino() = runTest {
        val guardado = vinoDe("Rutini Malbec", anio = 2021)
        enElDiario(guardado)

        val resultado = buscar(vinoDe("rutini  MALBEC", anio = 2021))
        assertEquals(TipoCoincidencia.MISMO_VINO, resultado.tipo)
        assertEquals(guardado.id, resultado.vino?.id)
    }

    @Test
    fun ninguneTieneAnadaTambienEsElMismoVino() = runTest {
        enElDiario(vinoDe("Rutini Malbec"))
        assertEquals(TipoCoincidencia.MISMO_VINO, buscar(vinoDe("Rutini Malbec")).tipo)
    }

    @Test
    fun unaLetraDistintaEsParecido() = runTest {
        enElDiario(vinoDe("Rutini Malbec", anio = 2021))
        val resultado = buscar(vinoDe("Rutini Malbek", anio = 2021))
        assertEquals(TipoCoincidencia.PARECIDO, resultado.tipo)
    }

    @Test
    fun unNombreContenidoEnOtroEsParecido() = runTest {
        enElDiario(vinoDe("Rutini Malbec Reserva"))
        assertEquals(TipoCoincidencia.PARECIDO, buscar(vinoDe("Rutini Malbec")).tipo)
    }

    @Test
    fun mismoNombreConLaAnadaCargadaSoloEnUnoEsParecido() = runTest {
        enElDiario(vinoDe("Rutini Malbec", anio = 2021))
        assertEquals(TipoCoincidencia.PARECIDO, buscar(vinoDe("Rutini Malbec")).tipo)
    }

    @Test
    fun anadasDistintasSonVinosDistintosYSeAvisaDeLasOtrasAnadas() = runTest {
        val dosMilVeintiuno = vinoDe("Rutini Malbec", anio = 2021)
        enElDiario(dosMilVeintiuno)

        val resultado = buscar(vinoDe("Rutini Malbec", anio = 2022))
        assertEquals(TipoCoincidencia.NUEVO, resultado.tipo)
        assertEquals(listOf(dosMilVeintiuno.id), resultado.otrasAnadas.map { it.id })
    }

    @Test
    fun bodegasDistintasSonVinosDistintos() = runTest {
        enElDiario(vinoDe("Malbec Reserva", bodega = "Rutini"))
        val resultado = buscar(vinoDe("Malbec Reserva", bodega = "Zuccardi"))
        assertEquals(TipoCoincidencia.NUEVO, resultado.tipo)
        assertEquals(emptyList<Vino>(), resultado.otrasAnadas)
    }

    @Test
    fun nombresSinRelacionSonVinosNuevos() = runTest {
        enElDiario(vinoDe("Rutini Malbec"))
        assertEquals(TipoCoincidencia.NUEVO, buscar(vinoDe("Zuccardi Serie A")).tipo)
    }

    @Test
    fun siHayVariosParecidosEligeElMasCercano() = runTest {
        val cercano = vinoDe("Rutini Malbec Reserva")
        enElDiario(vinoDe("Rutini Malbec Reserva Especial Edicion Limitada"), cercano)

        val resultado = buscar(vinoDe("Rutini Malbec"))
        assertEquals(cercano.id, resultado.vino?.id)
    }

    @Test
    fun noSeComparaConLosVinosEliminados() = runTest {
        val eliminado = vinoDe("Rutini Malbec", anio = 2021)
        enElDiario(eliminado)
        repositorio.eliminarVino(eliminado.id)

        assertEquals(TipoCoincidencia.NUEVO, buscar(vinoDe("Rutini Malbec", anio = 2021)).tipo)
    }
}
