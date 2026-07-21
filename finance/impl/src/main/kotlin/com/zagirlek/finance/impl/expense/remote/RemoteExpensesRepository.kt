package com.zagirlek.finance.impl.expense.remote

import com.zagirlek.finance.api.account.AccountsRepository
import com.zagirlek.finance.api.expense.Expense
import com.zagirlek.finance.api.expense.ExpensesRepository
import com.zagirlek.finance.api.transaction.TransactionPeriod
import com.zagirlek.finance.impl.network.FinanceHttpClient
import com.zagirlek.finance.impl.transaction.remote.RemoteTransactionHistoryLoader
import com.zagirlek.finance.impl.transaction.remote.toExpense

class RemoteExpensesRepository(
    accountsRepository: AccountsRepository,
    httpClient: FinanceHttpClient,
) : ExpensesRepository {
    private val historyLoader = RemoteTransactionHistoryLoader(accountsRepository, httpClient)

    override suspend fun getExpenses(period: TransactionPeriod): List<Expense> = historyLoader.load(period)
        .asSequence()
        .filter { !it.category.isIncome }
        .map { it.toExpense() }
        .sortedByDescending(Expense::occurredAt)
        .toList()
}
