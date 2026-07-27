package com.zagirlek.ya_money

import android.app.Application
import androidx.work.Configuration
import com.zagirlek.ya_money.connectivity.AndroidNetworkMonitor
import com.zagirlek.ya_money.di.AppDependencies
import com.zagirlek.ya_money.sync.FinanceSyncWorkerFactory
import com.zagirlek.ya_money.sync.FinanceWorkScheduler

class YaMoneyApplication : Application(), Configuration.Provider {
    private val workScheduler by lazy {
        FinanceWorkScheduler(applicationContext)
    }

    val networkMonitor by lazy {
        AndroidNetworkMonitor(
            context = applicationContext,
            onNetworkAvailable = workScheduler::enqueueImmediate,
        )
    }

    val dependencies: AppDependencies by lazy {
        AppDependencies(
            context = applicationContext,
            onSyncRequested = workScheduler::enqueueImmediate,
        )
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(
                FinanceSyncWorkerFactory {
                    dependencies.financeSyncCoordinator
                },
            )
            .build()

    override fun onCreate() {
        super.onCreate()
        networkMonitor
        workScheduler.enqueueImmediate()
        workScheduler.schedulePeriodic()
    }
}
