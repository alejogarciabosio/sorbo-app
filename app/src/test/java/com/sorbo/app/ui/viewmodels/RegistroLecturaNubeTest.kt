// Pruebas · ViewModel de Registrar: mejora opcional de la lectura de la etiqueta con la nube, con dobles de prueba.
package com.sorbo.app.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.sorbo.app.data.memoria.ConectividadSimulada
import com.sorbo.app.data.memoria.PreferenciasRepositoryEnMemoria
import com.sorbo.app.data.memoria.SesionRepositoryEnMemoria
import com.sorbo.app.data.memoria.VinoRepositoryEnMemoria
import com.sorbo.app.domain.AHORA
import com.sorbo.app.domain.model.LecturaNube
import com.sorbo.app.domain.repository.LectorEtiquetaNube
import com.sorbo.app.domain.usecase.BuscarVinoParecido
import com.sorbo.app.domain.usecase.LeerEtiquetaEnLaNube
import com.sorbo.app.domain.usecase.ObtenerDetalleVino
import com.sorbo.app.domain.usecase.PuedeLeerEnLaNube
import com.sorbo.app.domain.usecase.RegistrarDegustacion
import com.sorbo.app.domain.usecase.SugerirDatosDesdeFoto
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class RegistroLecturaNubeTest {

    /** Lector de la nube que responde lo que indique el test; si [puerta] tiene valor, espera a que se complete. */
    private class LectorNubeFalso : LectorEtiquetaNube {
        var respuesta: Result<LecturaNube> = Result.success(ZUCCARDI)
        var puerta: CompletableDeferred<Unit>? = null
        var llamadas = 0
        override suspend fun leer(fotoPath: String): Result<LecturaNube> {
            llamadas++
            puerta?.await()
            return respuesta
        }
    }

    private companion object {
        val ZUCCARDI = LecturaNube(
            nombre = "José Zuccardi",
            bodega = "José Zuccardi",
            cepa = "Malbec",
            anio = 2017,
            lineas = listOf("JOSÉ", "ZUCCARDI", "MALBEC", "2017"),
        )
    }

    private val reloj = Clock.fixed(AHORA, ZoneOffset.UTC)
    private val repositorio = VinoRepositoryEnMemoria(reloj)
    private val lectorLocal = LectorEtiquetaFalso()
    private val lectorNube = LectorNubeFalso()
    private val sesion = SesionRepositoryEnMemoria()
    private val preferencias = PreferenciasRepositoryEnMemoria()

    @Before
    fun antes() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun despues() = Dispatchers.resetMain()

    private suspend fun cumplirLasCondiciones() {
        sesion.crearCuenta("ana@correo.com", "Clave-segura-1")
        preferencias.aceptarAvisoIa()
    }

    private fun crearViewModel() = RegistroViewModel(
        buscarVinoParecido = BuscarVinoParecido(repositorio),
        registrarDegustacion = RegistrarDegustacion(repositorio, reloj),
        obtenerDetalleVino = ObtenerDetalleVino(repositorio),
        almacenFotos = AlmacenFotosFalso(),
        sugerirDatosDesdeFoto = SugerirDatosDesdeFoto(lectorLocal),
        reloj = reloj,
        savedStateHandle = SavedStateHandle(),
        leerEtiquetaEnLaNube = LeerEtiquetaEnLaNube(
            PuedeLeerEnLaNube(sesion, preferencias, ConectividadSimulada()),
            lectorNube,
        ) { 2027 },
    )

    private fun RegistroViewModel.campos() = (estado.value as EstadoRegistro.Editando).campos

    @Test
    fun laNubeMejoraLoQueSugirioElTelefono() = runTest {
        cumplirLasCondiciones()
        val viewModel = crearViewModel()

        viewModel.alElegirFoto("origen") // ML Kit lee "RUTINI / Malbec / 2021 / Rutini Wines"

        val campos = viewModel.campos()
        assertEquals("José Zuccardi", campos.nombre)
        assertEquals("José Zuccardi", campos.bodega)
        assertEquals("Malbec", campos.cepa)
        assertEquals("2017", campos.anio)
        assertEquals(CampoSugerible.entries.toSet(), campos.sugeridos)
        assertEquals(LecturaEtiqueta.Inactiva, campos.lectura)
    }

    @Test
    fun sinPermisoNoSeLlamaALaNubeYQuedaLoDelTelefono() = runTest {
        val viewModel = crearViewModel() // sin sesión

        viewModel.alElegirFoto("origen")

        assertEquals(0, lectorNube.llamadas)
        assertEquals("RUTINI", viewModel.campos().nombre)
    }

    @Test
    fun siLaNubeFallaQuedaLoDelTelefonoSinAvisarDeError() = runTest {
        cumplirLasCondiciones()
        lectorNube.respuesta = Result.failure(RuntimeException("sin servicio"))
        val viewModel = crearViewModel()

        viewModel.alElegirFoto("origen")

        val campos = viewModel.campos()
        assertEquals("RUTINI", campos.nombre)
        assertEquals("2021", campos.anio)
        assertEquals(LecturaEtiqueta.Inactiva, campos.lectura)
    }

    @Test
    fun loQueLaPersonaEscribioNuncaSePisa() = runTest {
        cumplirLasCondiciones()
        val viewModel = crearViewModel()
        viewModel.alCambiarNombre("Mi vino favorito")

        viewModel.alElegirFoto("origen")

        val campos = viewModel.campos()
        assertEquals("Mi vino favorito", campos.nombre)
        assertEquals("José Zuccardi", campos.bodega)
        assertEquals(false, CampoSugerible.NOMBRE in campos.sugeridos)
    }

    @Test
    fun loQueSeEditaMientrasLaNubeTodaviaNoRespondioSeRespeta() = runTest {
        cumplirLasCondiciones()
        val puerta = CompletableDeferred<Unit>()
        lectorNube.puerta = puerta
        val viewModel = crearViewModel()
        viewModel.alElegirFoto("origen") // ML Kit responde enseguida; la nube queda esperando

        assertEquals("RUTINI", viewModel.campos().nombre)
        viewModel.alCambiarBodega("Mi bodega")
        puerta.complete(Unit)

        val campos = viewModel.campos()
        assertEquals("Mi bodega", campos.bodega)
        assertEquals("José Zuccardi", campos.nombre)
        assertEquals("2017", campos.anio)
    }

    @Test
    fun siElTelefonoNoLeyoNadaLaNubeCompletaLosCampos() = runTest {
        cumplirLasCondiciones()
        lectorLocal.texto = ETIQUETA_SIN_TEXTO
        val viewModel = crearViewModel()

        viewModel.alElegirFoto("origen")

        val campos = viewModel.campos()
        assertEquals("José Zuccardi", campos.nombre)
        assertEquals(LecturaEtiqueta.Inactiva, campos.lectura)
    }

    @Test
    fun siSeCambiaLaFotoLaRespuestaVieja_seDescarta() = runTest {
        cumplirLasCondiciones()
        val puerta = CompletableDeferred<Unit>()
        lectorNube.puerta = puerta
        val viewModel = crearViewModel()
        viewModel.alElegirFoto("origen")

        viewModel.alQuitarFoto()
        puerta.complete(Unit)

        assertEquals("", viewModel.campos().anio)
    }
}
