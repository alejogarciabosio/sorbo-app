// Capa de presentación · pantalla: dibuja el estado que expone el ViewModel y le avisa de los eventos.
package com.sorbo.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sorbo.app.R
import com.sorbo.app.ui.components.Banner
import com.sorbo.app.ui.components.CampoDeCuenta
import com.sorbo.app.ui.components.IconoEnCirculo
import com.sorbo.app.ui.components.TipoBanner
import com.sorbo.app.ui.components.esDeLaClave
import com.sorbo.app.ui.components.esDeRepetirClave
import com.sorbo.app.ui.components.esDelServidor
import com.sorbo.app.ui.components.mensaje
import com.sorbo.app.ui.viewmodels.EstadoNuevaContrasena
import com.sorbo.app.ui.viewmodels.NuevaContrasenaViewModel

/** F6 (desde el enlace del email) y G4 (desde Ajustes): elegir la contraseña nueva y confirmar que se cambió. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaNuevaContrasena(viewModel: NuevaContrasenaViewModel, onAtras: () -> Unit, onContinuar: () -> Unit) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.cuenta_nueva_clave_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onAtras) {
                        Icon(painterResource(R.drawable.ic_atras), contentDescription = stringResource(R.string.accion_volver))
                    }
                },
            )
        },
    ) { padding ->
        when (val actual = estado) {
            is EstadoNuevaContrasena.Editando -> Formulario(actual, viewModel, Modifier.padding(padding))
            EstadoNuevaContrasena.Listo -> ClaveCambiada(onContinuar, Modifier.padding(padding))
        }
    }
}

@Composable
private fun Formulario(estado: EstadoNuevaContrasena.Editando, viewModel: NuevaContrasenaViewModel, modifier: Modifier) {
    val error = estado.error
    Column(
        modifier = modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            stringResource(R.string.cuenta_nueva_clave_subtitulo),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        if (error != null && error.esDelServidor && !error.esDeLaClave) {
            Banner(tipo = TipoBanner.Error, mensaje = error.mensaje())
        }
        CampoDeCuenta(
            valor = estado.clave,
            onCambio = viewModel::alCambiarClave,
            etiqueta = stringResource(R.string.cuenta_clave),
            icono = R.drawable.ic_candado,
            esClave = true,
            enabled = !estado.enviando,
            mensajeDeError = if (error != null && error.esDeLaClave) error.mensaje() else null,
        )
        CampoDeCuenta(
            valor = estado.repetirClave,
            onCambio = viewModel::alCambiarRepetirClave,
            etiqueta = stringResource(R.string.cuenta_repetir_clave),
            icono = R.drawable.ic_candado,
            esClave = true,
            enabled = !estado.enviando,
            mensajeDeError = if (error != null && error.esDeRepetirClave) error.mensaje() else null,
            imeAction = ImeAction.Done,
            onListo = viewModel::alGuardar,
        )
        BotonPrincipalDeCuenta(
            texto = stringResource(R.string.cuenta_guardar_clave),
            enviando = estado.enviando,
            onClick = viewModel::alGuardar,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun ClaveCambiada(onContinuar: () -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconoEnCirculo(R.drawable.ic_check)
        Text(
            stringResource(R.string.cuenta_clave_cambiada_titulo),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            stringResource(R.string.cuenta_clave_cambiada_mensaje),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onContinuar) { Text(stringResource(R.string.cuenta_continuar)) }
    }
}
