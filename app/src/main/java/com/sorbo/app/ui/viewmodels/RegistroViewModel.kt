// Capa de presentación · ViewModel: guarda el estado del formulario de registro y decide cuándo guardar o preguntar.
package com.sorbo.app.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sorbo.app.domain.model.Compania
import com.sorbo.app.domain.model.Comida
import com.sorbo.app.domain.model.Dulzor
import com.sorbo.app.domain.model.Ocasion
import com.sorbo.app.domain.model.Vino
import com.sorbo.app.domain.repository.AlmacenFotos
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
import java.io.Serializable
import java.time.Clock
import java.time.LocalDate

/**
 * Lo que el usuario fue cargando. El año es texto porque se escribe a mano; se convierte al guardar.
 * Es [Serializable] para que [SavedStateHandle] lo conserve si Android mata la app mientras se usa la cámara.
 */
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
    /** El día en que se probó el vino: hoy salvo que el usuario lo cambie. */
    val fecha: LocalDate,
    /** Ruta de la foto ya achicada y guardada en el teléfono. */
    val fotoPath: String? = null,
    val procesandoFoto: Boolean = false,
    val errorFoto: Boolean = false,
) : Serializable

sealed interface EstadoRegistro {
    /** El formulario abierto; [faltantes] marca los obligatorios que quedaron sin completar al intentar guardar. */
    data class Editando(
        val campos: CamposRegistro,
        val faltantes: Set<CampoObligatorio> = emptySet(),
    ) : EstadoRegistro

    /** Hay un vino parecido en el diario: se pregunta "¿Es el mismo vino?" antes de guardar. */
    data class PreguntandoMismoVino(val campos: CamposRegistro, val parecido: DetalleVino) : EstadoRegistro

    /**
     * @property degustacionId la degustación recién guardada (sirve para deshacerla).
     * @property nombreDelVino el vino al que se sumó, para avisar "Sumamos esta vez a …".
     */
    data class Guardado(
        val vinoId: String,
        val degustacionId: String,
        val vinoNuevo: Boolean,
        val nombreDelVino: String,
    ) : EstadoRegistro
}

