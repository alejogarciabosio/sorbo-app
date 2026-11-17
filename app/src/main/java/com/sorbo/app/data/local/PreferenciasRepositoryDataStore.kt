// Capa de datos · preferencias con DataStore: implementación real de PreferenciasRepository, sobrevive a cerrar la app.
package com.sorbo.app.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sorbo.app.domain.repository.PreferenciasRepository
import com.sorbo.app.domain.util.agregarBusquedaReciente
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Guarda cuatro valores en un archivo de DataStore. Recibe el [DataStore] ya creado (lo arma `AppContainer`)
 * para poder probarlo con un archivo temporal. La regla de las búsquedas recientes es la de `domain/util`.
 */
class PreferenciasRepositoryDataStore(
    private val dataStore: DataStore<Preferences>,
) : PreferenciasRepository {

    private object Claves {
        val BIENVENIDA_VISTA = booleanPreferencesKey("bienvenida_vista")
        val AVISO_IA_ACEPTADO = booleanPreferencesKey("aviso_ia_aceptado")
        val IA_ACTIVADA = booleanPreferencesKey("ia_activada")
        val BUSQUEDAS_RECIENTES = stringPreferencesKey("busquedas_recientes")
    }

    // Las búsquedas se guardan en un solo texto separadas por salto de línea: una búsqueda no puede contenerlo.
    private val separador = "\n"

    override val bienvenidaVista: Flow<Boolean> = leer { it[Claves.BIENVENIDA_VISTA] ?: false }

    override val avisoIaAceptado: Flow<Boolean> = leer { it[Claves.AVISO_IA_ACEPTADO] ?: false }

    override val iaActivada: Flow<Boolean> = leer { it[Claves.IA_ACTIVADA] ?: true }

    override val busquedasRecientes: Flow<List<String>> = leer { busquedasDe(it) }

    override suspend fun marcarBienvenidaVista() {
        dataStore.edit { it[Claves.BIENVENIDA_VISTA] = true }
    }

    override suspend fun aceptarAvisoIa() {
        dataStore.edit { it[Claves.AVISO_IA_ACEPTADO] = true }
    }

    override suspend fun cambiarIaActivada(activada: Boolean) {
        dataStore.edit { it[Claves.IA_ACTIVADA] = activada }
    }

    override suspend fun agregarBusquedaReciente(texto: String) {
        dataStore.edit { preferencias ->
            val nuevas = agregarBusquedaReciente(busquedasDe(preferencias), texto.replace(separador, " "))
            preferencias[Claves.BUSQUEDAS_RECIENTES] = nuevas.joinToString(separador)
        }
    }

    override suspend fun borrarBusquedasRecientes() {
        dataStore.edit { it.remove(Claves.BUSQUEDAS_RECIENTES) }
    }

    private fun busquedasDe(preferencias: Preferences): List<String> =
        preferencias[Claves.BUSQUEDAS_RECIENTES]?.split(separador)?.filter { it.isNotEmpty() }.orEmpty()

    /** Cada valor es su propio flujo: solo emite cuando cambia ese valor, no cuando cambia otro. */
    private fun <T> leer(valor: (Preferences) -> T): Flow<T> = dataStore.data.map(valor).distinctUntilChanged()
}
