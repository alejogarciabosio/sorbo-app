// Capa de presentación · pantalla: dibuja "Mis gustos" (estadísticas calculadas en el teléfono) y le avisa al nav de los eventos.
package com.sorbo.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sorbo.app.R
import com.sorbo.app.domain.usecase.EstadisticasGustos
import com.sorbo.app.domain.usecase.FilaGusto
import com.sorbo.app.ui.components.Banner
import com.sorbo.app.ui.components.EstadoVacio
import com.sorbo.app.ui.components.FilaEstadistica
import com.sorbo.app.ui.components.TipoBanner
import com.sorbo.app.ui.components.frase
import com.sorbo.app.ui.viewmodels.EstadoGustos
import com.sorbo.app.ui.viewmodels.GustosViewModel

/** Mis gustos: cuántas degustaciones faltan para el análisis o, con 5 o más, lo que más y menos disfrutás. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGustos(
    viewModel: GustosViewModel,
    onRegistrar: () -> Unit,
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.destino_gustos)) }) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val actual = estado) {
                EstadoGustos.Cargando -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                EstadoGustos.Vacio -> EstadoVacio(
                    icono = R.drawable.ic_ia,
                    titulo = stringResource(R.string.gustos_vacio_titulo),
                    mensaje = stringResource(R.string.gustos_vacio_mensaje),
                    textoAccion = stringResource(R.string.gustos_registrar_vino),
                    onAccion = onRegistrar,
                )
                is EstadoGustos.PocasDegustaciones -> FaltanDegustaciones(actual, onRegistrar)
                is EstadoGustos.ConEstadisticas -> Estadisticas(actual.estadisticas)
            }
        }
    }
}

@Composable
private fun FaltanDegustaciones(estado: EstadoGustos.PocasDegustaciones, onRegistrar: () -> Unit) {
    val colores = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size(96.dp).background(colores.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(R.drawable.ic_ia), contentDescription = null, modifier = Modifier.size(44.dp), tint = colores.onPrimaryContainer)
        }
        Text(
            stringResource(R.string.gustos_conociendote_titulo),
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            pluralStringResource(R.plurals.gustos_faltan_mensaje, estado.faltan, estado.faltan),
            style = MaterialTheme.typography.bodyMedium,
            color = colores.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        LinearProgressIndicator(
            progress = { estado.registradas.toFloat() / estado.necesarias },
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        )
        Text(
            stringResource(R.string.gustos_progreso, estado.registradas, estado.necesarias),
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            color = colores.onSurfaceVariant,
        )
        Button(onClick = onRegistrar, modifier = Modifier.padding(top = 24.dp)) {
            Text(stringResource(R.string.gustos_registrar_vino))
        }
    }
}

@Composable
private fun Estadisticas(estadisticas: EstadisticasGustos) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            pluralStringResource(R.plurals.gustos_basado_en, estadisticas.totalDegustaciones, estadisticas.totalDegustaciones),
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (estadisticas.loQueMasDisfrutas.isEmpty() && estadisticas.loQueNoConvence.isEmpty()) {
            Text(
                stringResource(R.string.gustos_sin_patron),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        if (estadisticas.loQueMasDisfrutas.isNotEmpty()) {
            SeccionDeFilas(R.string.gustos_lo_que_mas_disfrutas, estadisticas.loQueMasDisfrutas, positiva = true, mostrarOrigen = true)
        }
        if (estadisticas.loQueNoConvence.isNotEmpty()) {
            SeccionDeFilas(R.string.gustos_lo_que_no_te_convence, estadisticas.loQueNoConvence, positiva = false, mostrarOrigen = false)
        }
        Banner(
            tipo = TipoBanner.Info,
            mensaje = stringResource(R.string.gustos_nota_calculo),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun SeccionDeFilas(titulo: Int, filas: List<FilaGusto>, positiva: Boolean, mostrarOrigen: Boolean) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(stringResource(titulo), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (mostrarOrigen) {
                Text(stringResource(R.string.gustos_segun_tu_diario), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        filas.forEach { fila ->
            FilaEstadistica(
                etiqueta = stringResource(fila.categoria.frase()),
                valor = stringResource(R.string.gustos_valor, fila.favorables, fila.total),
                progreso = fila.proporcion,
                positiva = positiva,
            )
        }
    }
}
