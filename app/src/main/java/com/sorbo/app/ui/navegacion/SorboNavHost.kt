// Capa de presentación · navegación: rutas y mapa de pantallas (Navigation Compose, una sola Activity).
package com.sorbo.app.ui.navegacion

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.first
import com.sorbo.app.AppContainer
import com.sorbo.app.R
import com.sorbo.app.ui.viewmodels.BuscarViewModel
import com.sorbo.app.ui.viewmodels.DetalleViewModel
import com.sorbo.app.ui.viewmodels.DiarioViewModel
import com.sorbo.app.ui.viewmodels.EditarViewModel
import com.sorbo.app.ui.viewmodels.GustosViewModel
import com.sorbo.app.ui.screens.PantallaGustos
import com.sorbo.app.ui.viewmodels.RegistroViewModel
import com.sorbo.app.ui.viewmodels.VolverAProbarViewModel
import com.sorbo.app.ui.screens.PantallaAjustes
import com.sorbo.app.ui.screens.PantallaMiCuenta
import com.sorbo.app.ui.screens.PantallaPrivacidad
import com.sorbo.app.ui.viewmodels.AjustesViewModel
import com.sorbo.app.ui.screens.PantallaBuscar
import com.sorbo.app.ui.screens.PantallaDetalle
import com.sorbo.app.ui.screens.PantallaDiario
import com.sorbo.app.ui.screens.PantallaEditar
import com.sorbo.app.ui.screens.PantallaRegistro
import com.sorbo.app.ui.screens.PantallaVolverAProbar
import com.sorbo.app.ui.screens.PantallaCrearCuenta
import com.sorbo.app.ui.screens.PantallaCuentaLista
import com.sorbo.app.ui.screens.PantallaIniciarSesion
import com.sorbo.app.ui.screens.PantallaNuevaContrasena
import com.sorbo.app.ui.screens.PantallaRecuperarContrasena
import com.sorbo.app.ui.viewmodels.CrearCuentaViewModel
import com.sorbo.app.ui.viewmodels.IniciarSesionViewModel
import com.sorbo.app.ui.viewmodels.NuevaContrasenaViewModel
import com.sorbo.app.ui.viewmodels.ResumenIaViewModel
import com.sorbo.app.ui.viewmodels.RecuperarContrasenaViewModel

private val argumentoVino = listOf(navArgument(Rutas.ARG_VINO_ID) { type = NavType.StringType })

private val argumentoNombre = listOf(
    navArgument(Rutas.ARG_NOMBRE) {
        type = NavType.StringType
        nullable = true
        defaultValue = null
    },
)

