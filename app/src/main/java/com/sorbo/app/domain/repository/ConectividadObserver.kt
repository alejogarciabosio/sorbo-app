// Capa de dominio · contrato: interfaz que la capa de datos implementa y que usan los casos de uso y los ViewModels.
package com.sorbo.app.domain.repository

import kotlinx.coroutines.flow.Flow

/** Avisa si el teléfono tiene red. La implementación real usa `ConnectivityManager.NetworkCallback`. */
interface ConectividadObserver {
    val hayConexion: Flow<Boolean>
}
