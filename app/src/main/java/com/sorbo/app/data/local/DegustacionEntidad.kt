// Capa de datos · entidad de Room: fila de la tabla "degustaciones". Nunca llega a la UI (se convierte con un mapper).
package com.sorbo.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sorbo.app.domain.model.Comida
import com.sorbo.app.domain.model.Compania
import com.sorbo.app.domain.model.Dulzor
import com.sorbo.app.domain.model.EstadoSync
import com.sorbo.app.domain.model.Ocasion
import java.time.Instant
import java.time.LocalDate

/**
 * Una vez que se probó un vino. La clave foránea asegura que no exista una degustación sin su vino;
 * el índice en [vinoId] hace rápida la consulta de "las degustaciones de este vino".
 */
@Entity(
    tableName = "degustaciones",
    foreignKeys = [
        ForeignKey(entity = VinoEntidad::class, parentColumns = ["id"], childColumns = ["vinoId"]),
    ],
    indices = [Index("vinoId")],
)
data class DegustacionEntidad(
    @PrimaryKey val id: String,
    val vinoId: String,
    val fecha: LocalDate,
    val meGusto: Boolean,
    val dulzor: Dulzor?,
    val comida: Comida?,
    val compania: Compania?,
    val ocasion: Ocasion?,
    val nota: String?,
    val actualizadoEn: Instant,
    val estadoSync: EstadoSync,
    val eliminado: Boolean,
)
