// Capa de presentación · ViewModel: guarda el estado del formulario de edición y decide cuándo guardar o descartar fotos.
package com.sorbo.app.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sorbo.app.domain.model.Vino
import com.sorbo.app.domain.repository.AlmacenFotos
import com.sorbo.app.domain.usecase.DatosDelVino
import com.sorbo.app.domain.usecase.EditarVino
import com.sorbo.app.domain.usecase.ObtenerDetalleVino
import com.sorbo.app.domain.usecase.ResultadoEdicion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.Serializable

/**
 * Lo que se está corrigiendo. [fotoOriginal] es la foto que el vino tiene guardada: si la elegida es otra,
 * la original solo se borra cuando se guardan los cambios. Es [Serializable] para sobrevivir a que Android mate la app.
 */
data class CamposEdicion(
    val nombre: String,
    val bodega: String,
    val cepa: String,
    val anio: String,
    val fotoPath: String?,
    val fotoOriginal: String?,
    val procesandoFoto: Boolean = false,
    val errorFoto: Boolean = false,
) : Serializable

sealed interface EstadoEdicion {
    data object Cargando : EstadoEdicion

    /** El vino no existe (por ejemplo, se eliminó mientras se editaba). */
    data object NoEncontrado : EstadoEdicion

    data class Editando(val campos: CamposEdicion, val nombreVacio: Boolean = false) : EstadoEdicion

    data object Guardado : EstadoEdicion
}

class EditarViewModel(
    private val vinoId: String,
    private val obtenerDetalleVino: ObtenerDetalleVino,
    private val editarVino: EditarVino,
    private val almacenFotos: AlmacenFotos,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoEdicion>(EstadoEdicion.Cargando)
    val estado: StateFlow<EstadoEdicion> = _estado.asStateFlow()

    private var guardando = false

    init {
        viewModelScope.launch {
            // Si el sistema había matado la app se retoma lo que estaba cargado; una foto a medio procesar se da por perdida.
            val retomado = savedStateHandle.get<CamposEdicion>(CLAVE_CAMPOS)?.copy(procesandoFoto = false, errorFoto = false)
            if (retomado != null) {
                mostrar(EstadoEdicion.Editando(retomado))
                return@launch
            }
            val vino = obtenerDetalleVino(vinoId).first()?.vino
            if (vino == null) _estado.value = EstadoEdicion.NoEncontrado else mostrar(EstadoEdicion.Editando(camposDe(vino)))
        }
    }

    fun alCambiarNombre(texto: String) = editar(sinError = true) { it.copy(nombre = texto) }
    fun alCambiarBodega(texto: String) = editar { it.copy(bodega = texto) }
    fun alCambiarCepa(texto: String) = editar { it.copy(cepa = texto) }
    fun alCambiarAnio(texto: String) = editar { it.copy(anio = texto.filter(Char::isDigit).take(LARGO_ANIO)) }

    /** [origen] es la dirección de la foto que sacó la cámara o eligió el usuario de la galería. */
    fun alElegirFoto(origen: String) {
        if (_estado.value !is EstadoEdicion.Editando) return
        editar { it.copy(procesandoFoto = true, errorFoto = false) }
        viewModelScope.launch {
            almacenFotos.guardar(origen)
                .onSuccess { ruta -> alTenerFotoLista(ruta) }
                .onFailure { editar { it.copy(procesandoFoto = false, errorFoto = true) } }
        }
    }

    fun alQuitarFoto() {
        val actual = (_estado.value as? EstadoEdicion.Editando)?.campos ?: return
        descartarSiEsNueva(actual.fotoPath, actual.fotoOriginal)
        editar { it.copy(fotoPath = null, errorFoto = false) }
    }

    fun alGuardar() {
        val actual = _estado.value as? EstadoEdicion.Editando ?: return
        if (guardando || actual.campos.procesandoFoto) return
        guardando = true
        viewModelScope.launch {
            try {
                val resultado = editarVino(vinoId, datosDe(actual.campos))
                when (resultado) {
                    ResultadoEdicion.NombreVacio -> mostrar(actual.copy(nombreVacio = true))
                    ResultadoEdicion.NoExiste -> {
                        descartarSiEsNueva(actual.campos.fotoPath, actual.campos.fotoOriginal)
                        mostrar(EstadoEdicion.NoEncontrado)
                    }
                    is ResultadoEdicion.Guardado -> {
                        resultado.fotoAnterior?.let(almacenFotos::borrar)
                        mostrar(EstadoEdicion.Guardado)
                    }
                }
            } finally {
                guardando = false
            }
        }
    }

    /**
     * El usuario cerró el formulario sin guardar: si había elegido una foto nueva se descarta para no dejar archivos sueltos.
     * No se hace en `onCleared` porque Android también destruye la pantalla por su cuenta y después la restaura.
     */
    fun alCerrar() {
        val campos = (_estado.value as? EstadoEdicion.Editando)?.campos ?: return
        descartarSiEsNueva(campos.fotoPath, campos.fotoOriginal)
    }

    private fun editar(sinError: Boolean = false, cambio: (CamposEdicion) -> CamposEdicion) {
        val actual = _estado.value as? EstadoEdicion.Editando ?: return
        mostrar(EstadoEdicion.Editando(cambio(actual.campos), nombreVacio = actual.nombreVacio && !sinError))
    }

    private fun alTenerFotoLista(ruta: String) {
        val actual = (_estado.value as? EstadoEdicion.Editando)?.campos
        if (actual == null) {
            almacenFotos.borrar(ruta)
            return
        }
        descartarSiEsNueva(actual.fotoPath, actual.fotoOriginal)
        editar { it.copy(fotoPath = ruta, procesandoFoto = false) }
    }

    /** Borra [foto] solo si la eligió el usuario en esta edición; la que el vino ya tenía no se toca hasta guardar. */
    private fun descartarSiEsNueva(foto: String?, original: String?) {
        if (foto != null && foto != original) almacenFotos.borrar(foto)
    }

    /** Publica el estado nuevo y guarda lo cargado para poder retomarlo si el sistema mata la app. */
    private fun mostrar(nuevo: EstadoEdicion) {
        _estado.value = nuevo
        when (nuevo) {
            is EstadoEdicion.Editando -> savedStateHandle[CLAVE_CAMPOS] = nuevo.campos
            EstadoEdicion.Guardado -> savedStateHandle.remove<CamposEdicion>(CLAVE_CAMPOS)
            EstadoEdicion.Cargando, EstadoEdicion.NoEncontrado -> Unit
        }
    }

    private fun camposDe(vino: Vino) = CamposEdicion(
        nombre = vino.nombre,
        bodega = vino.bodega.orEmpty(),
        cepa = vino.cepa.orEmpty(),
        anio = vino.anio?.toString().orEmpty(),
        fotoPath = vino.fotoPath,
        fotoOriginal = vino.fotoPath,
    )

    private fun datosDe(campos: CamposEdicion) = DatosDelVino(
        nombre = campos.nombre,
        bodega = campos.bodega,
        cepa = campos.cepa,
        anio = campos.anio.takeIf { it.length == LARGO_ANIO }?.toIntOrNull(),
        fotoPath = campos.fotoPath,
    )

    private companion object {
        const val LARGO_ANIO = 4
        const val CLAVE_CAMPOS = "campos_edicion"
    }
}
