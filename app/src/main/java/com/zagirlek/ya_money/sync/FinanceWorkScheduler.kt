package com.zagirlek.ya_money.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class FinanceWorkScheduler(
    context: Context,
) {
    private val workManager = WorkManager.getInstance(context.applicationContext)
    private val networkConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun enqueueImmediate() {
        val request = OneTimeWorkRequestBuilder<FinanceSyncWorker>()
            .setConstraints(networkConstraints)
            .setBackoffCriteria(
                backoffPolicy = BackoffPolicy.EXPONENTIAL,
                backoffDelay = MinimumBackoffSeconds,
                timeUnit = TimeUnit.SECONDS,
            )
            .build()
        workManager.enqueueUniqueWork(
            ImmediateWorkName,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request,
        )
    }

    fun schedulePeriodic() {
        val request = PeriodicWorkRequestBuilder<FinanceSyncWorker>(
            repeatInterval = PeriodicIntervalHours,
            repeatIntervalTimeUnit = TimeUnit.HOURS,
        )
            .setConstraints(networkConstraints)
            .build()
        workManager.enqueueUniquePeriodicWork(
            PeriodicWorkName,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    private companion object {
        const val ImmediateWorkName = "finance-immediate-sync"
        const val PeriodicWorkName = "finance-periodic-sync"
        const val PeriodicIntervalHours = 2L
        const val MinimumBackoffSeconds = 10L
    }
}
