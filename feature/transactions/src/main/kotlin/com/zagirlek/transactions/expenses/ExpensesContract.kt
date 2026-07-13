package com.zagirlek.transactions.expenses

import com.zagirlek.finance.api.expense.ExpenseId
import com.zagirlek.ui.listitem.ListItem

sealed interface ExpensesState {
    data object Loading : ExpensesState

    data class Content(
        val summaryTitle: String,
        val total: String,
        val items: List<ListItem>,
    ) : ExpensesState

    data object Empty : ExpensesState

    data class Error(val message: String) : ExpensesState
}

sealed interface ExpensesIntent {
    data class ExpenseClicked(val id: ExpenseId) : ExpensesIntent
    data object RetryClicked : ExpensesIntent
}
