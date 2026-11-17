// Capa de datos · entidad de Room: fila de la tabla "vinos". Nunca llega a la UI (se convierte con un mapper).
package com.sorbo.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sorbo.app.domain.model.EstadoSync
import java.time.Instant

/**
 * Un vino guardado en el teléfono. Además de los datos del modelo guarda [textoBusqueda]
 * (nombre, bodega, cepa y año ya normalizados) para poder buscar con una consulta.
 */
@Entity(tableName = "vinos")
data class VinoEntidad(
    @PrimaryKey val id: String,
    val usuarioId: String?,
    val nombre: String,
    val bodega: String?,
    val cepa: String?,
    val anio: Int?,
    val fotoPath: String?,
    val fotoUrl: String?,
    val textoBusqueda: String,
    val creadoEn: Instant,
    val actualizadoEn: Instant,
    val estadoSync: EstadoSync,
    val eliminado: Boolean,
)
