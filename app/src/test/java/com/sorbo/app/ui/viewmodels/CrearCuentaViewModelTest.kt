// Pruebas · ViewModel de Crear cuenta: cada validación en su orden, errores del servidor y aviso de los vinos que se respaldan.
package com.sorbo.app.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.sorbo.app.data.memoria.SesionRepositoryEnMemoria
import com.sorbo.app.domain.model.Usuario
import com.sorbo.app.domain.repository.MotivoCuenta
import com.sorbo.app.domain.usecase.ResultadoInicioSesion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CrearCuentaViewModelTest {

    private val sesion = SesionRepositoryEnMemoria()
    private val entraron = mutableListOf<Usuario>()

    @Before
    fun antes() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun despues() = Dispatchers.resetMain()

    private fun crear(
        vinos: suspend () -> Int = { 0 },
        alEntrar: suspend (Usuario) -> ResultadoInicioSesion? = { entraron += it; null },
    ) = CrearCuentaViewModel(sesion, SavedStateHandle(), vinos, alEntrar)

    private fun CrearCuentaViewModel.editando() = estado.value as EstadoCrearCuenta.Editando

    private fun CrearCuentaViewModel.completarTodo(
        email: String = "ana@correo.com",
        clave: String = "Clave-segura-1",
        repetir: String = clave,
        terminos: Boolean = true,
    ) {
        alCambiarEmail(email)
        alCambiarClave(clave)
        alCambiarRepetirClave(repetir)
        alCambiarTerminos(terminos)
    }

    @Test
    fun conTodoBienCreaLaCuentaYDiceCuantosVinosSeRespaldan() = runTest {
        val viewModel = crear(vinos = { 5 })
        viewModel.completarTodo()

        viewModel.alCrearCuenta()

        assertEquals(EstadoCrearCuenta.Listo(5), viewModel.estado.value)
        assertEquals(listOf("ana@correo.com"), entraron.map { it.email })
    }

    @Test
    fun unEmailInvalidoSeAvisaPrimero() = runTest {
        val viewModel = crear()
        viewModel.completarTodo(email = "sin-arroba", clave = "corta", terminos = false)

        viewModel.alCrearCuenta()

        assertEquals(ErrorDeCuenta.EmailInvalido, viewModel.editando().error)
    }

    @Test
    fun laContrasenaTieneQueTenerElMinimo() = runTest {
        val viewModel = crear()
        viewModel.completarTodo(clave = "1234567", terminos = false)

        viewModel.alCrearCuenta()

        assertEquals(ErrorDeCuenta.ClaveCorta, viewModel.editando().error)
    }

    @Test
    fun lasDosContrasenasTienenQueCoincidir() = runTest {
        val viewModel = crear()
        viewModel.completarTodo(repetir = "Clave-segura-2")

        viewModel.alCrearCuenta()

        assertEquals(ErrorDeCuenta.ClavesDistintas, viewModel.editando().error)
    }

    @Test
    fun hayQueAceptarLosTerminos() = runTest {
        val viewModel = crear()
        viewModel.completarTodo(terminos = false)

        viewModel.alCrearCuenta()

        assertEquals(ErrorDeCuenta.TerminosSinAceptar, viewModel.editando().error)
        assertTrue(entraron.isEmpty())
    }

    @Test
    fun siElEmailYaTieneCuentaSeAvisa() = runTest {
        sesion.crearCuenta("ana@correo.com", "Clave-segura-1")
        sesion.cerrarSesion()
        val viewModel = crear()
        viewModel.completarTodo()

        viewModel.alCrearCuenta()

        assertEquals(ErrorDeCuenta.Servidor(MotivoCuenta.EMAIL_YA_REGISTRADO), viewModel.editando().error)
        assertFalse(viewModel.editando().enviando)
    }

    @Test
    fun sinConexionSeAvisaYQuedaTodoLoEscrito() = runTest {
        sesion.hayConexion = false
        val viewModel = crear()
        viewModel.completarTodo()

        viewModel.alCrearCuenta()

        val editando = viewModel.editando()
        assertEquals(ErrorDeCuenta.Servidor(MotivoCuenta.SIN_CONEXION), editando.error)
        assertEquals("Clave-segura-1", editando.clave)
        assertEquals("Clave-segura-1", editando.repetirClave)
        assertTrue(editando.aceptoTerminos)
    }

    @Test
    fun elRequisitoDeLaContrasenaSeTildaMientrasSeEscribe() = runTest {
        val viewModel = crear()
        viewModel.alCambiarClave("1234567")
        assertFalse(viewModel.editando().claveCumpleElMinimo)

        viewModel.alCambiarClave("12345678")

        assertTrue(viewModel.editando().claveCumpleElMinimo)
    }

    @Test
    fun siNoSePuedeContarLosVinosLaCuentaIgualSeCrea() = runTest {
        val viewModel = crear(vinos = { throw IllegalStateException("sin base") })
        viewModel.completarTodo()

        viewModel.alCrearCuenta()

        assertEquals(EstadoCrearCuenta.Listo(0), viewModel.estado.value)
    }

    @Test
    fun siFallaElRespaldoInicialLaCuentaIgualSeCrea() = runTest {
        val viewModel = crear(alEntrar = { throw IllegalStateException("sin red") })
        viewModel.completarTodo()

        viewModel.alCrearCuenta()

        assertTrue(viewModel.estado.value is EstadoCrearCuenta.Listo)
    }

    @Test
    fun laCantidadQueSeRespaldaLaDiceElCasoDeUsoDeIniciarSesion() = runTest {
        val viewModel = crear(
            vinos = { 99 },
            alEntrar = { ResultadoInicioSesion(vinosParaRespaldar = 3, hayRespaldoParaRestaurar = false) },
        )
        viewModel.completarTodo()

        viewModel.alCrearCuenta()

        assertEquals(EstadoCrearCuenta.Listo(3), viewModel.estado.value)
    }
}
