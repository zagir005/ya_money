package com.zagirlek.finance.api.expense

/** Public contract consumed by the transactions feature. */
interface ExpensesRepository {
    fun getExpenses(): List<Expense>
}
