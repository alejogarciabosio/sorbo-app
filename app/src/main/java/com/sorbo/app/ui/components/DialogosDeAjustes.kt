// Capa de presentación · componente reutilizable sin estado (state hoisting): los cuadros de confirmación de cerrar sesión y eliminar cuenta.
package com.sorbo.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sorbo.app.R
import com.sorbo.app.ui.viewmodels.DialogoAjustes
import com.sorbo.app.ui.viewmodels.ErrorDeCuenta

/** Muestra el cuadro que corresponda a [dialogo]: G5 (cerrar sesión con pendientes), G6 (eliminar cuenta), el progreso o el fallo. */
@Composable
fun DialogosDeAjustes(
    dialogo: DialogoAjustes,
    alConfirmarCerrarSesion: () -> Unit,
    alConfirmarEliminarCuenta: () -> Unit,
    alCerrar: () -> Unit,
) {
    when (dialogo) {
        DialogoAjustes.Ninguno -> Unit
        is DialogoAjustes.ConfirmarCerrarSesion -> AlertDialog(
            onDismissRequest = alCerrar,
            title = { Text(stringResource(R.string.ajustes_cerrar_titulo)) },
            text = { Text(pluralStringResource(R.plurals.ajustes_cerrar_mensaje, dialogo.pendientes, dialogo.pendientes)) },
            confirmButton = { TextButton(onClick = alConfirmarCerrarSesion) { Text(stringResource(R.string.ajustes_cerrar_sesion)) } },
            dismissButton = { TextButton(onClick = alCerrar) { Text(stringResource(R.string.accion_cancelar)) } },
        )
        DialogoAjustes.ConfirmarEliminarCuenta -> AlertDialog(
            onDismissRequest = alCerrar,
            title = { Text(stringResource(R.string.ajustes_eliminar_titulo)) },
            text = { Text(stringResource(R.string.ajustes_eliminar_mensaje)) },
            confirmButton = {
                TextButton(onClick = alConfirmarEliminarCuenta) {
                    Text(stringResource(R.string.ajustes_eliminar_cuenta), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = alCerrar) { Text(stringResource(R.string.accion_cancelar)) } },
        )
        DialogoAjustes.Eliminando -> AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            text = {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                    Text(stringResource(R.string.ajustes_eliminando))
                }
            },
        )
        is DialogoAjustes.FalloAlEliminar -> AlertDialog(
            onDismissRequest = alCerrar,
            title = { Text(stringResource(R.string.ajustes_eliminar_fallo)) },
            text = { Text(ErrorDeCuenta.Servidor(dialogo.motivo).mensaje()) },
            confirmButton = { TextButton(onClick = alCerrar) { Text(stringResource(R.string.ajustes_entendido)) } },
        )
    }
}
