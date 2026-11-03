package com.sorbo.app.domain.repository

import com.sorbo.app.domain.model.TextoEtiqueta

class LecturaEtiquetaException(mensaje: String, causa: Throwable? = null) : Exception(mensaje, causa)

/** Lee el texto de la foto de una etiqueta en el teléfono, sin conexión. La implementación usa ML Kit. */
interface LectorEtiqueta {
    /** @throws LecturaEtiquetaException si no se pudo leer la imagen. */
    suspend fun leer(fotoPath: String): TextoEtiqueta
}
