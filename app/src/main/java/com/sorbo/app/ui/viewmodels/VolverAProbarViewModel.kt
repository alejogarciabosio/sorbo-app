// Capa de presentación · ViewModel: guarda el estado del formulario "Lo volví a probar" y suma la degustación al vino.
package com.sorbo.app.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sorbo.app.domain.model.Compania
import com.sorbo.app.domain.model.Comida
import com.sorbo.app.domain.model.Dulzor
import com.sorbo.app.domain.model.Ocasion
import com.sorbo.app.domain.usecase.DestinoRegistro
import com.sorbo.app.domain.usecase.DetalleVino
import com.sorbo.app.domain.usecase.FormularioRegistro
import com.sorbo.app.domain.usecase.ObtenerDetalleVino
import com.sorbo.app.domain.usecase.RegistrarDegustacion
import com.sorbo.app.domain.usecase.ResultadoRegistro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.Serializable
import java.time.Clock
import java.time.LocalDate

/** Las respuestas de la nueva degustación. Es [Serializable] para sobrevivir a que Android mate la app. */
data class CamposDegustacion(
    val meGusto: Boolean? = null,
    val dulzor: Dulzor? = null,
    val comida: Comida? = null,
    val compania: Compania? = null,
    val ocasion: Ocasion? = null,
    val nota: String = "",
    val fecha: LocalDate,
) : Serializable

sealed interface EstadoVolverAProbar {
    data object Cargando : EstadoVolverAProbar

    /** El vino no existe (por ejemplo, se eliminó mientras se completaba el formulario). */
    data object NoEncontrado : EstadoVolverAProbar

    /**
     * @property precargado el formulario arrancó con lo que se eligió la última vez (dulzor, comida, compañía y ocasión).
     * @property faltaOpinion se intentó guardar sin decir si gustó.
     */
    data class Editando(
        val detalle: DetalleVino,
        val campos: CamposDegustacion,
        val precargado: Boolean,
        val faltaOpinion: Boolean = false,
    ) : EstadoVolverAProbar

    /** @property degustacionId la degustación recién sumada (sirve para deshacerla). */
    data class Guardado(val degustacionId: String, val nombreDelVino: String) : EstadoVolverAProbar
}

class VolverAProbarViewModel(
    private val vinoId: String,
    private val obtenerDetalleVino: ObtenerDetalleVino,
    private val registrarDegustacion: RegistrarDegustacion,
    private val reloj: Clock,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoVolverAProbar>(EstadoVolverAProbar.Cargando)
    val estado: StateFlow<EstadoVolverAProbar> = _estado.asStateFlow()

    /** El día de hoy, para no dejar elegir fechas futuras en el selector. */
    val hoy: LocalDate get() = LocalDate.now(reloj)

    private var guardando = false

    init {
        viewModelScope.launch {
            val detalle = obtenerDetalleVino(vinoId).first()
            if (detalle == null) {
                _estado.value = EstadoVolverAProbar.NoEncontrado
                return@launch
            }
            val retomado = savedStateHandle.get<CamposDegustacion>(CLAVE_CAMPOS)
            mostrar(
                if (retomado != null) {
                    EstadoVolverAProbar.Editando(detalle, retomado, precargado = false)
                } else {
                    camposDeLaUltimaVez(detalle)
                },
            )
        }
    }

    fun alElegirMeGusto(meGusto: Boolean) = editar(opinionRespondida = true) { it.copy(meGusto = meGusto) }

    // Tocar de nuevo la opción elegida la deselecciona: son datos opcionales.
    fun alElegirDulzor(dulzor: Dulzor) = editar { it.copy(dulzor = if (it.dulzor == dulzor) null else dulzor) }
    fun alElegirComida(comida: Comida) = editar { it.copy(comida = if (it.comida == comida) null else comida) }
    fun alElegirCompania(compania: Compania) = editar { it.copy(compania = if (it.compania == compania) null else compania) }
    fun alElegirOcasion(ocasion: Ocasion) = editar { it.copy(ocasion = if (it.ocasion == ocasion) null else ocasion) }
    fun alCambiarNota(texto: String) = editar { it.copy(nota = texto) }

    /** No se puede registrar una degustación "del futuro": una fecha posterior a hoy se toma como hoy. */
    fun alCambiarFecha(fecha: LocalDate) = editar { it.copy(fecha = minOf(fecha, hoy)) }

    fun alGuardar() {
        val actual = _estado.value as? EstadoVolverAProbar.Editando ?: return
        if (guardando) return
        if (actual.campos.meGusto == null) {
            mostrar(actual.copy(faltaOpinion = true))
            return
        }
        guardando = true
        viewModelScope.launch {
            try {
                val formulario = FormularioRegistro(
                    nombre = actual.detalle.vino.nombre,
                    meGusto = actual.campos.meGusto,
                    fecha = actual.campos.fecha,
                    dulzor = actual.campos.dulzor,
                    comida = actual.campos.comida,
                    compania = actual.campos.compania,
                    ocasion = actual.campos.ocasion,
                    nota = actual.campos.nota,
                )
                val resultado = registrarDegustacion(formulario, DestinoRegistro.VinoExistente(vinoId))
                if (resultado is ResultadoRegistro.Guardado) {
                    mostrar(EstadoVolverAProbar.Guardado(resultado.degustacionId, actual.detalle.vino.nombre))
                }
            } catch (_: IllegalArgumentException) {
                // El vino se eliminó mientras se completaba el formulario.
                mostrar(EstadoVolverAProbar.NoEncontrado)
            } finally {
                guardando = false
            }
        }
    }

    /** Arranca con lo que se eligió la última vez; "¿Te gustó?" queda vacío porque cada vez puede ser distinto. */
    private fun camposDeLaUltimaVez(detalle: DetalleVino): EstadoVolverAProbar.Editando {
        val ultima = detalle.degustaciones.firstOrNull()
        val campos = CamposDegustacion(
            dulzor = ultima?.dulzor,
            comida = ultima?.comida,
            compania = ultima?.compania,
            ocasion = ultima?.ocasion,
            fecha = hoy,
        )
        val hayAlgoPrecargado = ultima != null && listOf(ultima.dulzor, ultima.comida, ultima.compania, ultima.ocasion).any { it != null }
        return EstadoVolverAProbar.Editando(detalle, campos, precargado = hayAlgoPrecargado)
    }

    private fun editar(opinionRespondida: Boolean = false, cambio: (CamposDegustacion) -> CamposDegustacion) {
        val actual = _estado.value as? EstadoVolverAProbar.Editando ?: return
        mostrar(
            actual.copy(
                campos = cambio(actual.campos),
                faltaOpinion = actual.faltaOpinion && !opinionRespondida,
            ),
        )
    }

    /** Publica el estado nuevo y guarda lo cargado para poder retomarlo si el sistema mata la app. */
    private fun mostrar(nuevo: EstadoVolverAProbar) {
        _estado.value = nuevo
        when (nuevo) {
            is EstadoVolverAProbar.Editando -> savedStateHandle[CLAVE_CAMPOS] = nuevo.campos
            is EstadoVolverAProbar.Guardado -> savedStateHandle.remove<CamposDegustacion>(CLAVE_CAMPOS)
            EstadoVolverAProbar.Cargando, EstadoVolverAProbar.NoEncontrado -> Unit
        }
    }

    private companion object {
        const val CLAVE_CAMPOS = "campos_degustacion"
    }
}
