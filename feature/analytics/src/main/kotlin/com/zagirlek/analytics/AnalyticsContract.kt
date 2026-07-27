package com.zagirlek.analytics

import com.zagirlek.analytics.ui.summary.AnalyticsCategorySummary
import com.zagirlek.finance.api.account.Account
import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.error.NetworkError
import com.zagirlek.finance.api.transaction.Transaction
import com.zagirlek.finance.api.transaction.TransactionPeriod
import com.zagirlek.finance.api.transaction.TransactionType
import com.zagirlek.ui.mvi.Effect
import com.zagirlek.ui.mvi.Intent
import com.zagirlek.ui.mvi.Mutation
import com.zagirlek.ui.mvi.MviReducer
import com.zagirlek.ui.mvi.State
import java.time.LocalDate

sealed interface AnalyticsState : State {
    val period: TransactionPeriod
    val filters: AnalyticsFilters

    data class Loading(
        override val period: TransactionPeriod,
        override val filters: AnalyticsFilters,
    ) : AnalyticsState

    data class Content(
        override val period: TransactionPeriod,
        val allTransactions: List<Transaction>,
        val transactions: List<Transaction>,
        val accounts: List<Account>,
        override val filters: AnalyticsFilters,
        val summary: AnalyticsSummaryUi,
        val transactionItems: List<AnalyticsTransactionItemUi>,
        val filterOptions: AnalyticsFilterOptions,
        val isRefreshing: Boolean = false,
        val historyError: NetworkError? = null,
    ) : AnalyticsState

    data class Empty(
        override val period: TransactionPeriod,
        val allTransactions: List<Transaction>,
        val accounts: List<Account>,
        override val filters: AnalyticsFilters,
        val summary: AnalyticsSummaryUi,
        val filterOptions: AnalyticsFilterOptions,
        val isRefreshing: Boolean = false,
        val historyError: NetworkError? = null,
    ) : AnalyticsState

    data class Error(
        val error: NetworkError,
        override val period: TransactionPeriod,
        override val filters: AnalyticsFilters,
    ) : AnalyticsState
}

sealed interface AnalyticsIntent : Intent {
    data object BackClicked : AnalyticsIntent
    data object RetryClicked : AnalyticsIntent
    data object RefreshRequested : AnalyticsIntent
    data object TypeFilterClicked : AnalyticsIntent
    data object PeriodFilterClicked : AnalyticsIntent
    data object CustomPeriodClicked : AnalyticsIntent
    data object CategoryFilterClicked : AnalyticsIntent
    data object AccountFilterClicked : AnalyticsIntent
    data object ChartClicked : AnalyticsIntent
    data class TypeApplied(val type: TransactionType?) : AnalyticsIntent
    data class PeriodPresetApplied(val preset: AnalyticsPeriodPreset) : AnalyticsIntent
    data class PeriodApplied(val period: TransactionPeriod) : AnalyticsIntent
    data class CategoriesApplied(val categoryIds: Set<Int>) : AnalyticsIntent
    data class AccountApplied(val accountId: AccountId?) : AnalyticsIntent
}

sealed interface AnalyticsMutation : Mutation {
    data class Loading(
        val period: TransactionPeriod,
        val filters: AnalyticsFilters,
    ) : AnalyticsMutation

    data class Content(
        val period: TransactionPeriod,
        val allTransactions: List<Transaction>,
        val transactions: List<Transaction>,
        val accounts: List<Account>,
        val filters: AnalyticsFilters,
        val summary: AnalyticsSummaryUi,
        val transactionItems: List<AnalyticsTransactionItemUi>,
        val filterOptions: AnalyticsFilterOptions,
    ) : AnalyticsMutation

    data class Empty(
        val period: TransactionPeriod,
        val allTransactions: List<Transaction>,
        val accounts: List<Account>,
        val filters: AnalyticsFilters,
        val summary: AnalyticsSummaryUi,
        val filterOptions: AnalyticsFilterOptions,
    ) : AnalyticsMutation

    data class Error(
        val error: NetworkError,
        val period: TransactionPeriod,
        val filters: AnalyticsFilters,
    ) : AnalyticsMutation
    data class FiltersUpdated(val filters: AnalyticsFilters) : AnalyticsMutation
    data object Refreshing : AnalyticsMutation
    data object RefreshCompleted : AnalyticsMutation
    data class RefreshFailed(val error: NetworkError) : AnalyticsMutation
}

