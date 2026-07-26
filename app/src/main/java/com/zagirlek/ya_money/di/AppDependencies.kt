package com.zagirlek.ya_money.di

import android.content.Context
import com.zagirlek.finance.api.account.AccountsRepository
import com.zagirlek.finance.api.category.CategoriesRepository
import com.zagirlek.finance.api.transaction.TransactionHistoryRepository
import com.zagirlek.finance.api.transaction.TransactionsRepository
import com.zagirlek.finance.impl.FinanceDataGraph
import com.zagirlek.finance.impl.network.FinanceHttpClient
import com.zagirlek.finance.impl.transaction.remote.RemoteTransactionHistoryRepository
import com.zagirlek.ya_money.BuildConfig
import java.io.Closeable

class AppDependencies(
    context: Context,
    onSyncRequested: () -> Unit,
) : Closeable {
    private val financeHttpClient = FinanceHttpClient(BuildConfig.FINANCE_API_TOKEN)
    private val financeDataGraph = FinanceDataGraph(
        context = context,
        httpClient = financeHttpClient,
        onSyncRequested = onSyncRequested,
    )

    val accountsRepository: AccountsRepository = financeDataGraph.accountsRepository
    val categoriesRepository: CategoriesRepository = financeDataGraph.categoriesRepository
    val transactionsRepository: TransactionsRepository = financeDataGraph.transactionsRepository
    val financeSyncCoordinator = financeDataGraph.syncCoordinator
    val transactionHistoryRepository: TransactionHistoryRepository = RemoteTransactionHistoryRepository(
        accountsRepository = accountsRepository,
        httpClient = financeHttpClient,
    )

    override fun close() {
        financeDataGraph.close()
        financeHttpClient.close()
    }
}
