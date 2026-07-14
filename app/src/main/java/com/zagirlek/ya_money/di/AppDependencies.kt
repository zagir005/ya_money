package com.zagirlek.ya_money.di

import com.zagirlek.finance.api.expense.ExpensesRepository
import com.zagirlek.finance.impl.expense.FakeExpensesRepository

class AppDependencies {
    val expensesRepository: ExpensesRepository = FakeExpensesRepository()
}
