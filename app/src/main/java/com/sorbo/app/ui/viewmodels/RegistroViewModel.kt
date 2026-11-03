// Capa de presentación · ViewModel: guarda el estado del formulario de registro y decide cuándo guardar o preguntar.
package com.sorbo.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sorbo.app.domain.model.Compania
import com.sorbo.app.domain.model.Comida
import com.sorbo.app.domain.model.Dulzor
import com.sorbo.app.domain.model.Ocasion
import com.sorbo.app.domain.model.Vino
import com.sorbo.app.domain.usecase.BuscarVinoParecido
import com.sorbo.app.domain.usecase.CampoObligatorio
import com.sorbo.app.domain.usecase.DestinoRegistro
import com.sorbo.app.domain.usecase.DetalleVino
import com.sorbo.app.domain.usecase.FormularioRegistro
import com.sorbo.app.domain.usecase.ObtenerDetalleVino
import com.sorbo.app.domain.usecase.RegistrarDegustacion
import com.sorbo.app.domain.usecase.ResultadoRegistro
import com.sorbo.app.domain.usecase.TipoCoincidencia
import com.sorbo.app.domain.util.extraerAnio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

/** Lo que el usuario fue cargando. El año es texto porque se escribe a mano; se convierte al guardar. */
data class CamposRegistro(
    val nombre: String = "",
    val bodega: String = "",
    val cepa: String = "",
    val anio: String = "",
    val meGusto: Boolean? = null,
    val dulzor: Dulzor? = null,
    val comida: Comida? = null,
    val compania: Compania? = null,
    val ocasion: Ocasion? = null,
    val nota: String = "",
)

sealed interface EstadoRegistro {
    /** El formulario abierto; [faltantes] marca los obligatorios que quedaron sin completar al intentar guardar. */
    data class Editando(
        val campos: CamposRegistro,
        val faltantes: Set<CampoObligatorio> = emptySet(),
    ) : EstadoRegistro

    /** Hay un vino parecido en el diario: se pregunta "¿Es el mismo vino?" antes de guardar. */
    data class PreguntandoMismoVino(val campos: CamposRegistro, val parecido: DetalleVino) : EstadoRegistro

    data class Guardado(val vinoId: String, val vinoNuevo: Boolean) : EstadoRegistro
}

