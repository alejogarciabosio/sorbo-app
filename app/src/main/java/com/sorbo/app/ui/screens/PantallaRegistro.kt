// Capa de presentación · pantalla: dibuja el estado que expone el ViewModel y le avisa de los eventos.
package com.sorbo.app.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sorbo.app.R
import com.sorbo.app.domain.model.Compania
import com.sorbo.app.domain.model.Comida
import com.sorbo.app.domain.model.Dulzor
import com.sorbo.app.domain.model.Ocasion
import com.sorbo.app.domain.usecase.CampoObligatorio
import com.sorbo.app.ui.components.Banner
import com.sorbo.app.ui.components.ChipsDeOpciones
import com.sorbo.app.ui.components.FilaFoto
import com.sorbo.app.ui.components.HojaCamaraDenegada
import com.sorbo.app.ui.components.HojaMismoVino
import com.sorbo.app.ui.components.HojaPermisoCamara
import com.sorbo.app.ui.components.TipoBanner
import com.sorbo.app.ui.components.OpcionGrande
import com.sorbo.app.ui.components.etiqueta
import com.sorbo.app.ui.viewmodels.CamposRegistro
import com.sorbo.app.ui.viewmodels.EstadoRegistro
import com.sorbo.app.ui.viewmodels.RegistroViewModel

/** Las hojas que acompañan a la cámara: primero se explica el permiso y, si se rechaza, se ofrecen alternativas. */
private enum class HojaDeFoto { PERMISO_CAMARA, CAMARA_DENEGADA }

/** Formulario para registrar un vino: solo el nombre y si te gustó son obligatorios, el resto son opciones de un toque. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRegistro(
    viewModel: RegistroViewModel,
    onCerrar: () -> Unit,
    onGuardado: (vinoId: String) -> Unit,
    onNuevaDireccionParaCamara: () -> Uri,
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    var hojaDeFoto by rememberSaveable { mutableStateOf<HojaDeFoto?>(null) }
    var direccionDeCamara by rememberSaveable { mutableStateOf<Uri?>(null) }

    val sacarFoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { sacada ->
        if (sacada) direccionDeCamara?.let { viewModel.alElegirFoto(it.toString()) }
    }
    val elegirDeGaleria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { elegida ->
        elegida?.let { viewModel.alElegirFoto(it.toString()) }
    }
    val abrirCamara = {
        val direccion = onNuevaDireccionParaCamara()
        direccionDeCamara = direccion
        sacarFoto.launch(direccion)
    }
    val abrirGaleria = {
        elegirDeGaleria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
    val pedirPermisoDeCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) abrirCamara() else hojaDeFoto = HojaDeFoto.CAMARA_DENEGADA
    }
    val alSacarFoto = {
        val tienePermiso = ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (tienePermiso) abrirCamara() else hojaDeFoto = HojaDeFoto.PERMISO_CAMARA
    }

    (estado as? EstadoRegistro.Guardado)?.let { guardado ->
        LaunchedEffect(guardado) { onGuardado(guardado.vinoId) }
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

    when (hojaDeFoto) {
        HojaDeFoto.PERMISO_CAMARA -> HojaPermisoCamara(
            onContinuar = {
                hojaDeFoto = null
                pedirPermisoDeCamara.launch(Manifest.permission.CAMERA)
            },
            onAhoraNo = { hojaDeFoto = null },
        )
        HojaDeFoto.CAMARA_DENEGADA -> HojaCamaraDenegada(
            onElegirDeGaleria = {
                hojaDeFoto = null
                abrirGaleria()
            },
            onContinuarSinFoto = { hojaDeFoto = null },
            onAbrirAjustes = {
                hojaDeFoto = null
                val ajustes = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", contexto.packageName, null))
                contexto.startActivity(ajustes)
            },
        )
        null -> Unit
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_registrar)) },
                navigationIcon = {
                    IconButton(onClick = onCerrar) {
                        Icon(painterResource(R.drawable.ic_cerrar), contentDescription = stringResource(R.string.accion_cerrar))
                    }
                },
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
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
            Formulario(campos, faltantes, viewModel, alSacarFoto, abrirGaleria, Modifier.padding(padding))
        }
    }
}

@Composable
private fun Formulario(
    campos: CamposRegistro,
    faltantes: Set<CampoObligatorio>,
    viewModel: RegistroViewModel,
    onSacarFoto: () -> Unit,
    onElegirDeGaleria: () -> Unit,
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

        val faltaNombre = CampoObligatorio.NOMBRE in faltantes
        OutlinedTextField(
            value = campos.nombre,
            onValueChange = viewModel::alCambiarNombre,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.registro_nombre)) },
            isError = faltaNombre,
            supportingText = if (faltaNombre) {
                { Text(stringResource(R.string.registro_falta_nombre)) }
            } else {
                null
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
        )
        OutlinedTextField(
            value = campos.bodega,
            onValueChange = viewModel::alCambiarBodega,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.registro_bodega)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = campos.cepa,
                onValueChange = viewModel::alCambiarCepa,
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(R.string.registro_cepa)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            )
            OutlinedTextField(
                value = campos.anio,
                onValueChange = viewModel::alCambiarAnio,
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(R.string.registro_anio)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            )
        }

        Seccion(R.string.registro_te_gusto) {
            val faltaOpinion = CampoObligatorio.OPINION in faltantes
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OpcionGrande(
                    meGusto = true,
                    seleccionada = campos.meGusto == true,
                    onClick = { viewModel.alElegirMeGusto(true) },
                    modifier = Modifier.weight(1f),
                    conError = faltaOpinion,
                )
                OpcionGrande(
                    meGusto = false,
                    seleccionada = campos.meGusto == false,
                    onClick = { viewModel.alElegirMeGusto(false) },
                    modifier = Modifier.weight(1f),
                    conError = faltaOpinion,
                )
            }
            if (faltaOpinion) {
                Text(
                    stringResource(R.string.registro_falta_opinion),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        Seccion(R.string.registro_como_lo_sentiste) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                Dulzor.entries.forEachIndexed { posicion, dulzor ->
                    SegmentedButton(
                        selected = campos.dulzor == dulzor,
                        onClick = { viewModel.alElegirDulzor(dulzor) },
                        shape = SegmentedButtonDefaults.itemShape(posicion, Dulzor.entries.size),
                    ) {
                        Text(stringResource(dulzor.etiqueta()))
                    }
                }
            }
        }

        Seccion(R.string.registro_con_que_lo_comiste) {
            ChipsDeOpciones(Comida.entries, campos.comida, Comida::etiqueta, viewModel::alElegirComida)
        }
        Seccion(R.string.registro_con_quien) {
            ChipsDeOpciones(Compania.entries, campos.compania, Compania::etiqueta, viewModel::alElegirCompania)
        }
        Seccion(R.string.registro_ocasion) {
            ChipsDeOpciones(Ocasion.entries, campos.ocasion, Ocasion::etiqueta, viewModel::alElegirOcasion)
        }

        OutlinedTextField(
            value = campos.nota,
            onValueChange = viewModel::alCambiarNota,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.registro_nota)) },
            minLines = 3,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        )
    }
}

/** Una pregunta del formulario con su título y las opciones debajo. */
@Composable
private fun Seccion(titulo: Int, contenido: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(titulo), style = MaterialTheme.typography.titleMedium)
        contenido()
    }
}
