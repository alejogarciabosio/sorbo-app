package com.sorbo.app.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Los mismos casos que la versión en memoria, contra la implementación con DataStore.
 * Se usa un DataStore en memoria: el de archivo no puede reescribirse cuando los tests corren en Windows.
 * Que los datos sobreviven a cerrar la app se prueba en el teléfono.
 */
class PreferenciasRepositoryDataStoreTest {

    /** DataStore falso: guarda las preferencias en una variable en lugar de un archivo. */
    private class DataStoreEnMemoria : DataStore<Preferences> {
        private val estado = MutableStateFlow(emptyPreferences())
        override val data: Flow<Preferences> = estado

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
            transform(estado.value).also { estado.value = it }
    }

    private val dataStore = DataStoreEnMemoria()
    private val preferencias = PreferenciasRepositoryDataStore(dataStore)

    @Test
    fun alPrincipioNadaEstaVistoNiAceptadoYLaIaEstaActivada() = runTest {
        assertFalse(preferencias.bienvenidaVista.first())
        assertFalse(preferencias.avisoIaAceptado.first())
        assertTrue(preferencias.iaActivada.first())
        assertEquals(emptyList<String>(), preferencias.busquedasRecientes.first())
    }

    @Test
    fun guardaLoQueElUsuarioElige() = runTest {
        preferencias.marcarBienvenidaVista()
        preferencias.aceptarAvisoIa()
        preferencias.cambiarIaActivada(false)

        assertTrue(preferencias.bienvenidaVista.first())
        assertTrue(preferencias.avisoIaAceptado.first())
        assertFalse(preferencias.iaActivada.first())
    }

    @Test
    fun lasBusquedasRecientesSeAgreganYSeBorran() = runTest {
        preferencias.agregarBusquedaReciente("rutini")
        preferencias.agregarBusquedaReciente("malbec")
        assertEquals(listOf("malbec", "rutini"), preferencias.busquedasRecientes.first())

        preferencias.borrarBusquedasRecientes()
        assertEquals(emptyList<String>(), preferencias.busquedasRecientes.first())
    }

    @Test
    fun lasBusquedasRecientesRespetanLaReglaDelDominio() = runTest {
        listOf("uno", "dos", "tres", "cuatro", "cinco", "seis", "Dos", "  ").forEach {
            preferencias.agregarBusquedaReciente(it)
        }

        assertEquals(listOf("Dos", "seis", "cinco", "cuatro", "tres"), preferencias.busquedasRecientes.first())
    }

    @Test
    fun unaBusquedaConSaltoDeLineaNoSeParteEnDos() = runTest {
        preferencias.agregarBusquedaReciente("rutini\nmalbec")

        assertEquals(listOf("rutini malbec"), preferencias.busquedasRecientes.first())
    }

    @Test
    fun otraInstanciaSobreElMismoDataStoreLeeLoGuardado() = runTest {
        preferencias.marcarBienvenidaVista()
        preferencias.agregarBusquedaReciente("torrontés")

        val otra = PreferenciasRepositoryDataStore(dataStore)

        assertTrue(otra.bienvenidaVista.first())
        assertEquals(listOf("torrontés"), otra.busquedasRecientes.first())
    }
}