class RegistroViewModel(
    private val buscarVinoParecido: BuscarVinoParecido,
    private val registrarDegustacion: RegistrarDegustacion,
    private val obtenerDetalleVino: ObtenerDetalleVino,
    private val reloj: Clock,
    nombreInicial: String? = null,
) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoRegistro>(
        EstadoRegistro.Editando(CamposRegistro(nombre = nombreInicial.orEmpty())),
    )
    val estado: StateFlow<EstadoRegistro> = _estado.asStateFlow()

    private var guardando = false

    fun alCambiarNombre(texto: String) = editar(quitar = CampoObligatorio.NOMBRE) { it.copy(nombre = texto) }
    fun alCambiarBodega(texto: String) = editar { it.copy(bodega = texto) }
    fun alCambiarCepa(texto: String) = editar { it.copy(cepa = texto) }
    fun alCambiarAnio(texto: String) = editar { it.copy(anio = texto.filter(Char::isDigit).take(LARGO_ANIO)) }
    fun alCambiarNota(texto: String) = editar { it.copy(nota = texto) }

    fun alElegirMeGusto(meGusto: Boolean) = editar(quitar = CampoObligatorio.OPINION) { it.copy(meGusto = meGusto) }

    // Tocar de nuevo la opción elegida la deselecciona: son datos opcionales.
    fun alElegirDulzor(dulzor: Dulzor) = editar { it.copy(dulzor = if (it.dulzor == dulzor) null else dulzor) }
    fun alElegirComida(comida: Comida) = editar { it.copy(comida = if (it.comida == comida) null else comida) }
    fun alElegirCompania(compania: Compania) = editar { it.copy(compania = if (it.compania == compania) null else compania) }
    fun alElegirOcasion(ocasion: Ocasion) = editar { it.copy(ocasion = if (it.ocasion == ocasion) null else ocasion) }

    fun alGuardar() {
        val actual = _estado.value as? EstadoRegistro.Editando ?: return
        if (guardando) return
        val faltantes = faltantesDe(actual.campos)
        if (faltantes.isNotEmpty()) {
            _estado.value = actual.copy(faltantes = faltantes)
            return
        }
        guardando = true
        viewModelScope.launch {
            try {
                decidirDestino(actual.campos)
            } finally {
                guardando = false
            }
        }
    }

    fun alConfirmarMismoVino() = responderPregunta { pregunta ->
        guardar(pregunta.campos, DestinoRegistro.VinoExistente(pregunta.parecido.vino.id))
    }

    fun alElegirOtroVino() = responderPregunta { pregunta ->
        guardar(pregunta.campos, DestinoRegistro.VinoNuevo)
    }

    fun alVolverAEditar() {
        val pregunta = _estado.value as? EstadoRegistro.PreguntandoMismoVino ?: return
        _estado.value = EstadoRegistro.Editando(pregunta.campos)
    }

    private fun editar(quitar: CampoObligatorio? = null, cambio: (CamposRegistro) -> CamposRegistro) {
        val actual = _estado.value as? EstadoRegistro.Editando ?: return
        val faltantes = if (quitar == null) actual.faltantes else actual.faltantes - quitar
        _estado.value = EstadoRegistro.Editando(cambio(actual.campos), faltantes)
    }

    private fun responderPregunta(accion: suspend (EstadoRegistro.PreguntandoMismoVino) -> Unit) {
        val pregunta = _estado.value as? EstadoRegistro.PreguntandoMismoVino ?: return
        if (guardando) return
        guardando = true
        viewModelScope.launch {
            try {
                accion(pregunta)
            } finally {
                guardando = false
            }
        }
    }

    private suspend fun decidirDestino(campos: CamposRegistro) {
        val coincidencia = buscarVinoParecido(candidatoDe(campos))
        val existente = coincidencia.vino
        when {
            existente == null -> guardar(campos, DestinoRegistro.VinoNuevo)
            coincidencia.tipo == TipoCoincidencia.MISMO_VINO ->
                guardar(campos, DestinoRegistro.VinoExistente(existente.id))
            else -> {
                val detalle = obtenerDetalleVino(existente.id).first()
                if (detalle == null) {
                    guardar(campos, DestinoRegistro.VinoNuevo)
                } else {
                    _estado.value = EstadoRegistro.PreguntandoMismoVino(campos, detalle)
                }
            }
        }
    }

    private suspend fun guardar(campos: CamposRegistro, destino: DestinoRegistro) {
        _estado.value = when (val resultado = registrarDegustacion(formularioDe(campos), destino)) {
            is ResultadoRegistro.Invalido -> EstadoRegistro.Editando(campos, resultado.faltantes)
            is ResultadoRegistro.Guardado -> EstadoRegistro.Guardado(resultado.vinoId, resultado.vinoNuevo)
        }
    }

    private fun faltantesDe(campos: CamposRegistro): Set<CampoObligatorio> = buildSet {
        if (campos.nombre.isBlank()) add(CampoObligatorio.NOMBRE)
        if (campos.meGusto == null) add(CampoObligatorio.OPINION)
    }

    /** El año escrito a mano o, si no hay, el que aparece en el nombre ("Rutini Malbec 2021"). */
    private fun anioDe(campos: CamposRegistro): Int? =
        campos.anio.takeIf { it.length == LARGO_ANIO }?.toIntOrNull() ?: extraerAnio(campos.nombre)

    /** Un vino todavía sin guardar, solo para compararlo con los del diario. */
    private fun candidatoDe(campos: CamposRegistro): Vino {
        val ahora = reloj.instant()
        return Vino(
            id = ID_CANDIDATO,
            nombre = campos.nombre.trim(),
            creadoEn = ahora,
            actualizadoEn = ahora,
            bodega = campos.bodega.trim().ifEmpty { null },
            cepa = campos.cepa.trim().ifEmpty { null },
            anio = anioDe(campos),
        )
    }

    private fun formularioDe(campos: CamposRegistro) = FormularioRegistro(
        nombre = campos.nombre,
        meGusto = campos.meGusto,
        fecha = LocalDate.now(reloj),
        bodega = campos.bodega,
        cepa = campos.cepa,
        anio = anioDe(campos),
        dulzor = campos.dulzor,
        comida = campos.comida,
        compania = campos.compania,
        ocasion = campos.ocasion,
        nota = campos.nota,
    )

    private companion object {
        const val LARGO_ANIO = 4
        const val ID_CANDIDATO = "candidato"
    }
}
