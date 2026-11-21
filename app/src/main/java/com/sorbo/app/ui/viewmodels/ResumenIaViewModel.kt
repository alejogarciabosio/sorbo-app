// Capa de presentación · ViewModel del resumen con IA de Mis gustos (E2 a E7): decide qué se ofrece y maneja el pedido a la nube.
package com.sorbo.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sorbo.app.domain.model.AnalisisGustos
import com.sorbo.app.domain.model.EstadoSesion
import com.sorbo.app.domain.repository.AnalisisException
import com.sorbo.app.domain.repository.AnalisisGustosRepository
import com.sorbo.app.domain.repository.ConectividadObserver
import com.sorbo.app.domain.repository.MotivoAnalisis
import com.sorbo.app.domain.repository.PreferenciasRepository
import com.sorbo.app.domain.repository.SesionRepository
import com.sorbo.app.domain.repository.VinoRepository
import com.sorbo.app.domain.usecase.CalcularEstadisticasGustos
import com.sorbo.app.domain.usecase.EstadisticasGustos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Lo que Mis gustos muestra sobre el resumen con IA. Los números locales no dependen de esto: siempre se ven. */
sealed interface EstadoResumenIa {
    /** No se ofrece nada: faltan degustaciones, la IA está desactivada en Ajustes o todavía se está cargando. */
    data object Oculto : EstadoResumenIa

    /** E5: para generar el resumen hace falta una cuenta. */
    data object RequiereCuenta : EstadoResumenIa

    /**
     * Se puede pedir un resumen. [ultimo] es el último guardado (se ve aunque no haya conexión, E4) y [nuevasDesdeElUltimo]
     * cuántas degustaciones se sumaron después. [pideAviso] muestra el consentimiento de la primera vez (E7).
     */
    data class Disponible(
        val ultimo: AnalisisGustos?,
        val hayConexion: Boolean,
        val nuevasDesdeElUltimo: Int,
        val pideAviso: Boolean = false,
    ) : EstadoResumenIa

    /** E2: esperando la respuesta. El resumen anterior, si lo hay, se sigue viendo. */
    data class Generando(val ultimo: AnalisisGustos?) : EstadoResumenIa

    /** E6: no salió. Se conserva el resumen anterior y se puede reintentar. */
    data class ConError(val motivo: MotivoAnalisis, val ultimo: AnalisisGustos?) : EstadoResumenIa
}

class ResumenIaViewModel(
    private val repositorio: VinoRepository,
    private val calcular: CalcularEstadisticasGustos,
    private val analisis: AnalisisGustosRepository,
    private val preferencias: PreferenciasRepository,
    sesion: SesionRepository,
    conectividad: ConectividadObserver,
) : ViewModel() {

    /** En qué momento del pedido está la persona. */
    private sealed interface Fase {
        data object Reposo : Fase
        data object PidiendoAviso : Fase
        data object Generando : Fase
        data class Error(val motivo: MotivoAnalisis) : Fase
    }

    private val fase = MutableStateFlow<Fase>(Fase.Reposo)

    private val estadisticas = repositorio.observarDiario().map { vinos -> calcular(vinos.flatMap { it.degustaciones }) }

    private val permisos = combine(sesion.sesion, preferencias.iaActivada, conectividad.hayConexion) { s, activada, conexion ->
        Permisos(conSesion = s is EstadoSesion.ConSesion, iaActivada = activada, hayConexion = conexion)
    }

    val estado: StateFlow<EstadoResumenIa> =
        combine(estadisticas, analisis.observarUltimo(), permisos, fase) { est, ultimo, permiso, actual ->
            when {
                !est.habilitado || !permiso.iaActivada -> EstadoResumenIa.Oculto
                !permiso.conSesion -> EstadoResumenIa.RequiereCuenta
                actual == Fase.Generando -> EstadoResumenIa.Generando(ultimo)
                actual is Fase.Error -> EstadoResumenIa.ConError(actual.motivo, ultimo)
                else -> EstadoResumenIa.Disponible(
                    ultimo = ultimo,
                    hayConexion = permiso.hayConexion,
                    nuevasDesdeElUltimo = ultimo?.let { (est.totalDegustaciones - it.cantidadDegustaciones).coerceAtLeast(0) } ?: 0,
                    pideAviso = actual == Fase.PidiendoAviso,
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoResumenIa.Oculto)

    /** "Generar resumen" o "Actualizar". La primera vez pide el consentimiento antes de mandar nada. */
    fun alGenerar() {
        if (fase.value == Fase.Generando) return
        viewModelScope.launch {
            when {
                !preferencias.avisoIaAceptado.first() -> fase.value = Fase.PidiendoAviso
                else -> generar()
            }
        }
    }

    /** "Usar IA" en el aviso: queda guardado para no volver a preguntar. */
    fun alAceptarAviso() {
        viewModelScope.launch {
            preferencias.aceptarAvisoIa()
            generar()
        }
    }

    /** "Ahora no": no se manda nada. */
    fun alRechazarAviso() {
        if (fase.value == Fase.PidiendoAviso) fase.value = Fase.Reposo
    }

    /** Vuelve a la pantalla normal después de un error, con el resumen anterior a la vista. */
    fun alDescartarError() {
        if (fase.value is Fase.Error) fase.value = Fase.Reposo
    }

    private suspend fun generar() {
        fase.value = Fase.Generando
        val pedido: EstadisticasGustos = estadisticas.first()
        analisis.generar(pedido).fold(
            onSuccess = { fase.value = Fase.Reposo },
            onFailure = { falla ->
                fase.value = Fase.Error((falla as? AnalisisException)?.motivo ?: MotivoAnalisis.ERROR_DESCONOCIDO)
            },
        )
    }

    private data class Permisos(val conSesion: Boolean, val iaActivada: Boolean, val hayConexion: Boolean)
}
