package com.zagirlek.transactions.expenses

import com.zagirlek.finance.api.expense.ExpenseId
import com.zagirlek.ui.formatter.Money
import com.zagirlek.ui.mvi.Effect
import com.zagirlek.ui.mvi.Intent
import com.zagirlek.ui.mvi.Mutation
import com.zagirlek.ui.mvi.MviReducer
import com.zagirlek.ui.mvi.State

data class ExpenseItemUi(
    val id: ExpenseId,
    val lead: String,
    val content: String,
    val trail: String,
    val trailTag: String,
)

sealed interface ExpensesState : State {
    data object Loading : ExpensesState
    data object Empty : ExpensesState
    data object Error : ExpensesState

    data class Content(
        val total: Money,
        val items: List<ExpenseItemUi>,
    ) : ExpensesState
}

sealed interface ExpensesIntent : Intent {
    data class ExpenseClicked(val id: ExpenseId) : ExpensesIntent
    data object DateClicked : ExpensesIntent
    data object AnalyticsClicked : ExpensesIntent
    data object SettingsClicked : ExpensesIntent
    data object RetryClicked : ExpensesIntent
}

sealed interface ExpensesMutation : Mutation {
    data object Loading : ExpensesMutation
    data object Empty : ExpensesMutation
    data object Error : ExpensesMutation

    data class Content(
        val total: Money,
        val items: List<ExpenseItemUi>,
    ) : ExpensesMutation
}

sealed interface ExpensesEffect : Effect

object ExpensesReducer : MviReducer<ExpensesState, ExpensesMutation> {
    override fun reduce(
        state: ExpensesState,
        mutation: ExpensesMutation,
    ): ExpensesState = when (mutation) {
        ExpensesMutation.Loading -> ExpensesState.Loading
        ExpensesMutation.Empty -> ExpensesState.Empty
        ExpensesMutation.Error -> ExpensesState.Error
        is ExpensesMutation.Content -> ExpensesState.Content(
            total = mutation.total,
            items = mutation.items,
        )
    }
}
