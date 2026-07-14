package com.zagirlek.finance.api.expense

interface ExpensesRepository {
    fun getExpenses(): List<Expense>
}
