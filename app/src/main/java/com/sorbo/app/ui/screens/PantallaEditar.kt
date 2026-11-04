// Capa de presentación · pantalla: dibuja el estado que expone el ViewModel y le avisa de los eventos.
package com.sorbo.app.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sorbo.app.R
import com.sorbo.app.ui.components.Banner
import com.sorbo.app.ui.components.EstadoVacio
import com.sorbo.app.ui.components.FilaFoto
import com.sorbo.app.ui.components.TipoBanner
import com.sorbo.app.ui.components.rememberAccionesDeFoto
import com.sorbo.app.ui.viewmodels.CamposEdicion
import com.sorbo.app.ui.viewmodels.EditarViewModel
import com.sorbo.app.ui.viewmodels.EstadoEdicion

/** Corregir los datos de un vino (nombre, bodega, cepa, año y foto). Las degustaciones no se tocan desde acá. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEditar(
    viewModel: EditarViewModel,
    onCerrar: () -> Unit,
    onGuardado: () -> Unit,
    onNuevaDireccionParaCamara: () -> Uri,
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val cerrar = {
        viewModel.alCerrar()
        onCerrar()
    }
    BackHandler(onBack = cerrar)
    val accionesDeFoto = rememberAccionesDeFoto(viewModel::alElegirFoto, onNuevaDireccionParaCamara)

    if (estado == EstadoEdicion.Guardado) LaunchedEffect(Unit) { onGuardado() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_editar)) },
                navigationIcon = {
                    IconButton(onClick = cerrar) {
                        Icon(painterResource(R.drawable.ic_cerrar), contentDescription = stringResource(R.string.accion_cerrar))
                    }
                },
            )
        },
        bottomBar = {
            val editando = estado as? EstadoEdicion.Editando
            if (editando != null) {
                Surface(tonalElevation = 3.dp) {
                    Button(
                        onClick = viewModel::alGuardar,
                        enabled = !editando.campos.procesandoFoto,
                        modifier = Modifier.navigationBarsPadding().fillMaxWidth().padding(16.dp).heightIn(min = 56.dp),
                    ) {
                        Icon(painterResource(R.drawable.ic_check), contentDescription = null, modifier = Modifier.size(20.dp))
                        Text(stringResource(R.string.editar_guardar), modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
    ) { padding ->
        when (val actual = estado) {
            EstadoEdicion.Cargando, EstadoEdicion.Guardado -> Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) { CircularProgressIndicator() }
            EstadoEdicion.NoEncontrado -> EstadoVacio(
                icono = R.drawable.ic_vino,
                titulo = stringResource(R.string.detalle_no_encontrado_titulo),
                mensaje = stringResource(R.string.detalle_no_encontrado_mensaje),
                textoAccion = stringResource(R.string.accion_volver),
                onAccion = onCerrar,
                modifier = Modifier.padding(padding),
            )
            is EstadoEdicion.Editando -> Formulario(
                campos = actual.campos,
                nombreVacio = actual.nombreVacio,
                viewModel = viewModel,
                onSacarFoto = accionesDeFoto.sacarFoto,
                onElegirDeGaleria = accionesDeFoto.elegirDeGaleria,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun Formulario(
    campos: CamposEdicion,
    nombreVacio: Boolean,
    viewModel: EditarViewModel,
    onSacarFoto: () -> Unit,
    onElegirDeGaleria: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        FilaFoto(
            fotoPath = campos.fotoPath,
            procesando = campos.procesandoFoto,
            onSacarFoto = onSacarFoto,
            onElegirDeGaleria = onElegirDeGaleria,
            onQuitar = viewModel::alQuitarFoto,
        )
        if (campos.errorFoto) {
            Banner(tipo = TipoBanner.Error, mensaje = stringResource(R.string.foto_error))
        }
        OutlinedTextField(
            value = campos.nombre,
            onValueChange = viewModel::alCambiarNombre,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.registro_nombre)) },
            isError = nombreVacio,
            supportingText = if (nombreVacio) {
                { Text(stringResource(R.string.registro_falta_nombre)) }
            } else {
                null
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
        )
        OutlinedTextField(
            value = campos.bodega,
            onValueChange = viewModel::alCambiarBodega,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.registro_bodega)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = campos.cepa,
                onValueChange = viewModel::alCambiarCepa,
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(R.string.registro_cepa)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            )
            OutlinedTextField(
                value = campos.anio,
                onValueChange = viewModel::alCambiarAnio,
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(R.string.registro_anio)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            )
        }
    }
}
