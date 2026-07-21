package com.zagirlek.transactions

import com.zagirlek.ui.mvi.Effect
import com.zagirlek.ui.mvi.Intent
import com.zagirlek.ui.mvi.Mutation
import com.zagirlek.ui.mvi.MviReducer
import com.zagirlek.ui.mvi.RetryableErrorEffect
import com.zagirlek.ui.mvi.State

data class TransactionItemUi(
    val id: String,
    val lead: String,
    val content: String,
    val trail: String,
)

sealed interface TransactionsState : State {
    data object Loading : TransactionsState
    data object Empty : TransactionsState
    data object Error : TransactionsState

    data class Content(
        val total: String,
        val items: List<TransactionItemUi>,
    ) : TransactionsState
}

sealed interface TransactionsIntent : Intent {
    data class TransactionClicked(val id: String) : TransactionsIntent
    data object DateClicked : TransactionsIntent
    data object AnalyticsClicked : TransactionsIntent
    data object SettingsClicked : TransactionsIntent
    data object AddClicked : TransactionsIntent
    data object RetryClicked : TransactionsIntent
}

sealed interface TransactionsMutation : Mutation {
    data object Loading : TransactionsMutation
    data object Empty : TransactionsMutation
    data object Error : TransactionsMutation

    data class Content(
        val total: String,
        val items: List<TransactionItemUi>,
    ) : TransactionsMutation
}

sealed interface TransactionsEffect : Effect {
    data object ShowRetryableError : TransactionsEffect, RetryableErrorEffect
}

object TransactionsReducer : MviReducer<TransactionsState, TransactionsMutation> {
    override fun reduce(
        state: TransactionsState,
        mutation: TransactionsMutation,
    ): TransactionsState = when (mutation) {
        TransactionsMutation.Loading -> TransactionsState.Loading
        TransactionsMutation.Empty -> TransactionsState.Empty
        TransactionsMutation.Error -> TransactionsState.Error
        is TransactionsMutation.Content -> TransactionsState.Content(
            total = mutation.total,
            items = mutation.items,
        )
    }
}
