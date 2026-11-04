// Capa de presentación · pantalla: dibuja el estado que expone el ViewModel y le avisa de los eventos.
package com.sorbo.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sorbo.app.R
import com.sorbo.app.domain.model.Degustacion
import com.sorbo.app.domain.usecase.DetalleVino
import com.sorbo.app.ui.components.CabeceraVino
import com.sorbo.app.ui.components.EstadoVacio
import com.sorbo.app.ui.components.TarjetaDegustacion
import com.sorbo.app.ui.components.detalleEnLista
import com.sorbo.app.ui.components.etiqueta
import com.sorbo.app.ui.components.inicialesDeEtiqueta
import com.sorbo.app.ui.components.textoDeFecha
import com.sorbo.app.ui.components.tonoDeEtiqueta
import com.sorbo.app.ui.viewmodels.DetalleViewModel
import com.sorbo.app.ui.viewmodels.EstadoDetalle
import com.sorbo.app.ui.viewmodels.VinoEliminado
import java.time.LocalDate

/** Ficha de un vino: su foto, cuántas veces se probó, si gustó y cada degustación, de la más reciente a la más antigua. */
@Composable
fun PantallaDetalle(
    viewModel: DetalleViewModel,
    hoy: LocalDate,
    onVolver: () -> Unit,
    onEditar: () -> Unit,
    onVolviAProbar: () -> Unit,
    onAbrirVino: (String) -> Unit,
    onEliminado: (VinoEliminado) -> Unit,
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val eliminado by viewModel.eliminado.collectAsStateWithLifecycle()
    var confirmandoEliminar by rememberSaveable { mutableStateOf(false) }

    eliminado?.let { LaunchedEffect(it) { onEliminado(it) } }

    when (val actual = estado) {
        EstadoDetalle.Cargando -> Cargando()
        EstadoDetalle.NoEncontrado -> if (eliminado == null) {
            EstadoVacio(
                icono = R.drawable.ic_vino,
                titulo = stringResource(R.string.detalle_no_encontrado_titulo),
                mensaje = stringResource(R.string.detalle_no_encontrado_mensaje),
                textoAccion = stringResource(R.string.accion_volver),
                onAccion = onVolver,
            )
        }
        is EstadoDetalle.Contenido -> {
            Contenido(
                detalle = actual.detalle,
                hoy = hoy,
                onVolver = onVolver,
                onEditar = onEditar,
                onEliminar = { confirmandoEliminar = true },
                onVolviAProbar = onVolviAProbar,
                onAbrirVino = onAbrirVino,
            )
            if (confirmandoEliminar) {
                ConfirmarEliminar(
                    nombre = actual.detalle.vino.nombre,
                    veces = actual.detalle.veces,
                    onConfirmar = {
                        confirmandoEliminar = false
                        viewModel.alEliminar()
                    },
                    onCancelar = { confirmandoEliminar = false },
                )
            }
        }
    }
}

