// Capa de datos · repositorio en memoria: implementación provisoria hasta que esté Room.
package com.sorbo.app.data.memoria

import com.sorbo.app.domain.model.Compania
import com.sorbo.app.domain.model.Comida
import com.sorbo.app.domain.model.Degustacion
import com.sorbo.app.domain.model.Dulzor
import com.sorbo.app.domain.model.EstadoSync
import com.sorbo.app.domain.model.Ocasion
import com.sorbo.app.domain.model.Vino
import java.time.Clock
import java.time.LocalDate

/** Vinos de ejemplo (los de los mockups) para desarrollar las pantallas antes de tener Room. */
object DatosDeEjemplo {

    fun vinos(reloj: Clock): List<Pair<Vino, List<Degustacion>>> {
        val ahora = reloj.instant()
        val hoy = LocalDate.now(reloj)

        fun vino(id: String, nombre: String, bodega: String?, cepa: String?, anio: Int?) =
            Vino(id = id, nombre = nombre, creadoEn = ahora, actualizadoEn = ahora, bodega = bodega, cepa = cepa, anio = anio, estadoSync = EstadoSync.SINCRONIZADO)

        fun degustacion(id: String, vinoId: String, diasAtras: Long, meGusto: Boolean, dulzor: Dulzor?, comida: Comida?, compania: Compania?, ocasion: Ocasion?, nota: String? = null) =
            Degustacion(id, vinoId, hoy.minusDays(diasAtras), meGusto, ahora, dulzor, comida, compania, ocasion, nota, EstadoSync.SINCRONIZADO)

        return listOf(
            vino("vino-1", "Rutini Malbec 2021", "Rutini Wines", "Malbec", 2021) to listOf(
                degustacion("deg-1", "vino-1", 3, true, Dulzor.SECO, Comida.CARNE, Compania.AMIGOS, Ocasion.ASADO, "Ideal para el asado del domingo"),
                degustacion("deg-2", "vino-1", 98, true, Dulzor.SECO, Comida.PASTA, Compania.PAREJA, Ocasion.CENA),
            ),
            vino("vino-2", "El Esteco Torrontés", "El Esteco", "Torrontés", null) to listOf(
                degustacion("deg-3", "vino-2", 14, true, Dulzor.SECO, Comida.PESCADO, Compania.FAMILIA, Ocasion.ALMUERZO),
            ),
            vino("vino-3", "Alamos Cabernet", "Catena", "Cabernet Sauvignon", null) to listOf(
                degustacion("deg-4", "vino-3", 45, false, Dulzor.SECO, Comida.PIZZA, Compania.TRABAJO, Ocasion.PREVIA),
            ),
            vino("vino-4", "Santa Julia Dulce Natural", "Familia Zuccardi", "Torrontés", null) to listOf(
                degustacion("deg-5", "vino-4", 7, true, Dulzor.DULCE, Comida.POSTRE, Compania.PAREJA, Ocasion.CELEBRACION),
            ),
        )
    }
}
