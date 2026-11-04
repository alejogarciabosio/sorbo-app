// Capa de presentación · formato: cómo se escribe una fecha de degustación en las tarjetas del detalle.
package com.sorbo.app.ui.components

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** "12 de septiembre" para el año en curso y "12 de septiembre de 2025" para los anteriores. */
fun textoDeFecha(fecha: LocalDate, hoy: LocalDate, idioma: Locale = Locale.getDefault()): String {
    val patron = if (fecha.year == hoy.year) "d 'de' MMMM" else "d 'de' MMMM 'de' yyyy"
    return fecha.format(DateTimeFormatter.ofPattern(patron, idioma))
}
