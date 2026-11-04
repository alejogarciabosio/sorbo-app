// Capa de presentación · navegación: rutas y mapa de pantallas (Navigation Compose, una sola Activity).
package com.sorbo.app.ui.navegacion

import androidx.compose.foundation.layout.statusBarsPadding
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
import com.sorbo.app.AppContainer
import com.sorbo.app.R
import com.sorbo.app.ui.viewmodels.BuscarViewModel
import com.sorbo.app.ui.viewmodels.DetalleViewModel
import com.sorbo.app.ui.viewmodels.DiarioViewModel
import com.sorbo.app.ui.viewmodels.EditarViewModel
import com.sorbo.app.ui.viewmodels.RegistroViewModel
import com.sorbo.app.ui.screens.PantallaBuscar
import com.sorbo.app.ui.screens.PantallaDetalle
import com.sorbo.app.ui.screens.PantallaDiario
import com.sorbo.app.ui.screens.PantallaEditar
import com.sorbo.app.ui.screens.PantallaRegistro
import com.sorbo.app.ui.screens.GaleriaComponentes
import com.sorbo.app.ui.screens.AccesoDePrueba
import com.sorbo.app.ui.screens.PantallaEnConstruccion

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
            val viewModel = viewModel { DiarioViewModel(contenedor.vinoRepository) }
            PantallaDiario(
                viewModel = viewModel,
                onBuscar = { ir(Rutas.BUSCAR) },
                onAbrirVino = { ir(Rutas.detalle(it)) },
                onRegistrar = { ir(Rutas.registrar()) },
            )
        }
        composable(Rutas.GUSTOS) {
            PantallaEnConstruccion(titulo = R.string.destino_gustos, accesos = emptyList(), onIr = ir)
        }
        composable(Rutas.AJUSTES) {
            PantallaEnConstruccion(
                titulo = R.string.destino_ajustes,
                accesos = listOf(
                    AccesoDePrueba(R.string.ir_a_cuenta, Rutas.CUENTA),
                    AccesoDePrueba(R.string.ir_a_iniciar_sesion, Rutas.INICIAR_SESION),
                    AccesoDePrueba(R.string.ir_a_privacidad, Rutas.PRIVACIDAD),
                    AccesoDePrueba(R.string.ir_a_galeria, Rutas.GALERIA),
                ),
                onIr = ir,
            )
        }

        composable(Rutas.BUSCAR) {
            val viewModel = viewModel { BuscarViewModel(contenedor.buscarEnDiario) }
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
                    reloj = contenedor.reloj,
                    savedStateHandle = createSavedStateHandle(),
                    nombreInicial = nombreBuscado,
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
        composable(Rutas.VOLVER_A_PROBAR, arguments = argumentoVino) {
            PantallaEnConstruccion(titulo = R.string.titulo_volver_a_probar, accesos = emptyList(), onIr = ir, onAtras = atras)
        }

        composable(Rutas.INICIAR_SESION) {
            PantallaEnConstruccion(
                titulo = R.string.titulo_iniciar_sesion,
                accesos = listOf(
                    AccesoDePrueba(R.string.ir_a_crear_cuenta, Rutas.CREAR_CUENTA),
                    AccesoDePrueba(R.string.ir_a_recuperar_contrasena, Rutas.RECUPERAR_CONTRASENA),
                ),
                onIr = ir,
                onAtras = atras,
            )
        }
        composable(Rutas.CREAR_CUENTA) {
            PantallaEnConstruccion(titulo = R.string.titulo_crear_cuenta, accesos = emptyList(), onIr = ir, onAtras = atras)
        }
        composable(Rutas.RECUPERAR_CONTRASENA) {
            PantallaEnConstruccion(titulo = R.string.titulo_recuperar_contrasena, accesos = emptyList(), onIr = ir, onAtras = atras)
        }
        composable(Rutas.CUENTA) {
            PantallaEnConstruccion(
                titulo = R.string.titulo_cuenta,
                accesos = listOf(AccesoDePrueba(R.string.ir_a_cambiar_contrasena, Rutas.CAMBIAR_CONTRASENA)),
                onIr = ir,
                onAtras = atras,
            )
        }
        composable(Rutas.CAMBIAR_CONTRASENA) {
            PantallaEnConstruccion(titulo = R.string.titulo_cambiar_contrasena, accesos = emptyList(), onIr = ir, onAtras = atras)
        }
        composable(Rutas.PRIVACIDAD) {
            PantallaEnConstruccion(titulo = R.string.titulo_privacidad, accesos = emptyList(), onIr = ir, onAtras = atras)
        }

        composable(Rutas.GALERIA) {
            GaleriaComponentes(modifier = Modifier.statusBarsPadding())
        }
    }
}
