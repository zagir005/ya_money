package com.zagirlek.accounts

import com.zagirlek.finance.api.error.NetworkError
import com.zagirlek.ui.mvi.Effect
import com.zagirlek.ui.mvi.Intent
import com.zagirlek.ui.mvi.Mutation
import com.zagirlek.ui.mvi.MviReducer
import com.zagirlek.ui.mvi.State

data class AccountItemUi(
    val id: String,
    val lead: String,
    val content: String,
    val trail: String,
)

sealed interface AccountsState : State {
    data object Loading : AccountsState
    data object Empty : AccountsState
    data class Error(val error: NetworkError) : AccountsState

    data class Content(
        val total: String,
        val items: List<AccountItemUi>,
        val isRefreshing: Boolean = false,
        val refreshError: NetworkError? = null,
    ) : AccountsState
}

sealed interface AccountsIntent : Intent {
    data class AccountClicked(val id: String) : AccountsIntent
    data object DateClicked : AccountsIntent
    data object AnalyticsClicked : AccountsIntent
    data object SettingsClicked : AccountsIntent
    data object AddClicked : AccountsIntent
    data object RetryClicked : AccountsIntent
    data object RefreshRequested : AccountsIntent
}

sealed interface AccountsMutation : Mutation {
    data object Loading : AccountsMutation
    data object Empty : AccountsMutation
    data class Error(val error: NetworkError) : AccountsMutation
    data object Refreshing : AccountsMutation
    data class RefreshFailed(val error: NetworkError) : AccountsMutation

    data class Content(
        val total: String,
        val items: List<AccountItemUi>,
    ) : AccountsMutation
}

sealed interface AccountsEffect : Effect

object AccountsReducer : MviReducer<AccountsState, AccountsMutation> {
    override fun reduce(
        state: AccountsState,
        mutation: AccountsMutation,
    ): AccountsState = when (mutation) {
        AccountsMutation.Loading -> AccountsState.Loading
        AccountsMutation.Empty -> AccountsState.Empty
        is AccountsMutation.Error -> AccountsState.Error(mutation.error)
        AccountsMutation.Refreshing -> (state as? AccountsState.Content)?.copy(
            isRefreshing = true,
            refreshError = null,
        ) ?: state
        is AccountsMutation.RefreshFailed -> (state as? AccountsState.Content)?.copy(isRefreshing = false)
            ?.copy(refreshError = mutation.error)
            ?: AccountsState.Error(mutation.error)
        is AccountsMutation.Content -> AccountsState.Content(
            total = mutation.total,
            items = mutation.items,
        )
    }
}
