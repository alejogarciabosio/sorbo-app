// Capa de presentación · componente reutilizable sin estado: campo de texto de los formularios de cuenta (email o contraseña).
package com.sorbo.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Un campo del formulario de cuenta. Si [esClave], oculta lo escrito. [textoDeApoyo] se muestra debajo y, si hay
 * [mensajeDeError], lo reemplaza y pinta el campo como erróneo.
 */
@Composable
fun CampoDeCuenta(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    @DrawableRes icono: Int,
    modifier: Modifier = Modifier,
    esClave: Boolean = false,
    enabled: Boolean = true,
    hayError: Boolean = false,
    mensajeDeError: String? = null,
    textoDeApoyo: (@Composable () -> Unit)? = null,
    imeAction: ImeAction = ImeAction.Next,
    onListo: () -> Unit = {},
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = true,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(painterResource(icono), contentDescription = null) },
        isError = hayError || mensajeDeError != null,
        supportingText = when {
            mensajeDeError != null -> { { Text(mensajeDeError) } }
            else -> textoDeApoyo
        },
        visualTransformation = if (esClave) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (esClave) KeyboardType.Password else KeyboardType.Email,
            imeAction = imeAction,
        ),
        keyboardActions = KeyboardActions(onDone = { onListo() }, onGo = { onListo() }),
    )
}
