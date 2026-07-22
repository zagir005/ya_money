package com.zagirlek.analytics

import com.zagirlek.analytics.ui.summary.AnalyticsCategorySummary
import com.zagirlek.finance.api.account.Account
import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.transaction.TransactionHistoryEntry
import com.zagirlek.finance.api.transaction.TransactionPeriod
import com.zagirlek.finance.api.transaction.TransactionType
import com.zagirlek.ui.mvi.Effect
import com.zagirlek.ui.mvi.Intent
import com.zagirlek.ui.mvi.Mutation
import com.zagirlek.ui.mvi.MviReducer
import com.zagirlek.ui.mvi.State
import java.time.LocalDate

sealed interface AnalyticsState : State {
    data object Loading : AnalyticsState

    data class Content(
        val period: TransactionPeriod,
        val transactions: List<TransactionHistoryEntry>,
        val accounts: List<Account>,
        val filters: AnalyticsFilters,
        val summary: AnalyticsSummaryUi,
        val transactionItems: List<AnalyticsTransactionItemUi>,
        val filterOptions: AnalyticsFilterOptions,
        val isRefreshing: Boolean = false,
    ) : AnalyticsState

    data class Empty(
        val period: TransactionPeriod,
        val accounts: List<Account>,
        val filters: AnalyticsFilters,
        val summary: AnalyticsSummaryUi,
        val filterOptions: AnalyticsFilterOptions,
    ) : AnalyticsState

    data class Error(val message: String) : AnalyticsState
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
    data object Loading : AnalyticsMutation

    data class Content(
        val period: TransactionPeriod,
        val transactions: List<TransactionHistoryEntry>,
        val accounts: List<Account>,
        val filters: AnalyticsFilters,
        val summary: AnalyticsSummaryUi,
        val transactionItems: List<AnalyticsTransactionItemUi>,
        val filterOptions: AnalyticsFilterOptions,
    ) : AnalyticsMutation

    data class Empty(
        val period: TransactionPeriod,
        val accounts: List<Account>,
        val filters: AnalyticsFilters,
        val summary: AnalyticsSummaryUi,
        val filterOptions: AnalyticsFilterOptions,
    ) : AnalyticsMutation

    data class Error(val message: String) : AnalyticsMutation
    data object Refreshing : AnalyticsMutation
    data class RefreshFailed(val message: String) : AnalyticsMutation
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
        AnalyticsMutation.Loading -> AnalyticsState.Loading
        is AnalyticsMutation.Content -> AnalyticsState.Content(
            period = mutation.period,
            transactions = mutation.transactions,
            accounts = mutation.accounts,
            filters = mutation.filters,
            summary = mutation.summary,
            transactionItems = mutation.transactionItems,
            filterOptions = mutation.filterOptions,
        )
        is AnalyticsMutation.Empty -> AnalyticsState.Empty(
            period = mutation.period,
            accounts = mutation.accounts,
            filters = mutation.filters,
            summary = mutation.summary,
            filterOptions = mutation.filterOptions,
        )
        is AnalyticsMutation.Error -> AnalyticsState.Error(mutation.message)
        AnalyticsMutation.Refreshing -> (state as? AnalyticsState.Content)?.copy(isRefreshing = true) ?: state
        is AnalyticsMutation.RefreshFailed -> (state as? AnalyticsState.Content)?.copy(isRefreshing = false)
            ?: AnalyticsState.Error(mutation.message)
    }
}
