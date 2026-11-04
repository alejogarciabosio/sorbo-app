// Raíz de la app · contenedor de dependencias: arma repositorio y casos de uso y se los entrega a los ViewModels.
package com.sorbo.app

import android.content.Context
import com.sorbo.app.data.foto.AlmacenFotosLocal
import com.sorbo.app.data.foto.ArchivoParaCamara
import com.sorbo.app.data.memoria.DatosDeEjemplo
import com.sorbo.app.data.memoria.VinoRepositoryEnMemoria
import com.sorbo.app.data.ml.LectorEtiquetaMlKit
import com.sorbo.app.domain.repository.AlmacenFotos
import com.sorbo.app.domain.repository.LectorEtiqueta
import com.sorbo.app.domain.repository.VinoRepository
import com.sorbo.app.domain.usecase.BuscarEnDiario
import com.sorbo.app.domain.usecase.BuscarVinoParecido
import com.sorbo.app.domain.usecase.CalcularEstadisticasGustos
import com.sorbo.app.domain.usecase.DeshacerDegustacion
import com.sorbo.app.domain.usecase.EditarVino
import com.sorbo.app.domain.usecase.EliminarVino
import com.sorbo.app.domain.usecase.ObtenerDetalleVino
import com.sorbo.app.domain.usecase.RegistrarDegustacion
import com.sorbo.app.domain.usecase.SugerirDatosDesdeFoto
import java.time.Clock

/**
 * Inyección de dependencias manual: crea una sola vez las dependencias compartidas
 * (repositorios, casos de uso) y se las pasa a los ViewModels.
 */
class AppContainer(private val context: Context) {

    val reloj: Clock = Clock.systemDefaultZone()

    // Provisorio: se reemplaza por la implementación con Room, que cumple la misma interfaz.
    val vinoRepository: VinoRepository by lazy { VinoRepositoryEnMemoria(reloj, DatosDeEjemplo.vinos(reloj)) }

    val almacenFotos: AlmacenFotos by lazy { AlmacenFotosLocal(context) }
    val archivoParaCamara by lazy { ArchivoParaCamara(context) }

    // La lectura de la etiqueta ocurre en el teléfono con ML Kit: no usa la red.
    val lectorEtiqueta: LectorEtiqueta by lazy { LectorEtiquetaMlKit() }
    val sugerirDatosDesdeFoto by lazy { SugerirDatosDesdeFoto(lectorEtiqueta) }

    val buscarVinoParecido by lazy { BuscarVinoParecido(vinoRepository) }
    val registrarDegustacion by lazy { RegistrarDegustacion(vinoRepository, reloj) }
    val buscarEnDiario by lazy { BuscarEnDiario(vinoRepository) }
    val obtenerDetalleVino by lazy { ObtenerDetalleVino(vinoRepository) }
    val eliminarVino by lazy { EliminarVino(vinoRepository) }
    val editarVino by lazy { EditarVino(vinoRepository) }
    val deshacerDegustacion by lazy { DeshacerDegustacion(vinoRepository) }
    val calcularEstadisticasGustos by lazy { CalcularEstadisticasGustos() }
}
