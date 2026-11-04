// Capa de presentación · componente reutilizable sin estado (state hoisting): campo de texto que marca lo sugerido por la lectura.
package com.sorbo.app.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.sorbo.app.R

/**
 * Campo del formulario. Si [sugerido], lo completó la lectura de la etiqueta: lleva el ✦ y el color tertiary
 * que usa la app para todo lo que sugiere, y sigue siendo editable.
 */
@Composable
fun CampoDeTextoSugerible(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    sugerido: Boolean,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier,
    mensajeDeError: String? = null,
) {
    val colorSugerido = MaterialTheme.colorScheme.tertiary
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        modifier = modifier,
        label = { Text(etiqueta) },
        isError = mensajeDeError != null,
        supportingText = mensajeDeError?.let { { Text(it) } },
        trailingIcon = if (sugerido) {
            {
                Icon(
                    painterResource(R.drawable.ic_ia),
                    contentDescription = stringResource(R.string.lectura_campo_sugerido),
                    tint = colorSugerido,
                )
            }
        } else {
            null
        },
        colors = if (sugerido) {
            OutlinedTextFieldDefaults.colors(unfocusedBorderColor = colorSugerido)
        } else {
            OutlinedTextFieldDefaults.colors()
        },
        singleLine = true,
        keyboardOptions = keyboardOptions,
    )
}
