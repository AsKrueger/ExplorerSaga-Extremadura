package com.alonso.explorersaga.data.content

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.alonso.explorersaga.ExplorerSagaApplication
import java.util.concurrent.TimeUnit

/** Comprueba cada pocos días si hay un paquete de contenido nuevo y lo instala. */
class ContentSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as ExplorerSagaApplication).container
        return when (container.contentRepository.sync()) {
            SyncResult.FAILED -> if (runAttemptCount < 3) Result.retry() else Result.failure()
            else -> Result.success()
        }
    }

    companion object {
        private const val WORK_NAME = "content-sync"

        /** Programa (o reprograma, si cambia el ajuste de wifi) la sincronización periódica. */
        fun schedule(context: Context, wifiOnly: Boolean) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
            val request = PeriodicWorkRequestBuilder<ContentSyncWorker>(3, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
        }
    }
}
