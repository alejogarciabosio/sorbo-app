// Capa de presentación · pantalla: dibuja el estado que expone el ViewModel y le avisa de los eventos.
package com.sorbo.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.sorbo.app.R

/** G7: cómo usamos los datos, en lenguaje simple y diciendo qué sale del teléfono y qué no. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrivacidad(onAtras: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_privacidad)) },
                navigationIcon = {
                    IconButton(onClick = onAtras) {
                        Icon(painterResource(R.drawable.ic_atras), contentDescription = stringResource(R.string.accion_volver))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Seccion(R.string.privacidad_telefono_titulo, R.string.privacidad_telefono_mensaje)
            Seccion(R.string.privacidad_cuenta_titulo, R.string.privacidad_cuenta_mensaje)
            Seccion(R.string.privacidad_etiqueta_titulo, R.string.privacidad_etiqueta_mensaje)
            Seccion(R.string.privacidad_resumen_titulo, R.string.privacidad_resumen_mensaje)
            Seccion(R.string.privacidad_control_titulo, R.string.privacidad_control_mensaje)
        }
    }
}

@Composable
private fun Seccion(titulo: Int, mensaje: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(titulo), style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
        Text(stringResource(mensaje), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
