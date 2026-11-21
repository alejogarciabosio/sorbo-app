// Capa de presentación · pantalla: dibuja el estado que expone el ViewModel y le avisa de los eventos.
package com.sorbo.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sorbo.app.R
import com.sorbo.app.domain.util.LARGO_MINIMO_CLAVE
import com.sorbo.app.ui.components.Banner
import com.sorbo.app.ui.components.CampoDeCuenta
import com.sorbo.app.ui.components.TipoBanner
import com.sorbo.app.ui.components.esDeLaClave
import com.sorbo.app.ui.components.esDeRepetirClave
import com.sorbo.app.ui.components.esDelEmail
import com.sorbo.app.ui.components.esDelServidor
import com.sorbo.app.ui.components.mensaje
import com.sorbo.app.ui.viewmodels.CrearCuentaViewModel
import com.sorbo.app.ui.viewmodels.ErrorDeCuenta
import com.sorbo.app.ui.viewmodels.EstadoCrearCuenta

/** F3: crear cuenta. Cuando se crea, avisa cuántos vinos del teléfono se van a respaldar (F7) con [onListo]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCrearCuenta(
    viewModel: CrearCuentaViewModel,
    onAtras: () -> Unit,
    onYaTengoCuenta: () -> Unit,
    onVerPrivacidad: () -> Unit,
    onListo: (vinosDelTelefono: Int) -> Unit,
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    (estado as? EstadoCrearCuenta.Listo)?.let { listo -> LaunchedEffect(listo) { onListo(listo.vinosDelTelefono) } }
    val editando = estado as? EstadoCrearCuenta.Editando ?: return
    val error = editando.error

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.cuenta_crear_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onAtras) {
                        Icon(painterResource(R.drawable.ic_atras), contentDescription = stringResource(R.string.accion_volver))
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().imePadding()) {
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (error != null && error.esDelServidor && !error.esDelEmail && !error.esDeLaClave) {
                    Banner(tipo = TipoBanner.Error, mensaje = error.mensaje())
                }
                CampoDeCuenta(
                    valor = editando.email,
                    onCambio = viewModel::alCambiarEmail,
                    etiqueta = stringResource(R.string.cuenta_email),
                    icono = R.drawable.ic_email,
                    enabled = !editando.enviando,
                    mensajeDeError = if (error != null && error.esDelEmail) error.mensaje() else null,
                )
                CampoDeCuenta(
                    valor = editando.clave,
                    onCambio = viewModel::alCambiarClave,
                    etiqueta = stringResource(R.string.cuenta_clave),
                    icono = R.drawable.ic_candado,
                    esClave = true,
                    enabled = !editando.enviando,
                    mensajeDeError = if (error != null && error.esDeLaClave) error.mensaje() else null,
                    textoDeApoyo = { RequisitoDeClave(cumple = editando.claveCumpleElMinimo) },
                )
                CampoDeCuenta(
                    valor = editando.repetirClave,
                    onCambio = viewModel::alCambiarRepetirClave,
                    etiqueta = stringResource(R.string.cuenta_repetir_clave),
                    icono = R.drawable.ic_candado,
                    esClave = true,
                    enabled = !editando.enviando,
                    mensajeDeError = if (error != null && error.esDeRepetirClave) error.mensaje() else null,
                    imeAction = ImeAction.Done,
                    onListo = viewModel::alCrearCuenta,
                )
                AceptoTerminos(
                    acepto = editando.aceptoTerminos,
                    hayError = error == ErrorDeCuenta.TerminosSinAceptar,
                    onCambio = viewModel::alCambiarTerminos,
                    onVerPrivacidad = onVerPrivacidad,
                )
            }
            Column(
                modifier = Modifier.navigationBarsPadding().padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BotonPrincipalDeCuenta(
                    texto = stringResource(R.string.cuenta_crear),
                    enviando = editando.enviando,
                    onClick = viewModel::alCrearCuenta,
                )
                TextButton(onClick = onYaTengoCuenta) { Text(stringResource(R.string.cuenta_ya_tengo)) }
            }
        }
    }
}

/** El requisito se tilda al cumplirse: lleva ícono y texto, así que no depende solo del color. */
@Composable
private fun RequisitoDeClave(cumple: Boolean) {
    val color = if (cumple) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        if (cumple) Icon(painterResource(R.drawable.ic_check), contentDescription = null, modifier = Modifier.size(16.dp), tint = color)
        Text(stringResource(R.string.cuenta_clave_requisito, LARGO_MINIMO_CLAVE), color = color)
    }
}

/** Toda la fila se toca para marcar (el objetivo mide al menos 48 dp) y el enlace a privacidad queda aparte. */
@Composable
private fun AceptoTerminos(acepto: Boolean, hayError: Boolean, onCambio: (Boolean) -> Unit, onVerPrivacidad: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .toggleable(value = acepto, role = Role.Checkbox, onValueChange = onCambio),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = acepto, onCheckedChange = null)
            Text(
                stringResource(R.string.cuenta_acepto_terminos),
                modifier = Modifier.padding(start = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (hayError) {
            Text(
                ErrorDeCuenta.TerminosSinAceptar.mensaje(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
        TextButton(onClick = onVerPrivacidad) { Text(stringResource(R.string.cuenta_ver_privacidad)) }
    }
}
