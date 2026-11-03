// Capa de presentación · datos de un vino tal como se muestran en las filas (iniciales, tono de la miniatura y detalle).
package com.sorbo.app.ui.components

import com.sorbo.app.domain.model.Vino

/** Bodega, cepa y año, separados por puntos. */
internal fun Vino.detalleEnLista(): String = listOfNotNull(bodega, cepa, anio?.toString()).joinToString(" · ")

/** Las iniciales de las dos primeras palabras del nombre, para la miniatura sin foto. */
internal fun Vino.inicialesDeEtiqueta(): String =
    nombre.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1).uppercase() }

/** El mismo vino siempre tiene el mismo color de miniatura. */
internal fun Vino.tonoDeEtiqueta(): TonoEtiqueta =
    TonoEtiqueta.entries[Math.floorMod(nombreNormalizado.hashCode(), TonoEtiqueta.entries.size)]
