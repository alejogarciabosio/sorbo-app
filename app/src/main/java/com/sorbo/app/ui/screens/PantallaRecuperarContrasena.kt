// Capa de presentación · pantalla: dibuja el estado que expone el ViewModel y le avisa de los eventos.
package com.sorbo.app.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.sorbo.app.ui.components.esDelEmail
import com.sorbo.app.ui.components.esDelServidor
import com.sorbo.app.ui.components.mensaje
import com.sorbo.app.ui.viewmodels.EstadoRecuperarContrasena
import com.sorbo.app.ui.viewmodels.RecuperarContrasenaViewModel

/** F4: pedir el enlace con el email. F5: "Revisá tu email", con acceso a la app de correo y para reenviarlo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRecuperarContrasena(viewModel: RecuperarContrasenaViewModel, onAtras: () -> Unit) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { if (estado is EstadoRecuperarContrasena.Editando) Text(stringResource(R.string.titulo_recuperar_contrasena)) },
                navigationIcon = {
                    IconButton(onClick = onAtras) {
                        Icon(painterResource(R.drawable.ic_atras), contentDescription = stringResource(R.string.accion_volver))
                    }
                },
            )
        },
    ) { padding ->
        when (val actual = estado) {
            is EstadoRecuperarContrasena.Editando -> PedirEnlace(actual, viewModel, Modifier.padding(padding))
            is EstadoRecuperarContrasena.Enviado -> RevisaTuEmail(actual, viewModel, Modifier.padding(padding))
        }
    }
}

@Composable
private fun PedirEnlace(estado: EstadoRecuperarContrasena.Editando, viewModel: RecuperarContrasenaViewModel, modifier: Modifier) {
    val error = estado.error
    Column(
        modifier = modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            stringResource(R.string.cuenta_recuperar_subtitulo),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (error != null && error.esDelServidor && !error.esDelEmail) {
            Banner(tipo = TipoBanner.Error, mensaje = error.mensaje())
        }
        CampoDeCuenta(
            valor = estado.email,
            onCambio = viewModel::alCambiarEmail,
            etiqueta = stringResource(R.string.cuenta_email),
            icono = R.drawable.ic_email,
            enabled = !estado.enviando,
            mensajeDeError = if (error != null && error.esDelEmail) error.mensaje() else null,
            imeAction = ImeAction.Done,
            onListo = viewModel::alEnviarEnlace,
        )
        BotonPrincipalDeCuenta(
            texto = stringResource(R.string.cuenta_enviar_enlace),
            enviando = estado.enviando,
            onClick = viewModel::alEnviarEnlace,
        )
    }
}

@Composable
private fun RevisaTuEmail(estado: EstadoRecuperarContrasena.Enviado, viewModel: RecuperarContrasenaViewModel, modifier: Modifier) {
    val contexto = LocalContext.current
    var sinAppDeEmail by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconoEnCirculo(R.drawable.ic_email)
        Text(
            stringResource(R.string.cuenta_revisa_titulo),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            stringResource(R.string.cuenta_revisa_mensaje, estado.email),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (sinAppDeEmail) Banner(tipo = TipoBanner.Info, mensaje = stringResource(R.string.cuenta_sin_app_de_email))
        estado.error?.let { Banner(tipo = TipoBanner.Error, mensaje = it.mensaje()) }
        if (estado.reenviado) Banner(tipo = TipoBanner.Info, mensaje = stringResource(R.string.cuenta_enlace_reenviado))
        Button(onClick = { sinAppDeEmail = !abrirAppDeEmail(contexto) }) {
            Text(stringResource(R.string.cuenta_abrir_email))
        }
        TextButton(onClick = viewModel::alReenviarEnlace, enabled = !estado.reenviando) {
            Text(stringResource(if (estado.reenviando) R.string.cuenta_enviando else R.string.cuenta_reenviar_enlace))
        }
    }
}

/** Abre la app de correo que la persona tenga elegida. Devuelve falso si el teléfono no tiene ninguna. */
private fun abrirAppDeEmail(contexto: Context): Boolean = try {
    val intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_EMAIL).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    contexto.startActivity(intent)
    true
} catch (ausente: ActivityNotFoundException) {
    false
}
