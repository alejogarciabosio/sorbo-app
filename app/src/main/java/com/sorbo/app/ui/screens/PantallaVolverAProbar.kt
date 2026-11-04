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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sorbo.app.R
import com.sorbo.app.ui.components.AccionesDeDegustacion
import com.sorbo.app.ui.components.EstadoVacio
import com.sorbo.app.ui.components.MiniaturaEtiqueta
import com.sorbo.app.ui.components.PreguntasDeDegustacion
import com.sorbo.app.ui.components.RespuestasDeDegustacion
import com.sorbo.app.ui.components.SelectorDeFecha
import com.sorbo.app.ui.components.inicialesDeEtiqueta
import com.sorbo.app.ui.components.tonoDeEtiqueta
import com.sorbo.app.ui.viewmodels.EstadoVolverAProbar
import com.sorbo.app.ui.viewmodels.VolverAProbarViewModel

/** "Lo volví a probar": suma otra degustación a un vino que ya está en el diario, con lo de la última vez ya marcado. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaVolverAProbar(
    viewModel: VolverAProbarViewModel,
    onCerrar: () -> Unit,
    onGuardado: (EstadoVolverAProbar.Guardado) -> Unit,
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var eligiendoFecha by rememberSaveable { mutableStateOf(false) }

    (estado as? EstadoVolverAProbar.Guardado)?.let { guardado ->
        LaunchedEffect(guardado) { onGuardado(guardado) }
    }
    val editando = estado as? EstadoVolverAProbar.Editando

    if (eligiendoFecha && editando != null) {
        SelectorDeFecha(
            fecha = editando.campos.fecha,
            hoy = viewModel.hoy,
            onElegir = viewModel::alCambiarFecha,
            onCerrar = { eligiendoFecha = false },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_volver_a_probar)) },
                navigationIcon = {
                    IconButton(onClick = onCerrar) {
                        Icon(painterResource(R.drawable.ic_cerrar), contentDescription = stringResource(R.string.accion_cerrar))
                    }
                },
            )
        },
        bottomBar = {
            if (editando != null) {
                // El botón sube con el teclado para que "Guardar" siempre se pueda tocar.
                Surface(modifier = Modifier.imePadding(), tonalElevation = 3.dp) {
                    Button(
                        onClick = viewModel::alGuardar,
                        modifier = Modifier.navigationBarsPadding().fillMaxWidth().padding(16.dp).heightIn(min = 56.dp),
                    ) {
                        Icon(painterResource(R.drawable.ic_check), contentDescription = null, modifier = Modifier.size(20.dp))
                        Text(stringResource(R.string.volver_a_probar_guardar), modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
    ) { padding ->
        when (val actual = estado) {
            EstadoVolverAProbar.Cargando, is EstadoVolverAProbar.Guardado -> Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) { CircularProgressIndicator() }
            EstadoVolverAProbar.NoEncontrado -> EstadoVacio(
                icono = R.drawable.ic_vino,
                titulo = stringResource(R.string.detalle_no_encontrado_titulo),
                mensaje = stringResource(R.string.detalle_no_encontrado_mensaje),
                textoAccion = stringResource(R.string.accion_volver),
                onAccion = onCerrar,
                modifier = Modifier.padding(padding),
            )
            is EstadoVolverAProbar.Editando -> Contenido(
                estado = actual,
                viewModel = viewModel,
                onElegirFecha = { eligiendoFecha = true },
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun Contenido(
    estado: EstadoVolverAProbar.Editando,
    viewModel: VolverAProbarViewModel,
    onElegirFecha: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vino = estado.detalle.vino
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MiniaturaEtiqueta(
                iniciales = vino.inicialesDeEtiqueta(),
                tono = vino.tonoDeEtiqueta(),
                fotoPath = vino.fotoPath,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(vino.nombre, style = MaterialTheme.typography.titleLarge)
                Text(
                    pluralStringResource(R.plurals.detalle_te_gusto, estado.detalle.veces, estado.detalle.vecesQueGusto, estado.detalle.veces),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        PreguntasDeDegustacion(
            respuestas = RespuestasDeDegustacion(
                meGusto = estado.campos.meGusto,
                dulzor = estado.campos.dulzor,
                comida = estado.campos.comida,
                compania = estado.campos.compania,
                ocasion = estado.campos.ocasion,
                fecha = estado.campos.fecha,
                nota = estado.campos.nota,
            ),
            hoy = viewModel.hoy,
            faltaOpinion = estado.faltaOpinion,
            acciones = AccionesDeDegustacion(
                onMeGusto = viewModel::alElegirMeGusto,
                onDulzor = viewModel::alElegirDulzor,
                onComida = viewModel::alElegirComida,
                onCompania = viewModel::alElegirCompania,
                onOcasion = viewModel::alElegirOcasion,
                onNota = viewModel::alCambiarNota,
                onElegirFecha = onElegirFecha,
            ),
            detalleDelDulzor = if (estado.precargado) stringResource(R.string.volver_a_probar_precargado) else null,
        )
    }
}
