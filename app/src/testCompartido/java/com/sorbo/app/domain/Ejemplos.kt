package com.sorbo.app.domain

import com.sorbo.app.domain.model.Compania
import com.sorbo.app.domain.model.Comida
import com.sorbo.app.domain.model.Degustacion
import com.sorbo.app.domain.model.Dulzor
import com.sorbo.app.domain.model.Ocasion
import com.sorbo.app.domain.model.Vino
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger

/** Datos de ejemplo para los tests. */
val AHORA: Instant = Instant.parse("2026-11-03T12:00:00Z")
val HOY: LocalDate = LocalDate.of(2026, 11, 3)

private val contador = AtomicInteger()

fun vinoDe(
    nombre: String,
    bodega: String? = null,
    anio: Int? = null,
    cepa: String? = null,
    id: String = "vino-${contador.incrementAndGet()}",
) = Vino(id = id, nombre = nombre, creadoEn = AHORA, actualizadoEn = AHORA, bodega = bodega, cepa = cepa, anio = anio)

fun degustacionDe(
    vinoId: String,
    meGusto: Boolean = true,
    fecha: LocalDate = HOY,
    dulzor: Dulzor? = null,
    comida: Comida? = null,
    compania: Compania? = null,
    ocasion: Ocasion? = null,
) = Degustacion(
    id = "degustacion-${contador.incrementAndGet()}",
    vinoId = vinoId,
    fecha = fecha,
    meGusto = meGusto,
    actualizadoEn = AHORA,
    dulzor = dulzor,
    comida = comida,
    compania = compania,
    ocasion = ocasion,
)
