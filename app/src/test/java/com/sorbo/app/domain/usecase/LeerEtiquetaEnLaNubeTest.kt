package com.sorbo.app.domain.usecase

import com.sorbo.app.data.memoria.ConectividadSimulada
import com.sorbo.app.data.memoria.PreferenciasRepositoryEnMemoria
import com.sorbo.app.data.memoria.SesionRepositoryEnMemoria
import com.sorbo.app.domain.model.LecturaNube
import com.sorbo.app.domain.repository.LectorEtiquetaNube
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LeerEtiquetaEnLaNubeTest {

    private val zuccardi = LecturaNube(
        nombre = "José Zuccardi",
        bodega = "José Zuccardi",
        cepa = "Malbec",
        anio = 2017,
        lineas = listOf("JOSÉ", "ZUCCARDI", "MALBEC", "VALLE DE UCO - ARGENTINA", "2017"),
    )

    // ---- Los controles sobre lo que devuelve el modelo ----------------------------------------------------------

    @Test
    fun aceptaLoQueFiguraEnLosRenglonesLeidos() {
        val sugerencia = validarLecturaNube(zuccardi, anioMaximo = 2027)

        assertEquals("José Zuccardi", sugerencia.nombre)
        assertEquals("José Zuccardi", sugerencia.bodega)
        assertEquals("Malbec", sugerencia.cepa)
        assertEquals(2017, sugerencia.anio)
    }

    @Test
    fun descartaUnDatoInventadoQueNoEstaEnLosRenglones() {
        val sugerencia = validarLecturaNube(zuccardi.copy(nombre = "Gran Reserva Especial"), anioMaximo = 2027)

        assertNull(sugerencia.nombre)
        assertEquals("Malbec", sugerencia.cepa)
    }

    @Test
    fun descartaUnAnioQueNoFiguraOQueNoEsPlausible() {
        assertNull(validarLecturaNube(zuccardi.copy(anio = 2024), anioMaximo = 2027).anio)
        assertNull(validarLecturaNube(zuccardi.copy(anio = 1850, lineas = zuccardi.lineas + "1850"), anioMaximo = 2027).anio)
        assertNull(validarLecturaNube(zuccardi.copy(anio = 2090, lineas = zuccardi.lineas + "2090"), anioMaximo = 2027).anio)
    }

    @Test
    fun sinRenglonesNoHayNadaQueComprobarYSeDescartaTodo() {
        assertTrue(validarLecturaNube(zuccardi.copy(lineas = emptyList()), anioMaximo = 2027).estaVacia)
    }

    @Test
    fun laCepaEnMayusculasSeMuestraConFormato() {
        val lectura = LecturaNube(null, null, "CABERNET SAUVIGNON", null, listOf("TRUMPETER", "CABERNET SAUVIGNON"))

        assertEquals("Cabernet Sauvignon", validarLecturaNube(lectura, anioMaximo = 2027).cepa)
    }

    @Test
    fun unaCepaDeCorteConConectoresSeAceptaYSeFormatea() {
        val lectura = LecturaNube(
            nombre = "PUNTA CORRAL",
            bodega = "FERNANDO DUPONT",
            cepa = "MALBEC y SYRAH y CABERNET SAUVIGNON",
            anio = 2014,
            lineas = listOf("PUNTA CORRAL", "2014", "MALBEC", "SYRAH", "CABERNET SAUVIGNON", "FERNANDO DUPONT"),
        )

        assertEquals("Malbec y Syrah y Cabernet Sauvignon", validarLecturaNube(lectura, anioMaximo = 2027).cepa)
    }

    @Test
    fun unConectorSoloNoAlcanzaParaValidarUnDato() {
        val lectura = LecturaNube(null, null, "Merlot y Tannat", null, listOf("MALBEC", "2020"))

        assertNull(validarLecturaNube(lectura, anioMaximo = 2027).cepa)
    }

    @Test
    fun elNombreEnMayusculasSeRespetaTalCualSeLee() {
        val lectura = LecturaNube("DESierto PAMPA", null, null, null, listOf("DESierto", "PAMPA"))

        assertEquals("DESierto PAMPA", validarLecturaNube(lectura, anioMaximo = 2027).nombre)
    }

    // ---- El caso de uso -----------------------------------------------------------------------------------------

    private class LectorNubeFalso(var respuesta: Result<LecturaNube>, var demoraMs: Long = 0) : LectorEtiquetaNube {
        var llamadas = 0
        override suspend fun leer(fotoPath: String): Result<LecturaNube> {
            llamadas++
            delay(demoraMs)
            return respuesta
        }
    }

    private val sesion = SesionRepositoryEnMemoria()
    private val preferencias = PreferenciasRepositoryEnMemoria()
    private val conectividad = ConectividadSimulada()

    private suspend fun conTodoListo() {
        sesion.crearCuenta("ana@correo.com", "Clave-segura-1")
        preferencias.aceptarAvisoIa()
    }

    private fun caso(lector: LectorNubeFalso, esperaMs: Long = 5_000) =
        LeerEtiquetaEnLaNube(PuedeLeerEnLaNube(sesion, preferencias, conectividad), lector, esperaMs) { 2027 }

    @Test
    fun conPermisoDevuelveLaSugerenciaVerificada() = runTest {
        conTodoListo()
        val lector = LectorNubeFalso(Result.success(zuccardi))

        val sugerencia = caso(lector)("foto.jpg")

        assertEquals(2017, sugerencia?.anio)
        assertEquals(1, lector.llamadas)
    }

    @Test
    fun sinPermisoNiSiquieraSeEnviaLaFoto() = runTest {
        val lector = LectorNubeFalso(Result.success(zuccardi))

        assertNull(caso(lector)("foto.jpg"))
        assertEquals(0, lector.llamadas)
    }

    @Test
    fun siLaNubeFallaDevuelveNullSinTirarError() = runTest {
        conTodoListo()
        val lector = LectorNubeFalso(Result.failure(RuntimeException("sin servicio")))

        assertNull(caso(lector)("foto.jpg"))
    }

    @Test
    fun siTardaMasDeLoPermitidoSeDescarta() = runTest {
        conTodoListo()
        val lector = LectorNubeFalso(Result.success(zuccardi), demoraMs = 10_000)

        assertNull(caso(lector, esperaMs = 5_000)("foto.jpg"))
    }

    @Test
    fun siNingunDatoPasaLosControlesDevuelveNull() = runTest {
        conTodoListo()
        val lector = LectorNubeFalso(Result.success(zuccardi.copy(lineas = emptyList())))

        assertNull(caso(lector)("foto.jpg"))
    }
}
