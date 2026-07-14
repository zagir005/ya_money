package com.zagirlek.transactions.expenses

import com.zagirlek.finance.api.expense.ExpenseId

data class ExpenseItemUi(
    val id: ExpenseId,
    val lead: String,
    val content: String,
    val trail: String,
    val trailTag: String,
)

data class ExpensesStateContent(
    val total: String,
    val items: List<ExpenseItemUi>,
)

sealed interface ExpensesIntent {
    data class ExpenseClicked(val id: ExpenseId) : ExpensesIntent
    data object DateClicked : ExpensesIntent
    data object AnalyticsClicked : ExpensesIntent
    data object SettingsClicked : ExpensesIntent
}
