package com.zagirlek.analytics

import com.zagirlek.finance.api.account.Account
import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.transaction.TransactionHistoryEntry
import com.zagirlek.finance.api.transaction.TransactionPeriod
import com.zagirlek.finance.api.transaction.TransactionType
import com.zagirlek.ui.mvi.Effect
import com.zagirlek.ui.mvi.Intent
import com.zagirlek.ui.mvi.Mutation
import com.zagirlek.ui.mvi.MviReducer
import com.zagirlek.ui.mvi.RetryableErrorEffect
import com.zagirlek.ui.mvi.State

sealed interface AnalyticsState : State {
    data object Loading : AnalyticsState

    data class Content(
        val period: TransactionPeriod,
        val transactions: List<TransactionHistoryEntry>,
        val accounts: List<Account>,
        val filters: AnalyticsFilters,
        val isRefreshing: Boolean = false,
    ) : AnalyticsState

    data class Empty(
        val period: TransactionPeriod,
        val accounts: List<Account>,
        val filters: AnalyticsFilters,
    ) : AnalyticsState

    data class Error(val message: String) : AnalyticsState
}

sealed interface AnalyticsIntent : Intent {
    data object BackClicked : AnalyticsIntent
    data object RetryClicked : AnalyticsIntent
    data object RefreshRequested : AnalyticsIntent
    data object TypeFilterClicked : AnalyticsIntent
    data object PeriodFilterClicked : AnalyticsIntent
    data object CategoryFilterClicked : AnalyticsIntent
    data object AccountFilterClicked : AnalyticsIntent
    data class TypeApplied(val type: TransactionType?) : AnalyticsIntent
    data class PeriodApplied(val period: TransactionPeriod) : AnalyticsIntent
    data class CategoriesApplied(val categoryIds: Set<Int>) : AnalyticsIntent
    data class AccountApplied(val accountId: AccountId?) : AnalyticsIntent
}

sealed interface AnalyticsMutation : Mutation {
    data object Loading : AnalyticsMutation

    data class Content(
        val period: TransactionPeriod,
        val transactions: List<TransactionHistoryEntry>,
        val accounts: List<Account>,
        val filters: AnalyticsFilters,
    ) : AnalyticsMutation

    data class Empty(
        val period: TransactionPeriod,
        val accounts: List<Account>,
        val filters: AnalyticsFilters,
    ) : AnalyticsMutation

    data class Error(val message: String) : AnalyticsMutation
    data class FiltersChanged(val filters: AnalyticsFilters) : AnalyticsMutation
    data object Refreshing : AnalyticsMutation
    data class RefreshFailed(val message: String) : AnalyticsMutation
}

sealed interface AnalyticsEffect : Effect {
    data class ShowRetryableError(
        override val message: String,
    ) : AnalyticsEffect, RetryableErrorEffect

    data class ShowFilterSheet(val sheet: AnalyticsFilterSheet) : AnalyticsEffect
}

enum class AnalyticsFilterSheet {
    Type,
    Period,
    Categories,
    Account,
}

data class AnalyticsFilters(
    val type: TransactionType? = TransactionType.Expense,
    val categoryIds: Set<Int> = emptySet(),
    val accountId: AccountId? = null,
)

object AnalyticsReducer : MviReducer<AnalyticsState, AnalyticsMutation> {
    override fun reduce(
        state: AnalyticsState,
        mutation: AnalyticsMutation,
    ): AnalyticsState = when (mutation) {
        AnalyticsMutation.Loading -> AnalyticsState.Loading
        is AnalyticsMutation.Content -> AnalyticsState.Content(
            period = mutation.period,
            transactions = mutation.transactions,
            accounts = mutation.accounts,
            filters = mutation.filters,
        )
        is AnalyticsMutation.Empty -> AnalyticsState.Empty(
            period = mutation.period,
            accounts = mutation.accounts,
            filters = mutation.filters,
        )
        is AnalyticsMutation.Error -> AnalyticsState.Error(mutation.message)
        is AnalyticsMutation.FiltersChanged -> when (state) {
            is AnalyticsState.Content -> state.copy(filters = mutation.filters)
            is AnalyticsState.Empty -> state.copy(filters = mutation.filters)
            else -> state
        }
        AnalyticsMutation.Refreshing -> (state as? AnalyticsState.Content)?.copy(isRefreshing = true) ?: state
        is AnalyticsMutation.RefreshFailed -> (state as? AnalyticsState.Content)?.copy(isRefreshing = false)
            ?: AnalyticsState.Error(mutation.message)
    }
}
