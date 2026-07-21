package com.zagirlek.ya_money.di

import com.zagirlek.finance.impl.account.remote.RemoteAccountsRepository
import com.zagirlek.finance.api.account.AccountsRepository
import com.zagirlek.finance.api.expense.ExpensesRepository
import com.zagirlek.finance.api.income.IncomesRepository
import com.zagirlek.finance.api.transaction.TransactionHistoryRepository
import com.zagirlek.finance.impl.expense.remote.RemoteExpensesRepository
import com.zagirlek.finance.impl.income.remote.RemoteIncomesRepository
import com.zagirlek.finance.impl.network.FinanceHttpClient
import com.zagirlek.finance.impl.transaction.remote.RemoteTransactionHistoryRepository
import com.zagirlek.ya_money.BuildConfig

class AppDependencies {
    private val financeHttpClient = FinanceHttpClient(BuildConfig.FINANCE_API_TOKEN)

    val accountsRepository: AccountsRepository = RemoteAccountsRepository(financeHttpClient)
    val expensesRepository: ExpensesRepository = RemoteExpensesRepository(
        accountsRepository = accountsRepository,
        httpClient = financeHttpClient,
    )
    val incomesRepository: IncomesRepository = RemoteIncomesRepository(
        accountsRepository = accountsRepository,
        httpClient = financeHttpClient,
    )
    val transactionHistoryRepository: TransactionHistoryRepository = RemoteTransactionHistoryRepository(
        accountsRepository = accountsRepository,
        httpClient = financeHttpClient,
    )
}
