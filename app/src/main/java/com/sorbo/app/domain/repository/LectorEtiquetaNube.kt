// Capa de dominio · contrato: interfaz que la capa de datos implementa y que usan los casos de uso y los ViewModels.
package com.sorbo.app.domain.repository

import com.sorbo.app.domain.model.LecturaNube

/**
 * Lee la etiqueta con un modelo de visión en la nube (mejora opcional de RF03). Necesita conexión y sesión.
 * Es opcional: cualquier falla se devuelve como [Result.failure] y la app se queda con la lectura del teléfono.
 */
interface LectorEtiquetaNube {
    suspend fun leer(fotoPath: String): Result<LecturaNube>
}
