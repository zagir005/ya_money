package com.zagirlek.finance.api.expense

import com.zagirlek.finance.api.transaction.TransactionPeriod

@Deprecated("Use TransactionsRepository.")
interface ExpensesRepository {
    suspend fun getExpenses(period: TransactionPeriod): List<Expense>
}
