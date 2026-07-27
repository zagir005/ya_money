package com.zagirlek.ya_money.di

import android.content.Context
import com.zagirlek.finance.api.account.AccountsRepository
import com.zagirlek.finance.api.category.CategoriesRepository
import com.zagirlek.finance.api.sync.FinanceSyncStatusRepository
import com.zagirlek.finance.api.transaction.TransactionsRepository
import com.zagirlek.finance.impl.FinanceDataGraph
import com.zagirlek.finance.impl.network.FinanceHttpClient
import com.zagirlek.ya_money.BuildConfig
import java.io.Closeable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AppDependencies(
    context: Context,
    onSyncRequested: () -> Unit,
) : Closeable {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val financeHttpClient = FinanceHttpClient(BuildConfig.FINANCE_API_TOKEN)
    private val financeDataGraph = FinanceDataGraph(
        context = context,
        httpClient = financeHttpClient,
        onSyncRequested = onSyncRequested,
    )

    val accountsRepository: AccountsRepository = financeDataGraph.accountsRepository
    val categoriesRepository: CategoriesRepository = financeDataGraph.categoriesRepository
    val transactionsRepository: TransactionsRepository = financeDataGraph.transactionsRepository
    val syncStatusRepository: FinanceSyncStatusRepository =
        financeDataGraph.syncStatusRepository
    val financeSyncCoordinator = financeDataGraph.syncCoordinator

    fun retrySync() {
        applicationScope.launch {
            syncStatusRepository.retryFailedOperations()
        }
    }

    override fun close() {
        applicationScope.cancel()
        financeDataGraph.close()
        financeHttpClient.close()
    }
}
