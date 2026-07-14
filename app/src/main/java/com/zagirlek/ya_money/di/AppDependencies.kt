package com.zagirlek.ya_money.di

import com.zagirlek.finance.api.account.AccountsRepository
import com.zagirlek.finance.api.expense.ExpensesRepository
import com.zagirlek.finance.api.income.IncomesRepository
import com.zagirlek.finance.impl.account.FakeAccountsRepository
import com.zagirlek.finance.impl.expense.FakeExpensesRepository
import com.zagirlek.finance.impl.income.FakeIncomesRepository

class AppDependencies {
    val accountsRepository: AccountsRepository = FakeAccountsRepository()
    val expensesRepository: ExpensesRepository = FakeExpensesRepository()
    val incomesRepository: IncomesRepository = FakeIncomesRepository()
}
