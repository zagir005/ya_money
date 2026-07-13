package com.zagirlek.finance.impl.expense

import com.zagirlek.finance.api.expense.ExpensesRepository
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides

/** Metro bindings for the fake implementation used by the first iteration. */
@BindingContainer
object ExpensesBindings {
    @Provides
    fun provideExpensesRepository(): ExpensesRepository = FakeExpensesRepository()
}
