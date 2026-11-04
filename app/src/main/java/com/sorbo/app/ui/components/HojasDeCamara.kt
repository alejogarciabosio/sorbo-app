// Capa de presentación · componentes sin estado: las hojas que explican el permiso de la cámara y qué hacer si se rechaza.
package com.sorbo.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sorbo.app.R

/** Antes de pedir el permiso: para qué usa Sorbo la cámara. */
@Composable
fun HojaPermisoCamara(onContinuar: () -> Unit, onAhoraNo: () -> Unit) {
    HojaDeAviso(
        icono = R.drawable.ic_camara,
        titulo = stringResource(R.string.camara_explicacion_titulo),
        mensaje = stringResource(R.string.camara_explicacion_mensaje),
        onCerrar = onAhoraNo,
    ) {
        Button(onClick = onContinuar, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
            Text(stringResource(R.string.camara_continuar))
        }
        TextButton(onClick = onAhoraNo, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(R.string.camara_ahora_no))
        }
    }
}

/** Si rechazó el permiso: puede elegir de la galería o seguir sin foto (RNF09). */
@Composable
fun HojaCamaraDenegada(onElegirDeGaleria: () -> Unit, onContinuarSinFoto: () -> Unit, onAbrirAjustes: () -> Unit) {
    HojaDeAviso(
        icono = R.drawable.ic_camara,
        titulo = stringResource(R.string.camara_denegada_titulo),
        mensaje = stringResource(R.string.camara_denegada_mensaje),
        onCerrar = onContinuarSinFoto,
    ) {
        Button(onClick = onElegirDeGaleria, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
            Icon(painterResource(R.drawable.ic_galeria), contentDescription = null, modifier = Modifier.size(20.dp))
            Text(stringResource(R.string.camara_elegir_galeria), modifier = Modifier.padding(start = 8.dp))
        }
        FilledTonalButton(onClick = onContinuarSinFoto, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
            Text(stringResource(R.string.camara_sin_foto))
        }
        TextButton(onClick = onAbrirAjustes, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(R.string.camara_abrir_ajustes))
        }
    }
}
