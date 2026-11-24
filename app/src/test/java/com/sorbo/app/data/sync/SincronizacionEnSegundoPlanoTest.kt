package com.sorbo.app.data.sync

import com.sorbo.app.data.memoria.ProgramadorDeSincronizacionEnMemoria
import com.sorbo.app.data.memoria.SesionRepositoryEnMemoria
import com.sorbo.app.data.memoria.VinoRepositoryEnMemoria
import com.sorbo.app.domain.degustacionDe
import com.sorbo.app.domain.model.CambiosPendientes
import com.sorbo.app.domain.usecase.ResultadoSincronizacion
import com.sorbo.app.domain.vinoDe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SincronizacionEnSegundoPlanoTest {

    // --- Qué le contesta el SyncWorker a WorkManager ---

    @Test
    fun terminaSinCuentaSinPendientesOConTodoSubido() {
        assertEquals(AccionTrabajo.TERMINAR, accionPara(ResultadoSincronizacion.SinSesion))
        assertEquals(AccionTrabajo.TERMINAR, accionPara(ResultadoSincronizacion.NadaPendiente))
        assertEquals(AccionTrabajo.TERMINAR, accionPara(ResultadoSincronizacion.Sincronizado(1, 2)))
    }

    @Test
    fun reintentaSiSeArreglaEsperandoYFallaSiNo() {
        assertEquals(AccionTrabajo.REINTENTAR, accionPara(ResultadoSincronizacion.Fallo(reintentable = true)))
        assertEquals(AccionTrabajo.FALLAR, accionPara(ResultadoSincronizacion.Fallo(reintentable = false)))
    }

    // --- Cuándo se pide sincronizar ---

    private val vinos = VinoRepositoryEnMemoria()
    private val sesion = SesionRepositoryEnMemoria()
    private val programador = ProgramadorDeSincronizacionEnMemoria()
    private val disparador = DisparadorDeSincronizacion(vinos, sesion, programador)

    @Test
    fun sinCuentaNoSeProgramaNada() = runTest(UnconfinedTestDispatcher()) {
        val trabajo = disparador.iniciar(backgroundScope)
        val vino = vinoDe("Rutini Malbec")
        vinos.guardarVinoNuevo(vino, degustacionDe(vino.id))

        assertEquals(0, programador.veces)
        trabajo.cancel()
    }

    @Test
    fun conCuentaCadaCambioPideSincronizar() = runTest(UnconfinedTestDispatcher()) {
        sesion.crearCuenta("ana@mail.com", "secreta123")
        disparador.iniciar(backgroundScope)

        val vino = vinoDe("Rutini Malbec")
        vinos.guardarVinoNuevo(vino, degustacionDe(vino.id))
        val despuesDeGuardar = programador.veces
        vinos.agregarDegustacion(degustacionDe(vino.id))

        assertEquals(1, despuesDeGuardar)
        assertEquals(2, programador.veces)
    }

    @Test
    fun alAbrirLaAppConPendientesYCuentaSePideSincronizar() = runTest(UnconfinedTestDispatcher()) {
        val vino = vinoDe("Rutini Malbec")
        vinos.guardarVinoNuevo(vino, degustacionDe(vino.id))
        sesion.crearCuenta("ana@mail.com", "secreta123")

        disparador.iniciar(backgroundScope)

        assertEquals(1, programador.veces)
    }

    @Test
    fun conTodoRespaldadoNoSePideNada() = runTest(UnconfinedTestDispatcher()) {
        val vino = vinoDe("Rutini Malbec")
        vinos.guardarVinoNuevo(vino, degustacionDe(vino.id))
        vinos.marcarSincronizado(vinos.listarPendientes())
        sesion.crearCuenta("ana@mail.com", "secreta123")

        disparador.iniciar(backgroundScope)
        vinos.marcarSincronizado(CambiosPendientes(emptyList(), emptyList()))

        assertEquals(0, programador.veces)
    }
}
