// Capa de presentación · componente reutilizable sin estado (state hoisting): recibe datos y eventos por parámetro.
package com.sorbo.app.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sorbo.app.R
import com.sorbo.app.domain.model.EstadoSync
import com.sorbo.app.domain.model.VinoConDegustaciones
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** Lista de vinos con su última degustación. La usan el Diario y la Búsqueda. */
@Composable
fun ListaDeVinos(vinos: List<VinoConDegustaciones>, onAbrirVino: (String) -> Unit) {
    val formato = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())
    // Deja lugar abajo para que el botón "Registrar" no tape el último vino.
    LazyColumn(contentPadding = PaddingValues(bottom = 88.dp)) {
        items(vinos, key = { it.vino.id }) { item ->
            val vino = item.vino
            ItemVino(
                nombre = vino.nombre,
                detalle = vino.detalleEnLista(),
                cuando = item.ultimaFecha?.let { stringResource(R.string.diario_probado, it.format(formato)) }.orEmpty(),
                meGusto = item.gustoEnGeneral,
                iniciales = vino.inicialesDeEtiqueta(),
                tono = vino.tonoDeEtiqueta(),
                onClick = { onAbrirVino(vino.id) },
                pendienteRespaldo = vino.estadoSync == EstadoSync.PENDIENTE,
            )
        }
    }
}
