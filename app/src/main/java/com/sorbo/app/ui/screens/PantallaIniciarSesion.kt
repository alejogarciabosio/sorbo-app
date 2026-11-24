// Capa de presentación · pantalla: dibuja el estado que expone el ViewModel y le avisa de los eventos.
package com.sorbo.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sorbo.app.R
import com.sorbo.app.ui.components.Banner
import com.sorbo.app.ui.components.CampoDeCuenta
import com.sorbo.app.ui.components.TipoBanner
import com.sorbo.app.ui.components.esDeLaClave
import com.sorbo.app.ui.components.esDelEmail
import com.sorbo.app.ui.components.esDelServidor
import com.sorbo.app.ui.components.mensaje
import com.sorbo.app.ui.viewmodels.EstadoIniciarSesion
import com.sorbo.app.ui.viewmodels.IniciarSesionViewModel

/** F1 y F2: iniciar sesión. Si hay un error del servidor se ve en un aviso arriba; los de validación, pegados al campo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaIniciarSesion(
    viewModel: IniciarSesionViewModel,
    onCerrar: () -> Unit,
    onCrearCuenta: () -> Unit,
    onOlvideLaClave: () -> Unit,
    onListo: (hayRespaldoParaRestaurar: Boolean) -> Unit,
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    LaunchedEffect(estado) { (estado as? EstadoIniciarSesion.Listo)?.let { onListo(it.hayRespaldoParaRestaurar) } }
    val editando = estado as? EstadoIniciarSesion.Editando ?: return
    val error = editando.error

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onCerrar) {
                        Icon(painterResource(R.drawable.ic_cerrar), contentDescription = stringResource(R.string.accion_cerrar))
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().imePadding()) {
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                LogoDeSorbo()
                Text(
                    stringResource(R.string.cuenta_iniciar_titulo),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    stringResource(R.string.cuenta_iniciar_subtitulo),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (error != null && error.esDelServidor) {
                    Banner(tipo = TipoBanner.Error, mensaje = error.mensaje())
                }
                CampoDeCuenta(
                    valor = editando.email,
                    onCambio = viewModel::alCambiarEmail,
                    etiqueta = stringResource(R.string.cuenta_email),
                    icono = R.drawable.ic_email,
                    enabled = !editando.enviando,
                    hayError = error?.esDelEmail == true,
                    mensajeDeError = if (error != null && error.esDelEmail && !error.esDelServidor) error.mensaje() else null,
                )
                CampoDeCuenta(
                    valor = editando.clave,
                    onCambio = viewModel::alCambiarClave,
                    etiqueta = stringResource(R.string.cuenta_clave),
                    icono = R.drawable.ic_candado,
                    esClave = true,
                    enabled = !editando.enviando,
                    hayError = error?.esDeLaClave == true,
                    mensajeDeError = if (error != null && error.esDeLaClave && !error.esDelServidor) error.mensaje() else null,
                    imeAction = ImeAction.Done,
                    onListo = viewModel::alIniciarSesion,
                )
                TextButton(onClick = onOlvideLaClave, modifier = Modifier.align(Alignment.End)) {
                    Text(stringResource(R.string.cuenta_olvide_clave))
                }
                BotonPrincipalDeCuenta(
                    texto = stringResource(R.string.cuenta_iniciar),
                    enviando = editando.enviando,
                    onClick = viewModel::alIniciarSesion,
                )
                TextButton(onClick = onCrearCuenta, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(stringResource(R.string.cuenta_crear_nueva))
                }
            }
            OutlinedButton(
                onClick = onCerrar,
                modifier = Modifier.navigationBarsPadding().fillMaxWidth().padding(24.dp).heightIn(min = 56.dp),
            ) {
                Text(stringResource(R.string.cuenta_seguir_sin))
            }
        }
    }
}

/** La marca de Sorbo sobre fondo bordó. Es decorativa: el título de la pantalla ya dice dónde está la persona. */
@Composable
internal fun LogoDeSorbo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(56.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(R.drawable.ic_vino),
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

/** El botón grande de los formularios de cuenta: mientras espera se deshabilita (no hay doble toque) y muestra que trabaja. */
@Composable
internal fun BotonPrincipalDeCuenta(texto: String, enviando: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, enabled = !enviando, modifier = modifier.fillMaxWidth().heightIn(min = 56.dp)) {
        if (enviando) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = LocalContentColor.current)
            Spacer(Modifier.width(12.dp))
            Text(stringResource(R.string.cuenta_enviando))
        } else {
            Text(texto)
        }
    }
}
