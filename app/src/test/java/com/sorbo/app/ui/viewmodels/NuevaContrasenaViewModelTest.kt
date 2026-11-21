// Pruebas · ViewModel de Nueva contraseña: elegir y confirmar la contraseña, con y sin sesión abierta.
package com.sorbo.app.ui.viewmodels

import com.sorbo.app.data.memoria.SesionRepositoryEnMemoria
import com.sorbo.app.domain.repository.MotivoCuenta
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
class NuevaContrasenaViewModelTest {

    private val sesion = SesionRepositoryEnMemoria()

    @Before
    fun antes() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun despues() = Dispatchers.resetMain()

    private fun NuevaContrasenaViewModel.editando() = estado.value as EstadoNuevaContrasena.Editando

    @Test
    fun conSesionYContrasenasIgualesLaCambia() = runTest {
        sesion.crearCuenta("ana@correo.com", "Clave-segura-1")
        val viewModel = NuevaContrasenaViewModel(sesion)
        viewModel.alCambiarClave("Clave-nueva-22")
        viewModel.alCambiarRepetirClave("Clave-nueva-22")

        viewModel.alGuardar()

        assertEquals(EstadoNuevaContrasena.Listo, viewModel.estado.value)
        sesion.cerrarSesion()
        assertTrue(sesion.iniciarSesion("ana@correo.com", "Clave-nueva-22").isSuccess)
    }

    @Test
    fun unaContrasenaCortaNoSeEnvia() = runTest {
        val viewModel = NuevaContrasenaViewModel(sesion)
        viewModel.alCambiarClave("corta")
        viewModel.alCambiarRepetirClave("corta")

        viewModel.alGuardar()

        assertEquals(ErrorDeCuenta.ClaveCorta, viewModel.editando().error)
    }

    @Test
    fun siNoCoincidenSeAvisa() = runTest {
        val viewModel = NuevaContrasenaViewModel(sesion)
        viewModel.alCambiarClave("Clave-nueva-22")
        viewModel.alCambiarRepetirClave("Clave-nueva-23")

        viewModel.alGuardar()

        assertEquals(ErrorDeCuenta.ClavesDistintas, viewModel.editando().error)
    }

    @Test
    fun sinSesionElEnlaceNoSirve() = runTest {
        val viewModel = NuevaContrasenaViewModel(sesion)
        viewModel.alCambiarClave("Clave-nueva-22")
        viewModel.alCambiarRepetirClave("Clave-nueva-22")

        viewModel.alGuardar()

        assertEquals(ErrorDeCuenta.Servidor(MotivoCuenta.SIN_SESION), viewModel.editando().error)
        assertFalse(viewModel.editando().enviando)
    }

    @Test
    fun sinConexionSeAvisaYSeConservaLoEscrito() = runTest {
        sesion.crearCuenta("ana@correo.com", "Clave-segura-1")
        sesion.hayConexion = false
        val viewModel = NuevaContrasenaViewModel(sesion)
        viewModel.alCambiarClave("Clave-nueva-22")
        viewModel.alCambiarRepetirClave("Clave-nueva-22")

        viewModel.alGuardar()

        assertEquals(ErrorDeCuenta.Servidor(MotivoCuenta.SIN_CONEXION), viewModel.editando().error)
        assertEquals("Clave-nueva-22", viewModel.editando().clave)
    }
}
