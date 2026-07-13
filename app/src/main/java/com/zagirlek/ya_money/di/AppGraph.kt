package com.zagirlek.ya_money.di

import com.zagirlek.finance.api.expense.ExpensesRepository
import com.zagirlek.finance.impl.expense.ExpensesBindings
import dev.zacsweers.metro.DependencyGraph

@DependencyGraph(bindingContainers = [ExpensesBindings::class])
interface AppGraph {
    val expensesRepository: ExpensesRepository
}
