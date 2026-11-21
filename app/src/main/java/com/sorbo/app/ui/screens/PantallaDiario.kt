// Capa de presentación · pantalla: dibuja el estado que expone el ViewModel y le avisa de los eventos.
package com.sorbo.app.ui.screens

import com.sorbo.app.ui.components.TipoBanner
import com.sorbo.app.ui.components.Banner
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sorbo.app.R
import com.sorbo.app.ui.components.EstadoVacio
import com.sorbo.app.ui.components.ListaDeVinos
import com.sorbo.app.ui.viewmodels.DiarioViewModel
import com.sorbo.app.ui.viewmodels.EstadoDiario

/** Diario: la lista de vinos probados, del más reciente al más antiguo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDiario(
    viewModel: DiarioViewModel,
    onBuscar: () -> Unit,
    onAbrirVino: (String) -> Unit,
    onRegistrar: () -> Unit,
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val sinConexion by viewModel.sinConexion.collectAsStateWithLifecycle()
    val conCuenta by viewModel.conCuenta.collectAsStateWithLifecycle()
    val pendientes by viewModel.pendientes.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.destino_diario)) },
                actions = {
                    IconButton(onClick = onBuscar) {
                        Icon(painterResource(R.drawable.ic_buscar), contentDescription = stringResource(R.string.titulo_buscar))
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (sinConexion) {
                Banner(
                    tipo = TipoBanner.SinConexion,
                    mensaje = if (conCuenta && pendientes > 0) {
                        stringResource(R.string.diario_sin_conexion) + " " +
                            pluralStringResource(R.plurals.ajustes_respaldo_pendientes, pendientes, pendientes) + "."
                    } else {
                        stringResource(R.string.diario_sin_conexion)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (val actual = estado) {
                    EstadoDiario.Cargando -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    EstadoDiario.Vacio -> EstadoVacio(
                        icono = R.drawable.ic_vino,
                        titulo = stringResource(R.string.diario_vacio_titulo),
                        mensaje = stringResource(R.string.diario_vacio_mensaje),
                        textoAccion = stringResource(R.string.accion_registrar),
                        onAccion = onRegistrar,
                    )
                    EstadoDiario.ConError -> EstadoVacio(
                        icono = R.drawable.ic_error,
                        titulo = stringResource(R.string.diario_error_titulo),
                        mensaje = stringResource(R.string.diario_error_mensaje),
                        textoAccion = stringResource(R.string.accion_reintentar),
                        onAccion = viewModel::alReintentar,
                    )
                    is EstadoDiario.ConVinos -> ListaDeVinos(actual.vinos, onAbrirVino, mostrarPendientes = conCuenta)
                }
            }
        }
    }
}
