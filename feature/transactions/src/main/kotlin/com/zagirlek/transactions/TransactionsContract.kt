package com.zagirlek.transactions

import com.zagirlek.finance.api.error.NetworkError
import com.zagirlek.ui.mvi.Effect
import com.zagirlek.ui.mvi.Intent
import com.zagirlek.ui.mvi.Mutation
import com.zagirlek.ui.mvi.MviReducer
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
    data class Error(val error: NetworkError) : TransactionsState

    data class Content(
        val total: String,
        val items: List<TransactionItemUi>,
        val isRefreshing: Boolean = false,
        val refreshError: NetworkError? = null,
    ) : TransactionsState
}

sealed interface TransactionsIntent : Intent {
    data class TransactionClicked(val id: String) : TransactionsIntent
    data object DateClicked : TransactionsIntent
    data object AnalyticsClicked : TransactionsIntent
    data object SettingsClicked : TransactionsIntent
    data object AddClicked : TransactionsIntent
    data object RetryClicked : TransactionsIntent
    data object RefreshRequested : TransactionsIntent
}

sealed interface TransactionsMutation : Mutation {
    data object Loading : TransactionsMutation
    data object Empty : TransactionsMutation
    data class Error(val error: NetworkError) : TransactionsMutation
    data object Refreshing : TransactionsMutation
    data class RefreshFailed(val error: NetworkError) : TransactionsMutation

    data class Content(
        val total: String,
        val items: List<TransactionItemUi>,
    ) : TransactionsMutation
}

sealed interface TransactionsEffect : Effect

object TransactionsReducer : MviReducer<TransactionsState, TransactionsMutation> {
    override fun reduce(
        state: TransactionsState,
        mutation: TransactionsMutation,
    ): TransactionsState = when (mutation) {
        TransactionsMutation.Loading -> TransactionsState.Loading
        TransactionsMutation.Empty -> TransactionsState.Empty
        is TransactionsMutation.Error -> TransactionsState.Error(mutation.error)
        TransactionsMutation.Refreshing -> (state as? TransactionsState.Content)?.copy(
            isRefreshing = true,
            refreshError = null,
        ) ?: state
        is TransactionsMutation.RefreshFailed -> (state as? TransactionsState.Content)?.copy(isRefreshing = false)
            ?.copy(refreshError = mutation.error)
            ?: TransactionsState.Error(mutation.error)
        is TransactionsMutation.Content -> TransactionsState.Content(
            total = mutation.total,
            items = mutation.items,
        )
    }
}
