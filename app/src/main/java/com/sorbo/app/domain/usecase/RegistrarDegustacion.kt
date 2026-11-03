// Capa de dominio · caso de uso: una regla de negocio pura y testeable. No estaba en la guía de clase; se justifica en el README.
package com.sorbo.app.domain.usecase

import com.sorbo.app.domain.model.Compania
import com.sorbo.app.domain.model.Comida
import com.sorbo.app.domain.model.Degustacion
import com.sorbo.app.domain.model.Dulzor
import com.sorbo.app.domain.model.Ocasion
import com.sorbo.app.domain.model.Vino
import com.sorbo.app.domain.repository.VinoRepository
import com.sorbo.app.domain.util.extraerAnio
import java.time.Clock
import java.time.LocalDate
import java.util.UUID

/** Lo que el usuario cargó en el formulario de registro. Solo el nombre y la opinión son obligatorios. */
data class FormularioRegistro(
    val nombre: String,
    val meGusto: Boolean?,
    val fecha: LocalDate,
    val bodega: String? = null,
    val cepa: String? = null,
    val anio: Int? = null,
    val fotoPath: String? = null,
    val dulzor: Dulzor? = null,
    val comida: Comida? = null,
    val compania: Compania? = null,
    val ocasion: Ocasion? = null,
    val nota: String? = null,
)

/** Adónde va la degustación: lo decide la pantalla con [BuscarVinoParecido] y la respuesta del usuario. */
sealed interface DestinoRegistro {
    data object VinoNuevo : DestinoRegistro
    data class VinoExistente(val vinoId: String) : DestinoRegistro
}

enum class CampoObligatorio { NOMBRE, OPINION }

sealed interface ResultadoRegistro {
    data class Invalido(val faltantes: Set<CampoObligatorio>) : ResultadoRegistro
    data class Guardado(val vinoId: String, val degustacionId: String, val vinoNuevo: Boolean) : ResultadoRegistro
}

/** Valida el formulario y guarda el vino nuevo o agrega la degustación al vino existente. No usa la red. */
class RegistrarDegustacion(
    private val repositorio: VinoRepository,
    private val reloj: Clock = Clock.systemUTC(),
    private val generarId: () -> String = { UUID.randomUUID().toString() },
) {

    suspend operator fun invoke(formulario: FormularioRegistro, destino: DestinoRegistro): ResultadoRegistro {
        val nombre = formulario.nombre.trim()
        val meGusto = formulario.meGusto
        val faltantes = buildSet {
            if (nombre.isEmpty()) add(CampoObligatorio.NOMBRE)
            if (meGusto == null) add(CampoObligatorio.OPINION)
        }
        if (meGusto == null || faltantes.isNotEmpty()) return ResultadoRegistro.Invalido(faltantes)

        val ahora = reloj.instant()
        return when (destino) {
            DestinoRegistro.VinoNuevo -> {
                val vino = Vino(
                    id = generarId(),
                    nombre = nombre,
                    creadoEn = ahora,
                    actualizadoEn = ahora,
                    bodega = formulario.bodega.limpio(),
                    cepa = formulario.cepa.limpio(),
                    anio = formulario.anio ?: extraerAnio(nombre),
                    fotoPath = formulario.fotoPath,
                )
                val degustacion = degustacionDe(formulario, vino.id, meGusto, ahora)
                repositorio.guardarVinoNuevo(vino, degustacion)
                ResultadoRegistro.Guardado(vino.id, degustacion.id, vinoNuevo = true)
            }
            is DestinoRegistro.VinoExistente -> {
                val degustacion = degustacionDe(formulario, destino.vinoId, meGusto, ahora)
                repositorio.agregarDegustacion(degustacion)
                ResultadoRegistro.Guardado(destino.vinoId, degustacion.id, vinoNuevo = false)
            }
        }
    }

    private fun degustacionDe(formulario: FormularioRegistro, vinoId: String, meGusto: Boolean, ahora: java.time.Instant) =
        Degustacion(
            id = generarId(),
            vinoId = vinoId,
            fecha = formulario.fecha,
            meGusto = meGusto,
            actualizadoEn = ahora,
            dulzor = formulario.dulzor,
            comida = formulario.comida,
            compania = formulario.compania,
            ocasion = formulario.ocasion,
            nota = formulario.nota.limpio(),
        )

    private fun String?.limpio(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
}
