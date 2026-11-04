// Capa de dominio · contrato: interfaz que la capa de datos implementa y que usan los casos de uso y los ViewModels.
package com.sorbo.app.domain.repository

import com.sorbo.app.domain.model.Degustacion
import com.sorbo.app.domain.model.Vino
import com.sorbo.app.domain.model.VinoConDegustaciones
import kotlinx.coroutines.flow.Flow

/**
 * Único punto de acceso a los vinos y sus degustaciones. La UI siempre lee de acá (Room es la fuente de verdad)
 * y nunca espera a la red. Toda escritura deja el registro como PENDIENTE de respaldo.
 */
interface VinoRepository {

    /** El diario sin los vinos eliminados, con el probado más recientemente primero. */
    fun observarDiario(): Flow<List<VinoConDegustaciones>>

    /** Un vino con sus degustaciones, o null si no existe o está eliminado. */
    fun observarVino(id: String): Flow<VinoConDegustaciones?>

    /** Todos los vinos no eliminados. Se usa para decidir si un vino nuevo ya existe. */
    suspend fun listarVinos(): List<Vino>

    /**
     * Vinos cuyo texto de búsqueda contiene todas las palabras de [consultaNormalizada]
     * (ya sin tildes ni mayúsculas, ver `normalizar`).
     */
    suspend fun buscar(consultaNormalizada: String): List<VinoConDegustaciones>

    suspend fun guardarVinoNuevo(vino: Vino, degustacion: Degustacion)

    suspend fun agregarDegustacion(degustacion: Degustacion)

    suspend fun actualizarVino(vino: Vino)

    /** Borrado lógico de una sola degustación (el vino sigue en el diario). Sirve para deshacer "Sumamos esta vez a…". */
    suspend fun eliminarDegustacion(id: String)

    /** Borrado lógico del vino y de sus degustaciones. */
    suspend fun eliminarVino(id: String)

    /** Deshace [eliminarVino]. */
    suspend fun restaurarVino(id: String)
}
