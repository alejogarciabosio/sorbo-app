package com.sorbo.app.ui.navegacion

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sorbo.app.R
import com.sorbo.app.ui.galeria.GaleriaComponentes
import com.sorbo.app.ui.pantallas.AccesoDePrueba
import com.sorbo.app.ui.pantallas.PantallaEnConstruccion

private val argumentoVino = listOf(navArgument(Rutas.ARG_VINO_ID) { type = NavType.StringType })

/** Mapa de pantallas de la app. Cada pantalla provisoria se reemplaza por la real más adelante. */
@Composable
fun SorboNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val ir: (String) -> Unit = { navController.navigate(it) }
    val atras: () -> Unit = { navController.popBackStack() }

    NavHost(navController = navController, startDestination = Rutas.DIARIO, modifier = modifier) {
        composable(Rutas.DIARIO) {
            PantallaEnConstruccion(
                titulo = R.string.destino_diario,
                accesos = listOf(
                    AccesoDePrueba(R.string.ir_a_buscar, Rutas.BUSCAR),
                    AccesoDePrueba(R.string.ir_a_detalle, Rutas.detalle("ejemplo")),
                ),
                onIr = ir,
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
            PantallaEnConstruccion(titulo = R.string.titulo_buscar, accesos = listOf(AccesoDePrueba(R.string.accion_registrar, Rutas.REGISTRAR)), onIr = ir, onAtras = atras)
        }
        composable(Rutas.REGISTRAR) {
            PantallaEnConstruccion(titulo = R.string.titulo_registrar, accesos = emptyList(), onIr = ir, onAtras = atras)
        }
        composable(Rutas.DETALLE, arguments = argumentoVino) { entrada ->
            val vinoId = entrada.arguments?.getString(Rutas.ARG_VINO_ID).orEmpty()
            PantallaEnConstruccion(
                titulo = R.string.titulo_detalle,
                accesos = listOf(
                    AccesoDePrueba(R.string.ir_a_editar, Rutas.editar(vinoId)),
                    AccesoDePrueba(R.string.ir_a_volver_a_probar, Rutas.volverAProbar(vinoId)),
                ),
                onIr = ir,
                onAtras = atras,
            )
        }
        composable(Rutas.EDITAR, arguments = argumentoVino) {
            PantallaEnConstruccion(titulo = R.string.titulo_editar, accesos = emptyList(), onIr = ir, onAtras = atras)
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