sealed interface AnalyticsEffect : Effect {
    data class ShowFilterSheet(val sheet: AnalyticsFilterSheet) : AnalyticsEffect

    data object ShowChartDetails : AnalyticsEffect
}

enum class AnalyticsFilterSheet {
    Type,
    Period,
    Calendar,
    Categories,
    Account,
}

enum class AnalyticsPeriodPreset {
    Week,
    Month,
    Quarter,
    Year;

    fun toPeriod(endDate: LocalDate): TransactionPeriod = TransactionPeriod(
        startDate = when (this) {
            Week -> endDate.minusDays(6)
            Month -> endDate.minusMonths(1).plusDays(1)
            Quarter -> endDate.minusMonths(3).plusDays(1)
            Year -> endDate.minusYears(1).plusDays(1)
        },
        endDate = endDate,
    )
}

data class AnalyticsFilters(
    val type: TransactionType? = TransactionType.Expense,
    val categoryIds: Set<Int>? = null,
    val accountId: AccountId? = null,
)

data class AnalyticsSummaryUi(
    val total: String,
    val categories: List<AnalyticsCategorySummary>,
)

data class AnalyticsFilterOptions(
    val categories: List<AnalyticsCategoryOptionUi>,
    val accounts: List<AnalyticsAccountOptionUi>,
)

data class AnalyticsCategoryOptionUi(
    val id: Int,
    val name: String,
    val emoji: String,
)

data class AnalyticsAccountOptionUi(
    val id: AccountId,
    val name: String,
    val emoji: String,
)

data class AnalyticsTransactionItemUi(
    val id: String,
    val title: String,
    val subtitle: String,
    val emoji: String,
    val amount: String,
)

object AnalyticsReducer : MviReducer<AnalyticsState, AnalyticsMutation> {
    override fun reduce(
        state: AnalyticsState,
        mutation: AnalyticsMutation,
    ): AnalyticsState = when (mutation) {
        is AnalyticsMutation.Loading -> AnalyticsState.Loading(
            period = mutation.period,
            filters = mutation.filters,
        )
        is AnalyticsMutation.Content -> AnalyticsState.Content(
            period = mutation.period,
            allTransactions = mutation.allTransactions,
            transactions = mutation.transactions,
            accounts = mutation.accounts,
            filters = mutation.filters,
            summary = mutation.summary,
            transactionItems = mutation.transactionItems,
            filterOptions = mutation.filterOptions,
        )
        is AnalyticsMutation.Empty -> AnalyticsState.Empty(
            period = mutation.period,
            allTransactions = mutation.allTransactions,
            accounts = mutation.accounts,
            filters = mutation.filters,
            summary = mutation.summary,
            filterOptions = mutation.filterOptions,
        )
        is AnalyticsMutation.Error -> AnalyticsState.Error(
            error = mutation.error,
            period = mutation.period,
            filters = mutation.filters,
        )
        is AnalyticsMutation.FiltersUpdated -> when (state) {
            is AnalyticsState.Loading -> state.copy(filters = mutation.filters)
            is AnalyticsState.Content -> state.copy(filters = mutation.filters)
            is AnalyticsState.Empty -> state.copy(filters = mutation.filters)
            is AnalyticsState.Error -> state.copy(filters = mutation.filters)
        }
        AnalyticsMutation.Refreshing -> when (state) {
            is AnalyticsState.Content -> state.copy(isRefreshing = true, historyError = null)
            is AnalyticsState.Empty -> state.copy(isRefreshing = true, historyError = null)
            else -> state
        }
        AnalyticsMutation.RefreshCompleted -> when (state) {
            is AnalyticsState.Content -> state.copy(isRefreshing = false, historyError = null)
            is AnalyticsState.Empty -> state.copy(isRefreshing = false, historyError = null)
            else -> state
        }
        is AnalyticsMutation.RefreshFailed -> when (state) {
            is AnalyticsState.Content -> state.copy(
                isRefreshing = false,
                historyError = mutation.error,
            )
            is AnalyticsState.Empty -> state.copy(
                isRefreshing = false,
                historyError = mutation.error,
            )
            else -> state
        }
    }
}
