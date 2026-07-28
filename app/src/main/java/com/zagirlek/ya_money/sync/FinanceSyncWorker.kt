package com.zagirlek.ya_money.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.zagirlek.finance.api.error.FinanceNetworkException
import com.zagirlek.finance.impl.FinanceSyncCoordinator
import kotlin.coroutines.cancellation.CancellationException

class FinanceSyncWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
    private val syncCoordinator: FinanceSyncCoordinator,
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result = try {
        Log.d(SyncLogTag, "Worker started: id=$id, attempt=$runAttemptCount")
        val result = syncCoordinator.sync()
        if (result.needsRetry) {
            Log.d(SyncLogTag, "Worker finished with retry: id=$id")
            Result.retry()
        } else {
            Log.d(SyncLogTag, "Worker finished successfully: id=$id")
            Result.success()
        }
    } catch (error: CancellationException) {
        throw error
    } catch (error: FinanceNetworkException.Network) {
        Log.w(SyncLogTag, "Worker failed with network error: id=$id", error)
        Result.retry()
    } catch (error: FinanceNetworkException.ServerFailure) {
        Log.w(SyncLogTag, "Worker failed with server error: id=$id", error)
        Result.retry()
    } catch (error: Exception) {
        Log.e(SyncLogTag, "Worker failed permanently: id=$id", error)
        Result.failure()
    }

    private companion object {
        const val SyncLogTag = "YaMoneySync"
    }
}

class FinanceSyncWorkerFactory(
    private val coordinatorProvider: () -> FinanceSyncCoordinator,
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? = if (workerClassName == FinanceSyncWorker::class.java.name) {
        FinanceSyncWorker(
            appContext = appContext,
            workerParameters = workerParameters,
            syncCoordinator = coordinatorProvider(),
        )
    } else {
        null
    }
}
