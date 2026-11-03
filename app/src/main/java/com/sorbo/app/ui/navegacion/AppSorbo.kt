// Capa de presentación · navegación: rutas y mapa de pantallas (Navigation Compose, una sola Activity).
package com.sorbo.app.ui.navegacion

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sorbo.app.AppContainer
import com.sorbo.app.R

/** Raíz de la app: barra inferior, botón "Registrar" en el Diario y el mapa de pantallas. */
@Composable
fun AppSorbo(contenedor: AppContainer) {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route
    // Los avisos viven acá, y no en cada pantalla, para que sigan visibles cuando la pantalla que los generó ya se cerró.
    val avisos = remember { SnackbarHostState() }

    Scaffold(
        // Cada pantalla se ocupa de su propia barra superior, así que el Scaffold no suma insets.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        // Con la barra inferior, el Scaffold ya apoya el aviso arriba de ella; sin barra hay que subirlo sobre la del sistema.
        snackbarHost = {
            SnackbarHost(
                hostState = avisos,
                modifier = if (Rutas.mostrarBarraInferior(rutaActual)) Modifier else Modifier.navigationBarsPadding(),
            )
        },
        bottomBar = {
            if (Rutas.mostrarBarraInferior(rutaActual)) {
                NavigationBar {
                    DestinoPrincipal.entries.forEach { destino ->
                        val seleccionado = entradaActual?.destination?.hierarchy?.any { it.route == destino.ruta } == true
                        NavigationBarItem(
                            selected = seleccionado,
                            onClick = {
                                navController.navigate(destino.ruta) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(painterResource(destino.icono), contentDescription = null) },
                            label = { Text(stringResource(destino.etiqueta)) },
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (rutaActual == Rutas.DIARIO) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(Rutas.registrar()) },
                    icon = { Icon(painterResource(R.drawable.ic_camara), contentDescription = null) },
                    text = { Text(stringResource(R.string.accion_registrar)) },
                )
            }
        },
    ) { padding ->
        SorboNavHost(navController = navController, contenedor = contenedor, avisos = avisos, modifier = Modifier.padding(padding))
    }
}
