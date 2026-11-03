package com.sorbo.app.ui.diario

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sorbo.app.R
import com.sorbo.app.domain.model.EstadoSync
import com.sorbo.app.domain.model.VinoConDegustaciones
import com.sorbo.app.ui.components.EstadoVacio
import com.sorbo.app.ui.components.ItemVino
import com.sorbo.app.ui.components.TonoEtiqueta
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** Diario: la lista de vinos probados, del más reciente al más antiguo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDiario(
    viewModel: DiarioViewModel,
    onBuscar: () -> Unit,
    onAbrirVino: (String) -> Unit,
    onRegistrar: () -> Unit,
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.destino_diario)) },
                actions = {
                    IconButton(onClick = onBuscar) {
                        Icon(painterResource(R.drawable.ic_buscar), contentDescription = stringResource(R.string.titulo_buscar))
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val actual = estado) {
                EstadoDiario.Cargando -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                EstadoDiario.Vacio -> EstadoVacio(
                    icono = R.drawable.ic_vino,
                    titulo = stringResource(R.string.diario_vacio_titulo),
                    mensaje = stringResource(R.string.diario_vacio_mensaje),
                    textoAccion = stringResource(R.string.accion_registrar),
                    onAccion = onRegistrar,
                )
                is EstadoDiario.ConVinos -> ListaDeVinos(actual.vinos, onAbrirVino)
            }
        }
    }
}

@Composable
private fun ListaDeVinos(vinos: List<VinoConDegustaciones>, onAbrirVino: (String) -> Unit) {
    val formato = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())
    // Deja lugar abajo para que el botón "Registrar" no tape el último vino.
    LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 88.dp)) {
        items(vinos, key = { it.vino.id }) { item ->
            val vino = item.vino
            ItemVino(
                nombre = vino.nombre,
                detalle = listOfNotNull(vino.bodega, vino.cepa, vino.anio?.toString()).joinToString(" · "),
                cuando = item.ultimaFecha?.let { stringResource(R.string.diario_probado, it.format(formato)) }.orEmpty(),
                meGusto = item.gustoEnGeneral,
                iniciales = vino.nombre.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1).uppercase() },
                tono = TonoEtiqueta.entries[Math.floorMod(vino.nombreNormalizado.hashCode(), TonoEtiqueta.entries.size)],
                onClick = { onAbrirVino(vino.id) },
                pendienteRespaldo = vino.estadoSync == EstadoSync.PENDIENTE,
            )
        }
    }
}
