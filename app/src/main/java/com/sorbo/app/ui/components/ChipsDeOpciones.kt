// Capa de presentación · componente reutilizable sin estado (state hoisting): recibe datos y eventos por parámetro.
package com.sorbo.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sorbo.app.R

/** Una lista cerrada de opciones como chips que se acomodan en filas; elige una sola (o ninguna). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> ChipsDeOpciones(
    opciones: List<T>,
    seleccionada: T?,
    etiqueta: (T) -> Int,
    onElegir: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        opciones.forEach { opcion ->
            val elegida = opcion == seleccionada
            FilterChip(
                selected = elegida,
                onClick = { onElegir(opcion) },
                label = { Text(stringResource(etiqueta(opcion))) },
                leadingIcon = if (elegida) {
                    { Icon(painterResource(R.drawable.ic_check), contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else {
                    null
                },
            )
        }
    }
}
