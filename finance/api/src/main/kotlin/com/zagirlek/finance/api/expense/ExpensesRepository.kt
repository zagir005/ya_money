package com.zagirlek.finance.api.expense

interface ExpensesRepository {
    suspend fun getExpenses(): List<Expense>
}
