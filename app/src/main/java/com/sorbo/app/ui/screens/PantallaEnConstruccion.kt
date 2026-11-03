package com.sorbo.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sorbo.app.R

/** Un botón de acceso a otra pantalla, para poder probar la navegación mientras no existen las pantallas reales. */
data class AccesoDePrueba(val texto: Int, val destino: String)

/**
 * Pantalla provisoria: muestra el título y botones para recorrer la navegación.
 * Cada pantalla se reemplaza por la real en el paso que le corresponde.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEnConstruccion(
    titulo: Int,
    accesos: List<AccesoDePrueba>,
    onIr: (String) -> Unit,
    modifier: Modifier = Modifier,
    onAtras: (() -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(titulo)) },
            navigationIcon = {
                if (onAtras != null) {
                    IconButton(onClick = onAtras) {
                        Icon(painterResource(R.drawable.ic_atras), contentDescription = stringResource(R.string.accion_volver))
                    }
                }
            },
        )
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.en_construccion),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            accesos.forEach { acceso ->
                FilledTonalButton(onClick = { onIr(acceso.destino) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(acceso.texto))
                }
            }
        }
    }
}