class RegistroViewModel(
    private val buscarVinoParecido: BuscarVinoParecido,
    private val registrarDegustacion: RegistrarDegustacion,
    private val obtenerDetalleVino: ObtenerDetalleVino,
    private val almacenFotos: AlmacenFotos,
    private val reloj: Clock,
    private val savedStateHandle: SavedStateHandle,
    nombreInicial: String? = null,
) : ViewModel() {

    // Si el sistema había matado la app, se retoma lo que estaba cargado; una foto a medio procesar se da por perdida.
    private val _estado = MutableStateFlow<EstadoRegistro>(
        EstadoRegistro.Editando(
            savedStateHandle.get<CamposRegistro>(CLAVE_CAMPOS)?.copy(procesandoFoto = false, errorFoto = false)
                ?: CamposRegistro(nombre = nombreInicial.orEmpty(), fecha = LocalDate.now(reloj)),
        ),
    )
    val estado: StateFlow<EstadoRegistro> = _estado.asStateFlow()

    private var guardando = false

    fun alCambiarNombre(texto: String) = editar(quitar = CampoObligatorio.NOMBRE) { it.copy(nombre = texto) }
    fun alCambiarBodega(texto: String) = editar { it.copy(bodega = texto) }
    fun alCambiarCepa(texto: String) = editar { it.copy(cepa = texto) }
    fun alCambiarAnio(texto: String) = editar { it.copy(anio = texto.filter(Char::isDigit).take(LARGO_ANIO)) }
    fun alCambiarNota(texto: String) = editar { it.copy(nota = texto) }

    /** No se puede registrar un vino "del futuro": una fecha posterior a hoy se toma como hoy. */
    fun alCambiarFecha(fecha: LocalDate) = editar { it.copy(fecha = minOf(fecha, LocalDate.now(reloj))) }

    fun alElegirMeGusto(meGusto: Boolean) = editar(quitar = CampoObligatorio.OPINION) { it.copy(meGusto = meGusto) }

    // Tocar de nuevo la opción elegida la deselecciona: son datos opcionales.
    fun alElegirDulzor(dulzor: Dulzor) = editar { it.copy(dulzor = if (it.dulzor == dulzor) null else dulzor) }
    fun alElegirComida(comida: Comida) = editar { it.copy(comida = if (it.comida == comida) null else comida) }
    fun alElegirCompania(compania: Compania) = editar { it.copy(compania = if (it.compania == compania) null else compania) }
    fun alElegirOcasion(ocasion: Ocasion) = editar { it.copy(ocasion = if (it.ocasion == ocasion) null else ocasion) }

    /** [origen] es la dirección de la foto que sacó la cámara o eligió el usuario de la galería. */
    fun alElegirFoto(origen: String) {
        if (_estado.value !is EstadoRegistro.Editando) return
        editar { it.copy(procesandoFoto = true, errorFoto = false) }
        viewModelScope.launch {
            almacenFotos.guardar(origen)
                .onSuccess { ruta -> alTenerFotoLista(ruta) }
                .onFailure { editar { it.copy(procesandoFoto = false, errorFoto = true) } }
        }
    }

    fun alQuitarFoto() {
        val anterior = (_estado.value as? EstadoRegistro.Editando)?.campos?.fotoPath
        editar { it.copy(fotoPath = null, errorFoto = false) }
        anterior?.let(almacenFotos::borrar)
    }

    /**
     * El usuario cerró el formulario sin guardar (con la X o con "atrás"): se descarta la foto para no dejar archivos sueltos.
     * No se borra en `onCleared` porque Android también destruye la pantalla por su cuenta y después restaura el formulario.
     */
    fun alCerrar() {
        val campos = when (val actual = _estado.value) {
            is EstadoRegistro.Editando -> actual.campos
            is EstadoRegistro.PreguntandoMismoVino -> actual.campos
            is EstadoRegistro.Guardado -> null
        }
        campos?.fotoPath?.let(almacenFotos::borrar)
    }

    fun alGuardar() {
        val actual = _estado.value as? EstadoRegistro.Editando ?: return
        if (guardando || actual.campos.procesandoFoto) return
        val faltantes = faltantesDe(actual.campos)
        if (faltantes.isNotEmpty()) {
            mostrar(actual.copy(faltantes = faltantes))
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
        guardar(
            pregunta.campos,
            DestinoRegistro.VinoExistente(pregunta.parecido.vino.id),
            nombreExistente = pregunta.parecido.vino.nombre,
        )
    }

    fun alElegirOtroVino() = responderPregunta { pregunta ->
        guardar(pregunta.campos, DestinoRegistro.VinoNuevo)
    }

    fun alVolverAEditar() {
        val pregunta = _estado.value as? EstadoRegistro.PreguntandoMismoVino ?: return
        mostrar(EstadoRegistro.Editando(pregunta.campos))
    }

    private fun editar(quitar: CampoObligatorio? = null, cambio: (CamposRegistro) -> CamposRegistro) {
        val actual = _estado.value as? EstadoRegistro.Editando ?: return
        val faltantes = if (quitar == null) actual.faltantes else actual.faltantes - quitar
        mostrar(EstadoRegistro.Editando(cambio(actual.campos), faltantes))
    }

    /** Publica el estado nuevo y guarda lo cargado para poder retomarlo si el sistema mata la app. */
    private fun mostrar(nuevo: EstadoRegistro) {
        _estado.value = nuevo
        when (nuevo) {
            is EstadoRegistro.Editando -> savedStateHandle[CLAVE_CAMPOS] = nuevo.campos
            is EstadoRegistro.PreguntandoMismoVino -> savedStateHandle[CLAVE_CAMPOS] = nuevo.campos
            is EstadoRegistro.Guardado -> savedStateHandle.remove<CamposRegistro>(CLAVE_CAMPOS)
        }
    }

    private fun alTenerFotoLista(ruta: String) {
        val actual = _estado.value as? EstadoRegistro.Editando
        if (actual == null) {
            almacenFotos.borrar(ruta)
            return
        }
        actual.campos.fotoPath?.let(almacenFotos::borrar)
        editar { it.copy(fotoPath = ruta, procesandoFoto = false) }
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
                guardar(campos, DestinoRegistro.VinoExistente(existente.id), nombreExistente = existente.nombre)
            else -> {
                val detalle = obtenerDetalleVino(existente.id).first()
                if (detalle == null) {
                    guardar(campos, DestinoRegistro.VinoNuevo)
                } else {
                    mostrar(EstadoRegistro.PreguntandoMismoVino(campos, detalle))
                }
            }
        }
    }

    private suspend fun guardar(campos: CamposRegistro, destino: DestinoRegistro, nombreExistente: String? = null) {
        val nuevo = when (val resultado = registrarDegustacion(formularioDe(campos), destino)) {
            is ResultadoRegistro.Invalido -> EstadoRegistro.Editando(campos, resultado.faltantes)
            is ResultadoRegistro.Guardado -> {
                // Un vino que ya estaba en el diario conserva su foto: la de esta vez no se usa.
                if (!resultado.vinoNuevo) campos.fotoPath?.let(almacenFotos::borrar)
                EstadoRegistro.Guardado(
                    vinoId = resultado.vinoId,
                    degustacionId = resultado.degustacionId,
                    vinoNuevo = resultado.vinoNuevo,
                    nombreDelVino = nombreExistente ?: campos.nombre.trim(),
                )
            }
        }
        mostrar(nuevo)
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
        fecha = campos.fecha,
        bodega = campos.bodega,
        cepa = campos.cepa,
        anio = anioDe(campos),
        fotoPath = campos.fotoPath,
        dulzor = campos.dulzor,
        comida = campos.comida,
        compania = campos.compania,
        ocasion = campos.ocasion,
        nota = campos.nota,
    )

    private companion object {
        const val LARGO_ANIO = 4
        const val ID_CANDIDATO = "candidato"
        const val CLAVE_CAMPOS = "campos_registro"
    }
}
