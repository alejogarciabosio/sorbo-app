// Capa de datos · conversores de Room: traducen tipos que SQLite no conoce a tipos que sí puede guardar.
package com.sorbo.app.data.local

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate

/**
 * SQLite solo guarda números, texto y bytes. Room usa estas funciones para guardar y leer:
 * - [Instant] como milisegundos (Long): ordena bien y compara rápido.
 * - [LocalDate] como texto ISO ("2026-11-17"): se lee fácil y el orden alfabético coincide con el de las fechas.
 * Las enumeraciones (dulzor, comida, estado de sync…) no necesitan conversor: Room ya las guarda por su nombre.
 */
class Conversores {

    @TypeConverter
    fun instantAMilis(instante: Instant?): Long? = instante?.toEpochMilli()

    @TypeConverter
    fun milisAInstant(milis: Long?): Instant? = milis?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun fechaATexto(fecha: LocalDate?): String? = fecha?.toString()

    @TypeConverter
    fun textoAFecha(texto: String?): LocalDate? = texto?.let(LocalDate::parse)
}
