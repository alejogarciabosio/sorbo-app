package com.sorbo.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.sorbo.app.R

/** Botón grande de "¿Te gustó?": uno de los dos campos obligatorios del registro. */
@Composable
fun OpcionGrande(
    meGusto: Boolean,
    seleccionada: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    conError: Boolean = false,
) {
    val colores = MaterialTheme.colorScheme
    val fondo = if (seleccionada) colores.primary else colores.surface
    val contenido = if (seleccionada) colores.onPrimary else colores.onSurfaceVariant
    val borde = when {
        seleccionada -> null
        conError -> BorderStroke(1.dp, colores.error)
        else -> BorderStroke(1.dp, colores.outlineVariant)
    }
    Surface(
        modifier = modifier
            .height(76.dp)
            .selectable(selected = seleccionada, role = Role.RadioButton, onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = fondo,
        contentColor = contenido,
        border = borde,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
        ) {
            Icon(
                painterResource(if (meGusto) R.drawable.ic_me_gusto else R.drawable.ic_no_me_gusto),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
            )
            Text(
                stringResource(if (meGusto) R.string.veredicto_me_gusto else R.string.veredicto_no_me_gusto),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
