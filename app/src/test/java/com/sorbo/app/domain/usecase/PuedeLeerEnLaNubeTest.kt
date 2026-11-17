package com.sorbo.app.domain.usecase

import com.sorbo.app.data.memoria.ConectividadSimulada
import com.sorbo.app.data.memoria.PreferenciasRepositoryEnMemoria
import com.sorbo.app.data.memoria.SesionRepositoryEnMemoria
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PuedeLeerEnLaNubeTest {

    private val sesion = SesionRepositoryEnMemoria()
    private val preferencias = PreferenciasRepositoryEnMemoria()
    private val conectividad = ConectividadSimulada()
    private val puede = PuedeLeerEnLaNube(sesion, preferencias, conectividad)

    private suspend fun cumplirTodo() {
        sesion.crearCuenta("ana@correo.com", "Clave-segura-1")
        preferencias.aceptarAvisoIa()
    }

    @Test
    fun conLasCuatroCondicionesPuede() = runTest {
        cumplirTodo()
        assertTrue(puede())
    }

    @Test
    fun sinSesionNoPuede() = runTest {
        preferencias.aceptarAvisoIa()
        assertFalse(puede())
    }

    @Test
    fun sinAceptarElAvisoNoPuede() = runTest {
        sesion.crearCuenta("ana@correo.com", "Clave-segura-1")
        assertFalse(puede())
    }

    @Test
    fun conLaIaDesactivadaNoPuede() = runTest {
        cumplirTodo()
        preferencias.cambiarIaActivada(false)
        assertFalse(puede())
    }

    @Test
    fun sinConexionNoPuede() = runTest {
        cumplirTodo()
        conectividad.cambiar(false)
        assertFalse(puede())
    }
}
