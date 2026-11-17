// Capa de datos · conectividad real: avisa con un Flow si el teléfono tiene internet, usando ConnectivityManager.
package com.sorbo.app.data.red

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.sorbo.app.domain.repository.ConectividadObserver
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Escucha la red por defecto del teléfono. Emite el estado actual apenas alguien se suscribe y después
 * cada vez que cambia. Solo cuenta como conexión una red con internet **validada**: tener Wi-Fi no es tener internet.
 * Mientras nadie escucha, no hay ningún callback registrado (lo quita `awaitClose`).
 */
class ConectividadObserverAndroid(context: Context) : ConectividadObserver {

    private val connectivityManager = context.getSystemService(ConnectivityManager::class.java)

    override val hayConexion: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, capacidades: NetworkCapabilities) {
                trySend(tieneInternet(capacidades))
            }

            override fun onLost(network: Network) {
                trySend(false)
            }
        }

        trySend(hayConexionAhora())
        connectivityManager.registerDefaultNetworkCallback(callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

    private fun hayConexionAhora(): Boolean =
        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.let(::tieneInternet) ?: false

    private fun tieneInternet(capacidades: NetworkCapabilities): Boolean =
        capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
