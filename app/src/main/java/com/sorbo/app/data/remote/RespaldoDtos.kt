// Capa de datos · DTOs de las tablas de Supabase: vinos y degustaciones como los guarda la nube (snake_case).
package com.sorbo.app.data.remote

import com.google.gson.annotations.SerializedName
import com.sorbo.app.domain.model.Comida
import com.sorbo.app.domain.model.Compania
import com.sorbo.app.domain.model.Degustacion
import com.sorbo.app.domain.model.Dulzor
import com.sorbo.app.domain.model.EstadoSync
import com.sorbo.app.domain.model.Ocasion
import com.sorbo.app.domain.model.Vino
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime

/**
 * Una fila de `vinos`. `fotoPath` y `estadoSync` no viajan: son solo del teléfono.
 * Las fechas van como texto ISO-8601, que es lo que entiende `timestamptz`.
 */
data class VinoDto(
    val id: String,
    @SerializedName("usuario_id") val usuarioId: String?,
    val nombre: String,
    val bodega: String?,
    val cepa: String?,
    val anio: Int?,
    @SerializedName("foto_url") val fotoUrl: String?,
    @SerializedName("creado_en") val creadoEn: String,
    @SerializedName("actualizado_en") val actualizadoEn: String,
    val eliminado: Boolean,
)

/**
 * Una fila de `degustaciones`. No lleva `usuario_id`: la nube lo completa con el usuario del token,
 * que es el mismo dueño del vino (lo exige la clave foránea). Las enumeraciones viajan por su nombre.
 */
data class DegustacionDto(
    val id: String,
    @SerializedName("vino_id") val vinoId: String,
    val fecha: String,
    @SerializedName("me_gusto") val meGusto: Boolean,
    val dulzor: String?,
    val comida: String?,
    val compania: String?,
    val ocasion: String?,
    val nota: String?,
    @SerializedName("actualizado_en") val actualizadoEn: String,
    val eliminado: Boolean,
)

/** Para preguntar si hay algo en la nube alcanza con el id. */
data class IdDto(val id: String)

fun Vino.aDto(): VinoDto = VinoDto(
    id = id,
    usuarioId = usuarioId,
    nombre = nombre,
    bodega = bodega,
    cepa = cepa,
    anio = anio,
    fotoUrl = fotoUrl,
    creadoEn = creadoEn.toString(),
    actualizadoEn = actualizadoEn.toString(),
    eliminado = eliminado,
)

fun Degustacion.aDto(): DegustacionDto = DegustacionDto(
    id = id,
    vinoId = vinoId,
    fecha = fecha.toString(),
    meGusto = meGusto,
    dulzor = dulzor?.name,
    comida = comida?.name,
    compania = compania?.name,
    ocasion = ocasion?.name,
    nota = nota,
    actualizadoEn = actualizadoEn.toString(),
    eliminado = eliminado,
)

// --- De la nube al teléfono (restauración) ---

/** Supabase devuelve las fechas con zona (`2026-11-24T18:31:00.123+00:00`): se leen con `OffsetDateTime`. */
private fun instanteDe(texto: String): Instant = OffsetDateTime.parse(texto).toInstant()

/** Lo restaurado ya está en la nube: queda SINCRONIZADO. La foto local (`fotoPath`) se completa al bajarla. */
fun VinoDto.aDominio(): Vino = Vino(
    id = id,
    nombre = nombre,
    creadoEn = instanteDe(creadoEn),
    actualizadoEn = instanteDe(actualizadoEn),
    usuarioId = usuarioId,
    bodega = bodega,
    cepa = cepa,
    anio = anio,
    fotoUrl = fotoUrl,
    estadoSync = EstadoSync.SINCRONIZADO,
    eliminado = eliminado,
)

/** Un valor de enumeración que la app no conoce se descarta en lugar de romper la restauración. */
private inline fun <reified T : Enum<T>> enumDe(nombre: String?): T? =
    nombre?.let { n -> enumValues<T>().firstOrNull { it.name == n } }

fun DegustacionDto.aDominio(): Degustacion = Degustacion(
    id = id,
    vinoId = vinoId,
    fecha = LocalDate.parse(fecha),
    meGusto = meGusto,
    actualizadoEn = instanteDe(actualizadoEn),
    dulzor = enumDe<Dulzor>(dulzor),
    comida = enumDe<Comida>(comida),
    compania = enumDe<Compania>(compania),
    ocasion = enumDe<Ocasion>(ocasion),
    nota = nota,
    estadoSync = EstadoSync.SINCRONIZADO,
    eliminado = eliminado,
)
