// Capa de dominio · contrato: interfaz que la capa de datos implementa y que usan los casos de uso y los ViewModels.
package com.sorbo.app.domain.repository

/**
 * Pide que se respalde el diario en segundo plano, cuando haya red. El dominio no conoce WorkManager:
 * la implementación real (I5) encola un trabajo único; si ya hay uno pendiente, no se duplica.
 */
interface ProgramadorDeSincronizacion {
    fun programar()
}
