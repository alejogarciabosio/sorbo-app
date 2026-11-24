// Capa de datos · programador real: encola el SyncWorker en WorkManager con red como condición.
package com.sorbo.app.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.sorbo.app.domain.repository.ProgramadorDeSincronizacion
import java.util.concurrent.TimeUnit

/**
 * Trabajo **único** ("sincronizar-diario"): pedirlo muchas veces no lanza muchos a la vez.
 * Con `APPEND_OR_REPLACE`, si ya hay uno corriendo, el nuevo queda en fila detrás: así un cambio hecho
 * durante una subida también se sube. Solo corre con red; si falla, reintenta con espera creciente.
 */
class ProgramadorDeSincronizacionWorkManager(private val context: Context) : ProgramadorDeSincronizacion {

    override fun programar() {
        val pedido = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, ESPERA_INICIAL_SEGUNDOS, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(NOMBRE, ExistingWorkPolicy.APPEND_OR_REPLACE, pedido)
    }

    companion object {
        const val NOMBRE = "sincronizar-diario"
        private const val ESPERA_INICIAL_SEGUNDOS = 30L
    }
}
