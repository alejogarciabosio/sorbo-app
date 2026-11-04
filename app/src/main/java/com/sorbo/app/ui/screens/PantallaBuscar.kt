// Capa de presentación · pantalla: dibuja el estado que expone el ViewModel y le avisa de los eventos.
package com.sorbo.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sorbo.app.R
import com.sorbo.app.domain.usecase.ResultadoBusqueda
import com.sorbo.app.ui.components.Banner
import com.sorbo.app.ui.components.EstadoVacio
import com.sorbo.app.ui.components.ListaDeVinos
import com.sorbo.app.ui.components.TipoBanner
import com.sorbo.app.ui.viewmodels.BuscarViewModel
import com.sorbo.app.ui.viewmodels.EstadoBusqueda

/** Búsqueda en el diario: funciona sin conexión y avisa con claridad cuando el vino no está cargado. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaBuscar(
    viewModel: BuscarViewModel,
    onAtras: () -> Unit,
    onAbrirVino: (String) -> Unit,
    onRegistrar: (String) -> Unit,
) {
    val consulta by viewModel.consulta.collectAsStateWithLifecycle()
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val foco = remember { FocusRequester() }
    LaunchedEffect(Unit) { foco.requestFocus() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_buscar)) },
                navigationIcon = {
                    IconButton(onClick = onAtras) {
                        Icon(painterResource(R.drawable.ic_atras), contentDescription = stringResource(R.string.accion_volver))
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = consulta,
                onValueChange = viewModel::alCambiarConsulta,
                modifier = Modifier.fillMaxWidth().padding(16.dp).focusRequester(foco),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.buscar_hint)) },
                leadingIcon = { Icon(painterResource(R.drawable.ic_buscar), contentDescription = null) },
                trailingIcon = {
                    if (consulta.isNotEmpty()) {
                        IconButton(onClick = { viewModel.alCambiarConsulta("") }) {
                            Icon(painterResource(R.drawable.ic_cerrar), contentDescription = stringResource(R.string.buscar_limpiar))
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            )
            when (val actual = estado) {
                EstadoBusqueda.SinConsulta -> EstadoVacio(
                    icono = R.drawable.ic_buscar,
                    titulo = stringResource(R.string.buscar_inicial_titulo),
                    mensaje = stringResource(R.string.buscar_inicial_mensaje),
                )
                is EstadoBusqueda.ConResultado -> when (val resultado = actual.resultado) {
                    is ResultadoBusqueda.Encontrados -> ListaDeVinos(resultado.vinos, onAbrirVino)
                    is ResultadoBusqueda.SoloOtraAnada -> Column {
                        Banner(
                            tipo = TipoBanner.Info,
                            mensaje = stringResource(R.string.buscar_otra_anada, resultado.anioBuscado),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                        ListaDeVinos(resultado.vinos, onAbrirVino)
                    }
                    is ResultadoBusqueda.NoEncontrado -> EstadoVacio(
                        icono = R.drawable.ic_vino,
                        titulo = stringResource(R.string.buscar_sin_resultados_titulo),
                        mensaje = stringResource(R.string.buscar_sin_resultados_mensaje),
                        textoAccion = stringResource(R.string.accion_registrar),
                        onAccion = { onRegistrar(consulta) },
                    )
                }
            }
        }
    }
}
