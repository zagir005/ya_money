package com.zagirlek.analytics

import com.arkivanov.decompose.ComponentContext
import com.zagirlek.finance.api.account.AccountsRepository
import com.zagirlek.finance.api.transaction.TransactionHistoryRepository
import com.zagirlek.finance.api.transaction.TransactionPeriod
import com.zagirlek.ui.cmp.MviComponent
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class DefaultAnalyticsComponent(
    componentContext: ComponentContext,
    private val transactionHistoryRepository: TransactionHistoryRepository,
    private val accountsRepository: AccountsRepository,
    private val onBackRequested: () -> Unit,
) : MviComponent<AnalyticsState, AnalyticsMutation, AnalyticsIntent, AnalyticsReducer>(
    reducer = AnalyticsReducer,
    componentContext = componentContext,
), AnalyticsComponent {

    private val mutableState = MutableStateFlow<AnalyticsState>(AnalyticsState.Loading)
    private val mutableEffects = MutableSharedFlow<AnalyticsEffect>(extraBufferCapacity = 1)
    private var loadJob: Job? = null
    private var period = TransactionPeriod.currentMonthToDate()
    private var filters = AnalyticsFilters()

    override val state: StateFlow<AnalyticsState> = mutableState.asStateFlow()
    override val effects: Flow<AnalyticsEffect> = mutableEffects.asSharedFlow()

    init {
        loadAnalytics()
    }

    override fun accept(intent: AnalyticsIntent) {
        when (intent) {
            AnalyticsIntent.BackClicked -> onBackRequested()
            AnalyticsIntent.RetryClicked -> loadAnalytics()
            AnalyticsIntent.RefreshRequested -> loadAnalytics(isRefresh = true)
            AnalyticsIntent.TypeFilterClicked -> showFilterSheet(AnalyticsFilterSheet.Type)
            AnalyticsIntent.PeriodFilterClicked -> showFilterSheet(AnalyticsFilterSheet.Period)
            AnalyticsIntent.CategoryFilterClicked -> showFilterSheet(AnalyticsFilterSheet.Categories)
            AnalyticsIntent.AccountFilterClicked -> showFilterSheet(AnalyticsFilterSheet.Account)
            is AnalyticsIntent.TypeApplied -> updateFilters(filters.copy(type = intent.type))
            is AnalyticsIntent.PeriodApplied -> loadAnalytics(period = intent.period)
            is AnalyticsIntent.CategoriesApplied -> updateFilters(filters.copy(categoryIds = intent.categoryIds))
            is AnalyticsIntent.AccountApplied -> updateFilters(filters.copy(accountId = intent.accountId))
        }
    }

    private fun loadAnalytics(
        period: TransactionPeriod = this.period,
        isRefresh: Boolean = false,
    ) {
        val periodChanged = period != this.period
        if (periodChanged) {
            this.period = period
            loadJob?.cancel()
        } else if (loadJob?.isActive == true) {
            return
        }

        val isContentRefresh = isRefresh && mutableState.value is AnalyticsState.Content
        if (isContentRefresh) {
            AnalyticsMutation.Refreshing.reduce(mutableState)
        } else {
            AnalyticsMutation.Loading.reduce(mutableState)
        }

        loadJob = ioScope.launch {
            val mutation = try {
                val accounts = accountsRepository.getAccounts()
                val history = transactionHistoryRepository.getHistory(period)
                    .sortedByDescending { it.occurredAt }

                if (history.isEmpty()) {
                    AnalyticsMutation.Empty(period, accounts, filters)
                } else {
                    AnalyticsMutation.Content(period, history, accounts, filters)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                if (isContentRefresh) {
                    AnalyticsMutation.RefreshFailed(error.toErrorMessage())
                } else {
                    AnalyticsMutation.Error(error.toErrorMessage())
                }
            }

            componentScope.launch {
                if (mutation is AnalyticsMutation.Error) {
                    mutableEffects.tryEmit(AnalyticsEffect.ShowRetryableError(mutation.message))
                }
                if (mutation is AnalyticsMutation.RefreshFailed) {
                    mutableEffects.tryEmit(AnalyticsEffect.ShowRetryableError(mutation.message))
                }
                mutation.reduce(mutableState)
            }
        }
    }

    private fun updateFilters(updatedFilters: AnalyticsFilters) {
        filters = updatedFilters
        AnalyticsMutation.FiltersChanged(updatedFilters).reduce(mutableState)
    }

    private fun showFilterSheet(sheet: AnalyticsFilterSheet) {
        mutableEffects.tryEmit(AnalyticsEffect.ShowFilterSheet(sheet))
    }

    private fun Exception.toErrorMessage(): String = message ?: DEFAULT_ERROR_MESSAGE

    private companion object {
        const val DEFAULT_ERROR_MESSAGE = "Не удалось загрузить аналитику."
    }
}
