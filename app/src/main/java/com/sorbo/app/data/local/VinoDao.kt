// Capa de datos · DAO de Room: las consultas SQL sobre las tablas "vinos" y "degustaciones".
package com.sorbo.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.sorbo.app.domain.model.EstadoSync
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Room genera el código de esta interfaz. Las funciones que devuelven [Flow] vuelven a emitir solas
 * cada vez que cambia la tabla; las `suspend` corren fuera del hilo principal.
 * Todos los valores entran como parámetros (`:nombre`), nunca concatenando texto en el SQL.
 */
@Dao
interface VinoDao {

    @Query("SELECT * FROM vinos WHERE eliminado = 0")
    fun observarVinos(): Flow<List<VinoEntidad>>

    @Query("SELECT * FROM degustaciones WHERE eliminado = 0")
    fun observarDegustaciones(): Flow<List<DegustacionEntidad>>

    @Query("SELECT * FROM vinos WHERE eliminado = 0")
    suspend fun listarVinos(): List<VinoEntidad>

    @Query("SELECT * FROM degustaciones WHERE eliminado = 0")
    suspend fun listarDegustaciones(): List<DegustacionEntidad>

    @Query("SELECT EXISTS(SELECT 1 FROM vinos WHERE id = :id AND eliminado = 0)")
    suspend fun existeVino(id: String): Boolean

    @Insert
    suspend fun insertarVino(vino: VinoEntidad)

    @Insert
    suspend fun insertarDegustacion(degustacion: DegustacionEntidad)

    /** Guarda el vino y su primera degustación juntos: o se guardan los dos, o ninguno. */
    @Transaction
    suspend fun insertarVinoConDegustacion(vino: VinoEntidad, degustacion: DegustacionEntidad) {
        insertarVino(vino)
        insertarDegustacion(degustacion)
    }

    @Update
    suspend fun actualizarVino(vino: VinoEntidad)

    @Query(
        "UPDATE degustaciones SET eliminado = 1, actualizadoEn = :ahora, estadoSync = :estado WHERE id = :id",
    )
    suspend fun eliminarDegustacion(id: String, ahora: Instant, estado: EstadoSync = EstadoSync.PENDIENTE)

    @Query("UPDATE vinos SET eliminado = :eliminado, actualizadoEn = :ahora, estadoSync = :estado WHERE id = :id")
    suspend fun marcarVino(id: String, eliminado: Boolean, ahora: Instant, estado: EstadoSync)

    @Query(
        "UPDATE degustaciones SET eliminado = :eliminado, actualizadoEn = :ahora, estadoSync = :estado WHERE vinoId = :vinoId",
    )
    suspend fun marcarDegustacionesDe(vinoId: String, eliminado: Boolean, ahora: Instant, estado: EstadoSync)

    /** Borrado lógico (o su deshacer) del vino y de todas sus degustaciones, en una sola transacción. */
    @Transaction
    suspend fun cambiarEliminado(id: String, eliminado: Boolean, ahora: Instant) {
        marcarVino(id, eliminado, ahora, EstadoSync.PENDIENTE)
        marcarDegustacionesDe(id, eliminado, ahora, EstadoSync.PENDIENTE)
    }
}
