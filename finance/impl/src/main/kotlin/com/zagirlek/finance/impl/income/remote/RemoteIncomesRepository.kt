package com.zagirlek.finance.impl.income.remote

import com.zagirlek.finance.api.account.AccountsRepository
import com.zagirlek.finance.api.income.Income
import com.zagirlek.finance.api.income.IncomesRepository
import com.zagirlek.finance.api.transaction.TransactionPeriod
import com.zagirlek.finance.impl.network.FinanceHttpClient
import com.zagirlek.finance.impl.transaction.remote.RemoteTransactionHistoryLoader
import com.zagirlek.finance.impl.transaction.remote.toIncome

class RemoteIncomesRepository(
    accountsRepository: AccountsRepository,
    httpClient: FinanceHttpClient,
) : IncomesRepository {
    private val historyLoader = RemoteTransactionHistoryLoader(accountsRepository, httpClient)

    override suspend fun getIncomes(period: TransactionPeriod): List<Income> = historyLoader.load(period)
        .asSequence()
        .filter { it.category.isIncome }
        .map { it.toIncome() }
        .sortedByDescending(Income::occurredAt)
        .toList()
}
