// Capa de datos · trabajo de WorkManager: corre la sincronización en segundo plano, aunque la app esté cerrada.
package com.sorbo.app.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sorbo.app.SorboApplication
import com.sorbo.app.domain.usecase.ResultadoSincronizacion

/** Qué le contesta el trabajo a WorkManager según cómo terminó la sincronización. */
enum class AccionTrabajo { TERMINAR, REINTENTAR, FALLAR }

/**
 * Sin cuenta, sin nada pendiente o con todo subido, el trabajo termina. Si falló por algo que se arregla
 * esperando (sin red, servidor caído), WorkManager lo reintenta más tarde con espera creciente.
 * Si es un error que reintentar no arregla (un dato rechazado), falla; lo pendiente sigue PENDIENTE.
 */
fun accionPara(resultado: ResultadoSincronizacion): AccionTrabajo = when (resultado) {
    ResultadoSincronizacion.SinSesion,
    ResultadoSincronizacion.NadaPendiente,
    is ResultadoSincronizacion.Sincronizado,
    -> AccionTrabajo.TERMINAR
    is ResultadoSincronizacion.Fallo -> if (resultado.reintentable) AccionTrabajo.REINTENTAR else AccionTrabajo.FALLAR
}

/** Toma sus dependencias del contenedor de la app, igual que los ViewModels. */
class SyncWorker(context: Context, parametros: WorkerParameters) : CoroutineWorker(context, parametros) {

    override suspend fun doWork(): Result {
        val contenedor = (applicationContext as SorboApplication).container
        return when (accionPara(contenedor.sincronizarDiario())) {
            AccionTrabajo.TERMINAR -> Result.success()
            AccionTrabajo.REINTENTAR -> Result.retry()
            AccionTrabajo.FALLAR -> Result.failure()
        }
    }
}
