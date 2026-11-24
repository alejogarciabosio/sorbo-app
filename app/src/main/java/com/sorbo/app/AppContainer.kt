// Raíz de la app · contenedor de dependencias: arma repositorio y casos de uso y se los entrega a los ViewModels.
package com.sorbo.app

import android.content.Context
import com.sorbo.app.data.foto.AlmacenFotosLocal
import com.sorbo.app.data.foto.ArchivoParaCamara
import com.sorbo.app.data.local.SorboDatabase
import com.sorbo.app.data.local.VinoRepositoryRoom
import com.sorbo.app.data.local.PreferenciasRepositoryDataStore
import com.sorbo.app.data.local.preferenciasDataStore
import com.sorbo.app.data.memoria.ProgramadorDeSincronizacionEnMemoria
import com.sorbo.app.data.memoria.RespaldoRemotoEnMemoria
import com.sorbo.app.data.ml.LectorEtiquetaMlKit
import com.sorbo.app.data.red.ConectividadObserverAndroid
import com.sorbo.app.data.remote.AlmacenAnalisisPreferencias
import com.sorbo.app.data.remote.AlmacenSesionPreferencias
import com.sorbo.app.data.remote.AnalisisGustosRepositorySupabase
import com.sorbo.app.data.remote.ClienteSupabase
import com.sorbo.app.data.remote.LectorEtiquetaNubeSupabase
import com.sorbo.app.data.remote.SesionRepositorySupabase
import com.sorbo.app.data.remote.SupabaseFuncionesApi
import com.sorbo.app.domain.repository.AlmacenFotos
import com.sorbo.app.domain.repository.AnalisisGustosRepository
import com.sorbo.app.domain.repository.ConectividadObserver
import com.sorbo.app.domain.repository.PreferenciasRepository
import com.sorbo.app.domain.repository.ProgramadorDeSincronizacion
import com.sorbo.app.domain.repository.RespaldoRemoto
import com.sorbo.app.domain.repository.SesionRepository
import com.sorbo.app.domain.repository.LectorEtiqueta
import com.sorbo.app.domain.repository.LectorEtiquetaNube
import com.sorbo.app.domain.repository.VinoRepository
import com.sorbo.app.domain.usecase.AlIniciarSesion
import com.sorbo.app.domain.usecase.BuscarEnDiario
import com.sorbo.app.domain.usecase.BuscarVinoParecido
import com.sorbo.app.domain.usecase.CalcularEstadisticasGustos
import com.sorbo.app.domain.usecase.DeshacerDegustacion
import com.sorbo.app.domain.usecase.EditarVino
import com.sorbo.app.domain.usecase.EliminarVino
import com.sorbo.app.domain.usecase.LeerEtiquetaEnLaNube
import com.sorbo.app.domain.usecase.ObtenerDetalleVino
import com.sorbo.app.domain.usecase.PuedeLeerEnLaNube
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

    // Provisorios hasta I5: el respaldo real (Retrofit) y el programador con WorkManager reemplazan a estos.
    val respaldoRemoto: RespaldoRemoto by lazy { RespaldoRemotoEnMemoria() }
    val programadorDeSincronizacion: ProgramadorDeSincronizacion by lazy { ProgramadorDeSincronizacionEnMemoria() }

    // Lo llaman las pantallas de cuenta después de iniciar sesión o crear cuenta.
    val alIniciarSesion by lazy { AlIniciarSesion(vinoRepository, respaldoRemoto, programadorDeSincronizacion) }

    // Real: la cuenta usa Supabase Auth. La URL y la clave salen de local.properties a través de BuildConfig.
    // La misma instancia es el repositorio de sesión y el proveedor del token para los otros clientes de Supabase.
    private val sesionSupabase: SesionRepositorySupabase by lazy {
        SesionRepositorySupabase(
            api = ClienteSupabase.crearAuthApi(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_ANON_KEY),
            almacen = AlmacenSesionPreferencias(context),
        )
    }
    val sesionRepository: SesionRepository get() = sesionSupabase
    // Real: las preferencias se guardan en el teléfono con DataStore.
    val preferenciasRepository: PreferenciasRepository by lazy { PreferenciasRepositoryDataStore(context.preferenciasDataStore) }
    // Real: escucha la red del teléfono.
    val conectividadObserver: ConectividadObserver by lazy { ConectividadObserverAndroid(context) }
    private val funcionesApi: SupabaseFuncionesApi by lazy {
        ClienteSupabase.crearFuncionesApi(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_ANON_KEY)
    }
    // Real: el resumen con IA lo escribe la función analizar-gustos; el último queda guardado en el teléfono.
    val analisisGustosRepository: AnalisisGustosRepository by lazy {
        AnalisisGustosRepositorySupabase(
            api = funcionesApi,
            token = sesionSupabase,
            almacen = AlmacenAnalisisPreferencias(context),
            reloj = reloj,
        )
    }

    // La mejora opcional de la lectura de etiquetas: solo con sesión, aviso aceptado, IA activada y conexión.
    // ML Kit siempre lee primero en el teléfono; la nube solo mejora lo sugerido si responde a tiempo.
    val lectorEtiquetaNube: LectorEtiquetaNube by lazy { LectorEtiquetaNubeSupabase(api = funcionesApi, token = sesionSupabase) }
    val puedeLeerEnLaNube by lazy { PuedeLeerEnLaNube(sesionRepository, preferenciasRepository, conectividadObserver) }
    val leerEtiquetaEnLaNube by lazy { LeerEtiquetaEnLaNube(puedeLeerEnLaNube, lectorEtiquetaNube) }
}
