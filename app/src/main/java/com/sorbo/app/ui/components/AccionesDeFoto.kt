// Capa de presentación · componente con estado propio de UI: cámara, galería y permiso de cámara para elegir la foto de una etiqueta.
package com.sorbo.app.ui.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/** Las dos formas de conseguir una foto. Quien las usa decide dónde poner los botones que las disparan. */
class AccionesDeFoto(val sacarFoto: () -> Unit, val elegirDeGaleria: () -> Unit)

/** Las hojas que acompañan a la cámara: primero se explica el permiso y, si se rechaza, se ofrecen alternativas. */
private enum class HojaDeFoto { PERMISO_CAMARA, CAMARA_DENEGADA }

/**
 * Arma "Sacar foto" y "Galería" con todo lo que implican: pedir el permiso de cámara con su explicación (C1), ofrecer
 * alternativas si se rechaza (C7, RNF09) y entregar la dirección de la foto elegida. Lo usan Registrar y Editar.
 */
@Composable
fun rememberAccionesDeFoto(
    onFotoElegida: (String) -> Unit,
    onNuevaDireccionParaCamara: () -> Uri,
): AccionesDeFoto {
    val contexto = LocalContext.current
    var hojaDeFoto by rememberSaveable { mutableStateOf<HojaDeFoto?>(null) }
    var direccionDeCamara by rememberSaveable { mutableStateOf<Uri?>(null) }

    val sacarFoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { sacada ->
        if (sacada) direccionDeCamara?.let { onFotoElegida(it.toString()) }
    }
    val elegirDeGaleria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { elegida ->
        elegida?.let { onFotoElegida(it.toString()) }
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

    return AccionesDeFoto(
        sacarFoto = {
            val tienePermiso = ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            if (tienePermiso) abrirCamara() else hojaDeFoto = HojaDeFoto.PERMISO_CAMARA
        },
        elegirDeGaleria = abrirGaleria,
    )
}
