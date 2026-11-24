// Capa de presentación · pantalla: dibuja el estado que expone el ViewModel y le avisa de los eventos.
package com.sorbo.app.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sorbo.app.R
import com.sorbo.app.domain.model.EstadoRestauracion
import com.sorbo.app.domain.model.MotivoRestauracion
import com.sorbo.app.ui.components.Banner
import com.sorbo.app.ui.components.IconoEnCirculo
import com.sorbo.app.ui.components.TipoBanner
import com.sorbo.app.ui.viewmodels.RestaurarViewModel

/** F8: traer el diario de la nube a este teléfono, con el progreso a la vista y la posibilidad de reintentar. */
@Composable
fun PantallaRestaurar(viewModel: RestaurarViewModel, onContinuar: () -> Unit) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (val actual = estado) {
                EstadoRestauracion.SinIniciar -> {
                    Cabecera(R.drawable.ic_respaldo_ok, R.string.restaurar_encontrado_titulo)
                    Mensaje(stringResource(R.string.restaurar_encontrado_mensaje))
                    Button(onClick = viewModel::alRestaurar, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.restaurar_boton))
                    }
                    TextButton(onClick = onContinuar) { Text(stringResource(R.string.restaurar_ahora_no)) }
                }
                is EstadoRestauracion.Restaurando -> {
                    Cabecera(R.drawable.ic_sincronizar, R.string.restaurar_trayendo_titulo)
                    LinearProgressIndicator(
                        progress = { if (actual.total == 0) 0f else actual.bajados.toFloat() / actual.total },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Mensaje(stringResource(R.string.restaurar_trayendo_progreso, actual.bajados, actual.total))
                    Text(
                        stringResource(R.string.restaurar_trayendo_nota),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
                is EstadoRestauracion.Terminada -> {
                    Cabecera(R.drawable.ic_respaldo_ok, R.string.restaurar_listo_titulo)
                    Mensaje(pluralStringResource(R.plurals.restaurar_listo_mensaje, actual.vinos, actual.vinos))
                    Button(onClick = onContinuar) { Text(stringResource(R.string.cuenta_continuar)) }
                }
                EstadoRestauracion.SinRespaldo -> {
                    Cabecera(R.drawable.ic_respaldo_ok, R.string.restaurar_vacio_titulo)
                    Mensaje(stringResource(R.string.restaurar_vacio_mensaje))
                    Button(onClick = onContinuar) { Text(stringResource(R.string.cuenta_continuar)) }
                }
                is EstadoRestauracion.Error -> {
                    Cabecera(R.drawable.ic_error, R.string.restaurar_error_titulo)
                    Banner(tipo = TipoBanner.Error, mensaje = stringResource(actual.motivo.mensaje()))
                    Button(onClick = viewModel::alRestaurar, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.accion_reintentar))
                    }
                    TextButton(onClick = onContinuar) { Text(stringResource(R.string.restaurar_ahora_no)) }
                }
            }
        }
    }
}

/** El texto de cada motivo; el `when` es exhaustivo, así un motivo nuevo obliga a escribir su mensaje. */
private fun MotivoRestauracion.mensaje(): Int = when (this) {
    MotivoRestauracion.SIN_CONEXION -> R.string.restaurar_error_sin_conexion
    MotivoRestauracion.SIN_SESION -> R.string.restaurar_error_sin_sesion
    MotivoRestauracion.ERROR_DESCONOCIDO -> R.string.restaurar_error_desconocido
}

@Composable
private fun Cabecera(@DrawableRes icono: Int, titulo: Int) {
    IconoEnCirculo(icono)
    Text(
        stringResource(titulo),
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
private fun Mensaje(texto: String) {
    Text(texto, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
}
