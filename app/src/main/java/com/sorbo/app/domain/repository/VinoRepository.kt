// Capa de dominio · contrato: interfaz que la capa de datos implementa y que usan los casos de uso y los ViewModels.
package com.sorbo.app.domain.repository

import com.sorbo.app.domain.model.CambiosPendientes
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

    // --- Respaldo en la nube (sincronización) ---

    /** Todo lo que está PENDIENTE de subir, incluidos los vinos y degustaciones eliminados. */
    suspend fun listarPendientes(): CambiosPendientes

    /**
     * Marca como SINCRONIZADO lo que se subió. Cada registro se marca **solo si su `actualizadoEn` sigue
     * siendo el de [subidos]**: si el usuario lo cambió durante la subida, queda PENDIENTE para la próxima vuelta.
     * De los vinos guarda también `fotoUrl`, para no volver a subir la misma foto.
     */
    suspend fun marcarSincronizado(subidos: CambiosPendientes)

    /**
     * Asigna [usuarioId] a los vinos del teléfono que todavía no tienen dueño (los cargados sin cuenta),
     * y los deja PENDIENTE para que se respalden. Los vinos de otro usuario no se tocan.
     */
    suspend fun asignarUsuario(usuarioId: String)

    /**
     * Guarda lo que se trajo de la nube (restauración) **sin duplicar**: si la misma `id` ya está en el teléfono,
     * gana la versión con `actualizadoEn` más reciente. Lo que se guarda queda SINCRONIZADO.
     */
    suspend fun guardarRestaurados(cambios: CambiosPendientes)

    /** Cuántos vinos y degustaciones faltan respaldar. Sirve para mostrar "Respaldo al día" en Ajustes. */
    fun observarCantidadPendientes(): Flow<Int>
}
