package com.zagirlek.ya_money.sync

import android.content.Context
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
        val result = syncCoordinator.sync()
        if (result.needsRetry) Result.retry() else Result.success()
    } catch (error: CancellationException) {
        throw error
    } catch (error: FinanceNetworkException.Network) {
        Result.retry()
    } catch (error: FinanceNetworkException.ServerFailure) {
        Result.retry()
    } catch (error: Exception) {
        Result.failure()
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