/** Mapa de pantallas de la app. Cada pantalla provisoria se reemplaza por la real más adelante. */
@Composable
fun SorboNavHost(
    navController: NavHostController,
    contenedor: AppContainer,
    avisos: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()
    val ir: (String) -> Unit = { navController.navigate(it) }
    val atras: () -> Unit = { navController.popBackStack() }

    NavHost(navController = navController, startDestination = Rutas.DIARIO, modifier = modifier) {
        composable(Rutas.DIARIO) {
            val viewModel = viewModel { DiarioViewModel(contenedor.vinoRepository, contenedor.sesionRepository, contenedor.conectividadObserver) }
            PantallaDiario(
                viewModel = viewModel,
                onBuscar = { ir(Rutas.BUSCAR) },
                onAbrirVino = { ir(Rutas.detalle(it)) },
                onRegistrar = { ir(Rutas.registrar()) },
            )
        }
        composable(Rutas.GUSTOS) {
            val viewModel = viewModel { GustosViewModel(contenedor.vinoRepository, contenedor.calcularEstadisticasGustos) }
            val resumenIa = viewModel {
                ResumenIaViewModel(
                    repositorio = contenedor.vinoRepository,
                    calcular = contenedor.calcularEstadisticasGustos,
                    analisis = contenedor.analisisGustosRepository,
                    preferencias = contenedor.preferenciasRepository,
                    sesion = contenedor.sesionRepository,
                    conectividad = contenedor.conectividadObserver,
                )
            }
            PantallaGustos(
                viewModel = viewModel,
                resumenIa = resumenIa,
                hoy = LocalDate.now(contenedor.reloj),
                onRegistrar = { ir(Rutas.registrar()) },
                onIniciarSesion = { ir(Rutas.INICIAR_SESION) },
            )
        }
        composable(Rutas.AJUSTES) {
            val viewModel = viewModel { AjustesViewModel(contenedor.sesionRepository, contenedor.preferenciasRepository, contenedor.vinoRepository) }
            PantallaAjustes(
                viewModel = viewModel,
                onMiCuenta = { ir(Rutas.CUENTA) },
                onIniciarSesion = { ir(Rutas.INICIAR_SESION) },
                onCrearCuenta = { ir(Rutas.CREAR_CUENTA) },
                onPrivacidad = { ir(Rutas.PRIVACIDAD) },
            )
        }

        composable(Rutas.BUSCAR) {
            val viewModel = viewModel { BuscarViewModel(contenedor.buscarEnDiario, contenedor.preferenciasRepository, contenedor.sesionRepository) }
            PantallaBuscar(
                viewModel = viewModel,
                onAtras = atras,
                onAbrirVino = { ir(Rutas.detalle(it)) },
                onRegistrar = { nombre -> ir(Rutas.registrar(nombre)) },
            )
        }
        composable(Rutas.REGISTRAR, arguments = argumentoNombre) { entrada ->
            val nombreBuscado = entrada.arguments?.getString(Rutas.ARG_NOMBRE)
            val viewModel = viewModel {
                RegistroViewModel(
                    buscarVinoParecido = contenedor.buscarVinoParecido,
                    registrarDegustacion = contenedor.registrarDegustacion,
                    obtenerDetalleVino = contenedor.obtenerDetalleVino,
                    almacenFotos = contenedor.almacenFotos,
                    sugerirDatosDesdeFoto = contenedor.sugerirDatosDesdeFoto,
                    reloj = contenedor.reloj,
                    savedStateHandle = createSavedStateHandle(),
                    nombreInicial = nombreBuscado,
                    leerEtiquetaEnLaNube = contenedor.leerEtiquetaEnLaNube,
                )
            }
            PantallaRegistro(
                viewModel = viewModel,
                onCerrar = atras,
                onGuardado = { guardado ->
                    navController.navigate(Rutas.detalle(guardado.vinoId)) { popUpTo(Rutas.REGISTRAR) { inclusive = true } }
                    if (guardado.vinoNuevo) {
                        alcance.launch { avisos.showSnackbar(contexto.getString(R.string.detalle_vino_guardado)) }
                    } else {
                        alcance.launch {
                            val respuesta = avisos.showSnackbar(
                                message = contexto.getString(R.string.registro_sumamos_esta_vez, guardado.nombreDelVino),
                                actionLabel = contexto.getString(R.string.accion_deshacer),
                                duration = SnackbarDuration.Long,
                            )
                            if (respuesta == SnackbarResult.ActionPerformed) contenedor.deshacerDegustacion(guardado.degustacionId)
                        }
                    }
                },
                onNuevaDireccionParaCamara = contenedor.archivoParaCamara::nuevaDireccion,
            )
        }
        composable(Rutas.DETALLE, arguments = argumentoVino) { entrada ->
            val vinoId = entrada.arguments?.getString(Rutas.ARG_VINO_ID).orEmpty()
            val viewModel = viewModel(key = "detalle/$vinoId") {
                DetalleViewModel(vinoId, contenedor.obtenerDetalleVino, contenedor.eliminarVino)
            }
            PantallaDetalle(
                viewModel = viewModel,
                hoy = LocalDate.now(contenedor.reloj),
                onVolver = atras,
                onEditar = { ir(Rutas.editar(vinoId)) },
                onVolviAProbar = { ir(Rutas.volverAProbar(vinoId)) },
                onAbrirVino = { ir(Rutas.detalle(it)) },
                onEliminado = { eliminado ->
                    navController.popBackStack()
                    alcance.launch {
                        val respuesta = avisos.showSnackbar(
                            message = contexto.getString(R.string.detalle_vino_eliminado),
                            actionLabel = contexto.getString(R.string.accion_deshacer),
                            duration = SnackbarDuration.Long,
                        )
                        if (respuesta == SnackbarResult.ActionPerformed) contenedor.eliminarVino.deshacer(eliminado.vinoId)
                    }
                },
            )
        }
        composable(Rutas.EDITAR, arguments = argumentoVino) { entrada ->
            val vinoId = entrada.arguments?.getString(Rutas.ARG_VINO_ID).orEmpty()
            val viewModel = viewModel(key = "editar/$vinoId") {
                EditarViewModel(
                    vinoId = vinoId,
                    obtenerDetalleVino = contenedor.obtenerDetalleVino,
                    editarVino = contenedor.editarVino,
                    almacenFotos = contenedor.almacenFotos,
                    savedStateHandle = createSavedStateHandle(),
                )
            }
            PantallaEditar(
                viewModel = viewModel,
                onCerrar = atras,
                onGuardado = {
                    navController.popBackStack()
                    alcance.launch { avisos.showSnackbar(contexto.getString(R.string.editar_cambios_guardados)) }
                },
                onNuevaDireccionParaCamara = contenedor.archivoParaCamara::nuevaDireccion,
            )
        }
        composable(Rutas.VOLVER_A_PROBAR, arguments = argumentoVino) { entrada ->
            val vinoId = entrada.arguments?.getString(Rutas.ARG_VINO_ID).orEmpty()
            val viewModel = viewModel(key = "volver_a_probar/$vinoId") {
                VolverAProbarViewModel(
                    vinoId = vinoId,
                    obtenerDetalleVino = contenedor.obtenerDetalleVino,
                    registrarDegustacion = contenedor.registrarDegustacion,
                    reloj = contenedor.reloj,
                    savedStateHandle = createSavedStateHandle(),
                )
            }
            PantallaVolverAProbar(
                viewModel = viewModel,
                onCerrar = atras,
                onGuardado = { guardado ->
                    navController.popBackStack()
                    alcance.launch {
                        val respuesta = avisos.showSnackbar(
                            message = contexto.getString(R.string.registro_sumamos_esta_vez, guardado.nombreDelVino),
                            actionLabel = contexto.getString(R.string.accion_deshacer),
                            duration = SnackbarDuration.Long,
                        )
                        if (respuesta == SnackbarResult.ActionPerformed) contenedor.deshacerDegustacion(guardado.degustacionId)
                    }
                },
            )
        }

        composable(Rutas.INICIAR_SESION) {
            val viewModel = viewModel { IniciarSesionViewModel(contenedor.sesionRepository, createSavedStateHandle()) }
            PantallaIniciarSesion(
                viewModel = viewModel,
                onCerrar = atras,
                onCrearCuenta = { navController.navigate(Rutas.CREAR_CUENTA) { popUpTo(Rutas.INICIAR_SESION) { inclusive = true } } },
                onOlvideLaClave = { ir(Rutas.RECUPERAR_CONTRASENA) },
                onListo = atras,
            )
        }
        composable(Rutas.CREAR_CUENTA) {
            val viewModel = viewModel {
                CrearCuentaViewModel(
                    sesion = contenedor.sesionRepository,
                    savedStateHandle = createSavedStateHandle(),
                    contarVinosDelTelefono = { contenedor.vinoRepository.observarDiario().first().size },
                )
            }
            PantallaCrearCuenta(
                viewModel = viewModel,
                onAtras = atras,
                onYaTengoCuenta = { navController.navigate(Rutas.INICIAR_SESION) { popUpTo(Rutas.CREAR_CUENTA) { inclusive = true } } },
                onVerPrivacidad = { ir(Rutas.PRIVACIDAD) },
                onListo = { vinos ->
                    navController.navigate(Rutas.cuentaLista(vinos)) { popUpTo(Rutas.CREAR_CUENTA) { inclusive = true } }
                },
            )
        }
        composable(Rutas.CUENTA_LISTA, arguments = listOf(navArgument(Rutas.ARG_VINOS) { type = NavType.IntType })) { entrada ->
            PantallaCuentaLista(
                vinosDelTelefono = entrada.arguments?.getInt(Rutas.ARG_VINOS) ?: 0,
                onContinuar = { navController.popBackStack(Rutas.DIARIO, inclusive = false) },
            )
        }
        composable(Rutas.RECUPERAR_CONTRASENA) {
            val viewModel = viewModel { RecuperarContrasenaViewModel(contenedor.sesionRepository, createSavedStateHandle()) }
            PantallaRecuperarContrasena(viewModel = viewModel, onAtras = atras)
        }
        composable(Rutas.CUENTA) {
            val viewModel = viewModel { AjustesViewModel(contenedor.sesionRepository, contenedor.preferenciasRepository, contenedor.vinoRepository) }
            PantallaMiCuenta(viewModel = viewModel, onAtras = atras, onCambiarClave = { ir(Rutas.CAMBIAR_CONTRASENA) })
        }
        composable(Rutas.CAMBIAR_CONTRASENA) {
            val viewModel = viewModel { NuevaContrasenaViewModel(contenedor.sesionRepository) }
            PantallaNuevaContrasena(viewModel = viewModel, onAtras = atras, onContinuar = atras)
        }
        composable(Rutas.PRIVACIDAD) {
            PantallaPrivacidad(onAtras = atras)
        }
    }
}
