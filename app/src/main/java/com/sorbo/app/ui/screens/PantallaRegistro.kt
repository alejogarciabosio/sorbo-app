// Capa de presentación · pantalla: dibuja el estado que expone el ViewModel y le avisa de los eventos.
package com.sorbo.app.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sorbo.app.R
import com.sorbo.app.domain.usecase.CampoObligatorio
import com.sorbo.app.ui.components.AvisoLectura
import com.sorbo.app.ui.components.AvisoLecturaEtiqueta
import com.sorbo.app.ui.components.Banner
import com.sorbo.app.ui.components.CampoDeTextoSugerible
import com.sorbo.app.ui.components.AccionesDeDegustacion
import com.sorbo.app.ui.components.FilaFoto
import com.sorbo.app.ui.components.HojaMismoVino
import com.sorbo.app.ui.components.SelectorDeFecha
import com.sorbo.app.ui.components.TipoBanner
import com.sorbo.app.ui.components.rememberAccionesDeFoto
import com.sorbo.app.ui.components.PreguntasDeDegustacion
import com.sorbo.app.ui.components.RespuestasDeDegustacion
import com.sorbo.app.ui.viewmodels.CampoSugerible
import com.sorbo.app.ui.viewmodels.CamposRegistro
import com.sorbo.app.ui.viewmodels.EstadoRegistro
import com.sorbo.app.ui.viewmodels.LecturaEtiqueta
import com.sorbo.app.ui.viewmodels.RegistroViewModel

/** Formulario para registrar un vino: solo el nombre y si te gustó son obligatorios, el resto son opciones de un toque. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRegistro(
    viewModel: RegistroViewModel,
    onCerrar: () -> Unit,
    onGuardado: (EstadoRegistro.Guardado) -> Unit,
    onNuevaDireccionParaCamara: () -> Uri,
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val cerrar = {
        viewModel.alCerrar()
        onCerrar()
    }
    BackHandler(onBack = cerrar)
    var eligiendoFecha by rememberSaveable { mutableStateOf(false) }
    val accionesDeFoto = rememberAccionesDeFoto(viewModel::alElegirFoto, onNuevaDireccionParaCamara)

    (estado as? EstadoRegistro.Guardado)?.let { guardado ->
        LaunchedEffect(guardado) { onGuardado(guardado) }
    }
    val campos = when (val actual = estado) {
        is EstadoRegistro.Editando -> actual.campos
        is EstadoRegistro.PreguntandoMismoVino -> actual.campos
        is EstadoRegistro.Guardado -> null
    }
    val faltantes = (estado as? EstadoRegistro.Editando)?.faltantes.orEmpty()

    (estado as? EstadoRegistro.PreguntandoMismoVino)?.let { pregunta ->
        HojaMismoVino(
            parecido = pregunta.parecido,
            onEsEsteVino = viewModel::alConfirmarMismoVino,
            onEsOtroVino = viewModel::alElegirOtroVino,
            onVolverAEditar = viewModel::alVolverAEditar,
        )
    }

    if (eligiendoFecha && campos != null) {
        SelectorDeFecha(
            fecha = campos.fecha,
            hoy = viewModel.hoy,
            onElegir = viewModel::alCambiarFecha,
            onCerrar = { eligiendoFecha = false },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_registrar)) },
                navigationIcon = {
                    IconButton(onClick = cerrar) {
                        Icon(painterResource(R.drawable.ic_cerrar), contentDescription = stringResource(R.string.accion_cerrar))
                    }
                },
            )
        },
        bottomBar = {
            // El botón sube con el teclado para que "Guardar" siempre se pueda tocar.
            Surface(modifier = Modifier.imePadding(), tonalElevation = 3.dp) {
                Button(
                    onClick = viewModel::alGuardar,
                    enabled = campos?.procesandoFoto != true,
                    modifier = Modifier.navigationBarsPadding().fillMaxWidth().padding(16.dp).heightIn(min = 56.dp),
                ) {
                    Icon(painterResource(R.drawable.ic_check), contentDescription = null, modifier = Modifier.size(20.dp))
                    Text(stringResource(R.string.registro_guardar), modifier = Modifier.padding(start = 8.dp))
                }
            }
        },
    ) { padding ->
        if (campos != null) {
            Formulario(campos, faltantes, viewModel, accionesDeFoto.sacarFoto, accionesDeFoto.elegirDeGaleria, { eligiendoFecha = true }, Modifier.padding(padding))
        }
    }
}

/** Qué aviso corresponde mostrar según cómo va la lectura de la etiqueta. */
private fun avisoDeLectura(campos: CamposRegistro): AvisoLectura? = when {
    campos.lectura is LecturaEtiqueta.Leyendo -> AvisoLectura.Leyendo
    campos.lectura is LecturaEtiqueta.SinDatos -> AvisoLectura.SinDatos
    campos.lectura is LecturaEtiqueta.Fallo -> AvisoLectura.Fallo
    campos.sugeridos.isNotEmpty() -> AvisoLectura.Sugerido
    else -> null
}

