// Capa de datos · relación de Room: un vino con todas sus degustaciones, leído en una sola consulta.
package com.sorbo.app.data.local

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Room arma este objeto solo: lee el vino y, con [Relation], busca las degustaciones cuyo `vinoId` coincide.
 * Trae también las degustaciones eliminadas; el mapper las descarta antes de llegar al dominio.
 */
data class VinoConDegustacionesRelacion(
    @Embedded val vino: VinoEntidad,
    @Relation(parentColumn = "id", entityColumn = "vinoId")
    val degustaciones: List<DegustacionEntidad>,
)
