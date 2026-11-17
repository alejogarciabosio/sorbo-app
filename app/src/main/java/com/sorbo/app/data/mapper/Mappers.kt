// Capa de datos · mappers: convierten entidades de Room en modelos del dominio y viceversa, para que la UI nunca vea Room.
package com.sorbo.app.data.mapper

import com.sorbo.app.data.local.DegustacionEntidad
import com.sorbo.app.data.local.VinoConDegustacionesRelacion
import com.sorbo.app.data.local.VinoEntidad
import com.sorbo.app.domain.model.Degustacion
import com.sorbo.app.domain.model.Vino
import com.sorbo.app.domain.model.VinoConDegustaciones

/** Las degustaciones eliminadas se descartan acá: el dominio nunca las ve dentro de un vino. */
fun VinoConDegustacionesRelacion.aDominio(): VinoConDegustaciones = VinoConDegustaciones(
    vino = vino.aDominio(),
    degustaciones = degustaciones.filter { !it.eliminado }.map { it.aDominio() },
)

fun VinoEntidad.aDominio(): Vino = Vino(
    id = id,
    nombre = nombre,
    creadoEn = creadoEn,
    actualizadoEn = actualizadoEn,
    usuarioId = usuarioId,
    bodega = bodega,
    cepa = cepa,
    anio = anio,
    fotoPath = fotoPath,
    fotoUrl = fotoUrl,
    estadoSync = estadoSync,
    eliminado = eliminado,
)

/** El texto de búsqueda no se guarda en el modelo: se calcula de nuevo cada vez que el vino se escribe. */
fun Vino.aEntidad(): VinoEntidad = VinoEntidad(
    id = id,
    usuarioId = usuarioId,
    nombre = nombre,
    bodega = bodega,
    cepa = cepa,
    anio = anio,
    fotoPath = fotoPath,
    fotoUrl = fotoUrl,
    textoBusqueda = textoBusqueda,
    creadoEn = creadoEn,
    actualizadoEn = actualizadoEn,
    estadoSync = estadoSync,
    eliminado = eliminado,
)

fun DegustacionEntidad.aDominio(): Degustacion = Degustacion(
    id = id,
    vinoId = vinoId,
    fecha = fecha,
    meGusto = meGusto,
    actualizadoEn = actualizadoEn,
    dulzor = dulzor,
    comida = comida,
    compania = compania,
    ocasion = ocasion,
    nota = nota,
    estadoSync = estadoSync,
    eliminado = eliminado,
)

fun Degustacion.aEntidad(): DegustacionEntidad = DegustacionEntidad(
    id = id,
    vinoId = vinoId,
    fecha = fecha,
    meGusto = meGusto,
    dulzor = dulzor,
    comida = comida,
    compania = compania,
    ocasion = ocasion,
    nota = nota,
    actualizadoEn = actualizadoEn,
    estadoSync = estadoSync,
    eliminado = eliminado,
)
