// Capa de datos · archivo temporal donde la cámara del teléfono deja la foto antes de que la achiquemos y guardemos.
package com.sorbo.app.data.foto

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

class ArchivoParaCamara(private val context: Context) {

    /** Dirección (FileProvider) de un archivo vacío en la caché donde la cámara escribe la foto. */
    fun nuevaDireccion(): Uri {
        val carpeta = File(context.cacheDir, "camara").apply { mkdirs() }
        val archivo = File(carpeta, "etiqueta.jpg").apply { delete() }
        return FileProvider.getUriForFile(context, AlmacenFotosLocal.autoridadDeArchivos(context), archivo)
    }
}
