// Raíz de la app · contenedor de dependencias: arma repositorio y casos de uso y se los entrega a los ViewModels.
package com.sorbo.app

import android.content.Context
import com.sorbo.app.data.foto.AlmacenFotosLocal
import com.sorbo.app.data.foto.ArchivoParaCamara
import com.sorbo.app.data.local.SorboDatabase
import com.sorbo.app.data.local.VinoRepositoryRoom
import com.sorbo.app.data.memoria.AnalisisGustosRepositoryEnMemoria
import com.sorbo.app.data.local.PreferenciasRepositoryDataStore
import com.sorbo.app.data.local.preferenciasDataStore
import com.sorbo.app.data.ml.LectorEtiquetaMlKit
import com.sorbo.app.data.red.ConectividadObserverAndroid
import com.sorbo.app.data.remote.AlmacenSesionPreferencias
import com.sorbo.app.data.remote.ClienteSupabase
import com.sorbo.app.data.remote.SesionRepositorySupabase
import com.sorbo.app.domain.repository.AlmacenFotos
import com.sorbo.app.domain.repository.AnalisisGustosRepository
import com.sorbo.app.domain.repository.ConectividadObserver
import com.sorbo.app.domain.repository.PreferenciasRepository
import com.sorbo.app.domain.repository.SesionRepository
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

    // La base se crea una sola vez. El diario arranca vacío: los vinos de ejemplo eran solo para desarrollo.
    private val baseDeDatos: SorboDatabase by lazy { SorboDatabase.crear(context) }
    val vinoRepository: VinoRepository by lazy { VinoRepositoryRoom(baseDeDatos.vinoDao(), reloj) }

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

    // Real: la cuenta usa Supabase Auth. La URL y la clave salen de local.properties a través de BuildConfig.
    val sesionRepository: SesionRepository by lazy {
        SesionRepositorySupabase(
            api = ClienteSupabase.crearAuthApi(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_ANON_KEY),
            almacen = AlmacenSesionPreferencias(context),
        )
    }
    // Real: las preferencias se guardan en el teléfono con DataStore.
    val preferenciasRepository: PreferenciasRepository by lazy { PreferenciasRepositoryDataStore(context.preferenciasDataStore) }
    // Real: escucha la red del teléfono.
    val conectividadObserver: ConectividadObserver by lazy { ConectividadObserverAndroid(context) }
    val analisisGustosRepository: AnalisisGustosRepository by lazy { AnalisisGustosRepositoryEnMemoria(reloj) }
}
