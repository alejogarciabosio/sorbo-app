// Capa de presentación · pantalla: dibuja el estado que expone el ViewModel y le avisa de los eventos.
package com.sorbo.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sorbo.app.R
import com.sorbo.app.ui.components.DialogosDeAjustes
import com.sorbo.app.ui.components.FinalItemAjustes
import com.sorbo.app.ui.components.ItemAjustes
import com.sorbo.app.ui.viewmodels.AjustesViewModel
import com.sorbo.app.ui.viewmodels.EstadoAjustes

/** G3: Mi cuenta. Cambiar la contraseña, cerrar sesión o eliminar la cuenta (G6, con confirmación). Si la sesión termina, vuelve a Ajustes. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMiCuenta(viewModel: AjustesViewModel, onAtras: () -> Unit, onCambiarClave: () -> Unit) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val dialogo by viewModel.dialogo.collectAsStateWithLifecycle()
    LaunchedEffect(estado) { if (estado == EstadoAjustes.SinSesion) onAtras() }

    DialogosDeAjustes(
        dialogo = dialogo,
        alConfirmarCerrarSesion = viewModel::alConfirmarCerrarSesion,
        alConfirmarEliminarCuenta = viewModel::alConfirmarEliminarCuenta,
        alCerrar = viewModel::alCerrarDialogo,
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_cuenta)) },
                navigationIcon = {
                    IconButton(onClick = onAtras) {
                        Icon(painterResource(R.drawable.ic_atras), contentDescription = stringResource(R.string.accion_volver))
                    }
                },
            )
        },
    ) { padding ->
        val conSesion = estado as? EstadoAjustes.ConSesion ?: return@Scaffold
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            ItemAjustes(
                titulo = conSesion.email,
                icono = R.drawable.ic_persona,
                subtitulo = stringResource(R.string.ajustes_cuenta_conectada),
            )
            ItemAjustes(
                titulo = stringResource(R.string.titulo_cambiar_contrasena),
                icono = R.drawable.ic_candado,
                final = FinalItemAjustes.Flecha,
                onClick = onCambiarClave,
            )
            ItemAjustes(
                titulo = stringResource(R.string.ajustes_cerrar_sesion),
                icono = R.drawable.ic_cerrar_sesion,
                onClick = viewModel::alPedirCerrarSesion,
            )
            ListItem(
                modifier = Modifier.clickable(onClick = viewModel::alPedirEliminarCuenta),
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                headlineContent = {
                    Text(
                        stringResource(R.string.ajustes_eliminar_cuenta),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                },
                leadingContent = {
                    Icon(painterResource(R.drawable.ic_eliminar), contentDescription = null, tint = MaterialTheme.colorScheme.error)
                },
            )
        }
    }
}
