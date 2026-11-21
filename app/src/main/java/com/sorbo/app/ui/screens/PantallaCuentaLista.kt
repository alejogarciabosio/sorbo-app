// Capa de presentación · pantalla: dibuja el estado que expone el ViewModel y le avisa de los eventos.
package com.sorbo.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sorbo.app.R
import com.sorbo.app.ui.components.IconoEnCirculo

/** F7: la cuenta quedó creada. Dice cuántos vinos del teléfono se van a respaldar, o que desde ahora se respalda lo que se cargue. */
@Composable
fun PantallaCuentaLista(vinosDelTelefono: Int, onContinuar: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconoEnCirculo(R.drawable.ic_respaldo_ok)
        Text(
            stringResource(R.string.cuenta_lista_titulo),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            if (vinosDelTelefono > 0) {
                pluralStringResource(R.plurals.cuenta_lista_mensaje, vinosDelTelefono, vinosDelTelefono)
            } else {
                stringResource(R.string.cuenta_lista_sin_vinos)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onContinuar) { Text(stringResource(R.string.cuenta_continuar)) }
    }
}
