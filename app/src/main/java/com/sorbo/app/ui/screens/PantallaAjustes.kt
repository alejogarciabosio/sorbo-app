// Capa de presentación · pantalla: dibuja el estado que expone el ViewModel y le avisa de los eventos.
package com.sorbo.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sorbo.app.R
import com.sorbo.app.ui.components.DialogosDeAjustes
import com.sorbo.app.ui.components.FinalItemAjustes
import com.sorbo.app.ui.components.ItemAjustes
import com.sorbo.app.ui.viewmodels.AjustesViewModel
import com.sorbo.app.ui.viewmodels.EstadoAjustes

/** G1 (con cuenta) y G2 (sin cuenta): la cuenta y el respaldo, el control de la IA y la privacidad. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAjustes(
    viewModel: AjustesViewModel,
    onMiCuenta: () -> Unit,
    onIniciarSesion: () -> Unit,
    onCrearCuenta: () -> Unit,
    onPrivacidad: () -> Unit,
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val dialogo by viewModel.dialogo.collectAsStateWithLifecycle()

    DialogosDeAjustes(
        dialogo = dialogo,
        alConfirmarCerrarSesion = viewModel::alConfirmarCerrarSesion,
        alConfirmarEliminarCuenta = viewModel::alConfirmarEliminarCuenta,
        alCerrar = viewModel::alCerrarDialogo,
    )

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.destino_ajustes)) }) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val actual = estado) {
                EstadoAjustes.Cargando -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                EstadoAjustes.SinSesion -> Column(Modifier.verticalScroll(rememberScrollState())) {
                    InvitacionACrearCuenta(onIniciarSesion, onCrearCuenta)
                    ItemAjustes(
                        titulo = stringResource(R.string.ajustes_ia_titulo),
                        icono = R.drawable.ic_ia,
                        subtitulo = stringResource(R.string.ajustes_ia_sin_cuenta),
                        final = FinalItemAjustes.Interruptor(activo = false, onCambio = {}, habilitado = false),
                    )
                    ItemAjustes(
                        titulo = stringResource(R.string.ajustes_privacidad),
                        icono = R.drawable.ic_candado,
                        final = FinalItemAjustes.Flecha,
                        onClick = onPrivacidad,
                    )
                }
                is EstadoAjustes.ConSesion -> Column(Modifier.verticalScroll(rememberScrollState())) {
                    ItemAjustes(
                        titulo = actual.email,
                        icono = R.drawable.ic_persona,
                        subtitulo = if (actual.pendientes == 0) {
                            stringResource(R.string.ajustes_respaldo_al_dia)
                        } else {
                            pluralStringResource(R.plurals.ajustes_respaldo_pendientes, actual.pendientes, actual.pendientes)
                        },
                        final = FinalItemAjustes.Flecha,
                        onClick = onMiCuenta,
                    )
                    ItemAjustes(
                        titulo = stringResource(R.string.ajustes_ia_titulo),
                        icono = R.drawable.ic_ia,
                        subtitulo = stringResource(if (actual.iaActivada) R.string.ajustes_ia_activa else R.string.ajustes_ia_apagada),
                        final = FinalItemAjustes.Interruptor(activo = actual.iaActivada, onCambio = viewModel::alCambiarIa),
                    )
                    ItemAjustes(
                        titulo = stringResource(R.string.ajustes_privacidad),
                        icono = R.drawable.ic_candado,
                        final = FinalItemAjustes.Flecha,
                        onClick = onPrivacidad,
                    )
                    ItemAjustes(
                        titulo = stringResource(R.string.ajustes_cerrar_sesion),
                        icono = R.drawable.ic_cerrar_sesion,
                        onClick = viewModel::alPedirCerrarSesion,
                    )
                }
            }
        }
    }
}

@Composable
private fun InvitacionACrearCuenta(onIniciarSesion: () -> Unit, onCrearCuenta: () -> Unit) {
    OutlinedCard(Modifier.fillMaxWidth().padding(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.ajustes_sin_cuenta_titulo),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                stringResource(R.string.ajustes_sin_cuenta_mensaje),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onCrearCuenta, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.cuenta_crear)) }
            OutlinedButton(onClick = onIniciarSesion, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.cuenta_iniciar)) }
        }
    }
}