@Composable
private fun Formulario(
    campos: CamposRegistro,
    faltantes: Set<CampoObligatorio>,
    viewModel: RegistroViewModel,
    onSacarFoto: () -> Unit,
    onElegirDeGaleria: () -> Unit,
    onElegirFecha: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        FilaFoto(
            fotoPath = campos.fotoPath,
            procesando = campos.procesandoFoto,
            onSacarFoto = onSacarFoto,
            onElegirDeGaleria = onElegirDeGaleria,
            onQuitar = viewModel::alQuitarFoto,
        )
        if (campos.errorFoto) {
            Banner(tipo = TipoBanner.Error, mensaje = stringResource(R.string.foto_error))
        }

        avisoDeLectura(campos)?.let { aviso ->
            AvisoLecturaEtiqueta(aviso = aviso, onReintentar = viewModel::alReintentarLectura)
        }

        val opcionesDeTexto = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
        CampoDeTextoSugerible(
            valor = campos.nombre,
            onCambio = viewModel::alCambiarNombre,
            etiqueta = stringResource(R.string.registro_nombre),
            sugerido = CampoSugerible.NOMBRE in campos.sugeridos,
            keyboardOptions = opcionesDeTexto,
            modifier = Modifier.fillMaxWidth(),
            mensajeDeError = if (CampoObligatorio.NOMBRE in faltantes) stringResource(R.string.registro_falta_nombre) else null,
        )
        CampoDeTextoSugerible(
            valor = campos.bodega,
            onCambio = viewModel::alCambiarBodega,
            etiqueta = stringResource(R.string.registro_bodega),
            sugerido = CampoSugerible.BODEGA in campos.sugeridos,
            keyboardOptions = opcionesDeTexto,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CampoDeTextoSugerible(
                valor = campos.cepa,
                onCambio = viewModel::alCambiarCepa,
                etiqueta = stringResource(R.string.registro_cepa),
                sugerido = CampoSugerible.CEPA in campos.sugeridos,
                keyboardOptions = opcionesDeTexto,
                modifier = Modifier.weight(1f),
            )
            CampoDeTextoSugerible(
                valor = campos.anio,
                onCambio = viewModel::alCambiarAnio,
                etiqueta = stringResource(R.string.registro_anio),
                sugerido = CampoSugerible.ANIO in campos.sugeridos,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                modifier = Modifier.weight(1f),
            )
        }

        PreguntasDeDegustacion(
            respuestas = RespuestasDeDegustacion(
                meGusto = campos.meGusto,
                dulzor = campos.dulzor,
                comida = campos.comida,
                compania = campos.compania,
                ocasion = campos.ocasion,
                fecha = campos.fecha,
                nota = campos.nota,
            ),
            hoy = viewModel.hoy,
            faltaOpinion = CampoObligatorio.OPINION in faltantes,
            acciones = AccionesDeDegustacion(
                onMeGusto = viewModel::alElegirMeGusto,
                onDulzor = viewModel::alElegirDulzor,
                onComida = viewModel::alElegirComida,
                onCompania = viewModel::alElegirCompania,
                onOcasion = viewModel::alElegirOcasion,
                onNota = viewModel::alCambiarNota,
                onElegirFecha = onElegirFecha,
            ),
        )
    }
}