@Composable
private fun Cargando() {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Contenido(
    detalle: DetalleVino,
    hoy: LocalDate,
    onVolver: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    onVolviAProbar: () -> Unit,
    onAbrirVino: (String) -> Unit,
) {
    val vino = detalle.vino
    Scaffold(
        floatingActionButton = {
            // Nombre explícito: el texto del botón flotante no llega al lector de pantalla por sí solo.
            val nombreDelBoton = stringResource(R.string.detalle_lo_volvi_a_probar)
            ExtendedFloatingActionButton(
                onClick = onVolviAProbar,
                modifier = Modifier.semantics { contentDescription = nombreDelBoton },
                icon = { Icon(painterResource(R.drawable.ic_agregar), contentDescription = null) },
                text = { Text(stringResource(R.string.detalle_lo_volvi_a_probar)) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            // Deja lugar abajo para que el botón flotante no tape la última degustación.
            contentPadding = PaddingValues(bottom = padding.calculateBottomPadding() + 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                CabeceraVino(
                    inicial = vino.inicialesDeEtiqueta().take(1),
                    tono = vino.tonoDeEtiqueta(),
                    fotoPath = vino.fotoPath,
                    onVolver = onVolver,
                    onEditar = onEditar,
                    onEliminar = onEliminar,
                )
            }
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(vino.nombre, style = MaterialTheme.typography.headlineMedium)
                    val datos = vino.detalleEnLista()
                    if (datos.isNotEmpty()) {
                        Text(datos, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ResumenDeVeredicto(detalle)
                        detalle.dulzorHabitual?.let { dulzor ->
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                color = MaterialTheme.colorScheme.surface,
                            ) {
                                Text(
                                    stringResource(dulzor.etiqueta()),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                        }
                    }
                }
            }
            if (detalle.otrasAnadas.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.detalle_otras_anadas), style = MaterialTheme.typography.titleSmall)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            detalle.otrasAnadas.forEach { otra ->
                                val anio = otra.anio
                                if (anio != null) {
                                    val descripcion = stringResource(R.string.detalle_ir_a_anada, anio)
                                    AssistChip(
                                        onClick = { onAbrirVino(otra.id) },
                                        label = { Text(anio.toString()) },
                                        modifier = Modifier.heightIn(min = 48.dp).semantics { contentDescription = descripcion },
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(stringResource(R.string.detalle_veces_titulo), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(detalle.veces.toString(), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(detalle.degustaciones, key = { it.id }) { degustacion ->
                Tarjeta(degustacion, hoy)
            }
        }
    }
}

@Composable
private fun ResumenDeVeredicto(detalle: DetalleVino) {
    val colores = MaterialTheme.colorScheme
    val gusto = detalle.gustoEnGeneral
    val fondo = if (gusto) colores.primaryContainer else colores.surfaceContainerHighest
    val contenido = if (gusto) colores.onPrimaryContainer else colores.onSurfaceVariant
    Row(
        modifier = Modifier.background(fondo, MaterialTheme.shapes.small).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            painterResource(if (gusto) R.drawable.ic_me_gusto else R.drawable.ic_no_me_gusto),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = contenido,
        )
        Text(
            pluralStringResource(R.plurals.detalle_te_gusto, detalle.veces, detalle.vecesQueGusto, detalle.veces),
            style = MaterialTheme.typography.labelLarge,
            color = contenido,
        )
    }
}

@Composable
private fun Tarjeta(degustacion: Degustacion, hoy: LocalDate) {
    TarjetaDegustacion(
        fecha = textoDeFecha(degustacion.fecha, hoy),
        meGusto = degustacion.meGusto,
        modifier = Modifier.padding(horizontal = 16.dp),
        comida = degustacion.comida?.let { stringResource(it.etiqueta()) },
        compania = degustacion.compania?.let { stringResource(it.etiqueta()) },
        ocasion = degustacion.ocasion?.let { stringResource(it.etiqueta()) },
        dulzor = degustacion.dulzor?.let { stringResource(it.etiqueta()) },
        nota = degustacion.nota,
    )
}

@Composable
private fun ConfirmarEliminar(nombre: String, veces: Int, onConfirmar: () -> Unit, onCancelar: () -> Unit) {
    // Con pantallas angostas y fuente grande el diálogo ocupaba todo el ancho: se le deja un margen a cada lado.
    AlertDialog(
        onDismissRequest = onCancelar,
        modifier = Modifier.padding(horizontal = 16.dp),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        icon = { Icon(painterResource(R.drawable.ic_eliminar), contentDescription = null) },
        title = { Text(stringResource(R.string.detalle_eliminar_titulo)) },
        text = { Text(pluralStringResource(R.plurals.detalle_eliminar_mensaje, veces, nombre, veces)) },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text(stringResource(R.string.accion_eliminar), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text(stringResource(R.string.accion_cancelar)) } },
    )
}
