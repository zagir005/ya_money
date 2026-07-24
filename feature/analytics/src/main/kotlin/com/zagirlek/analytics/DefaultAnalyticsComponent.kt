package com.zagirlek.analytics

import com.arkivanov.decompose.ComponentContext
import com.zagirlek.finance.api.account.AccountsRepository
import com.zagirlek.finance.api.error.toNetworkError
import com.zagirlek.finance.api.money.CurrencyCode
import com.zagirlek.finance.api.money.Money
import com.zagirlek.finance.api.transaction.TransactionHistoryEntry
import com.zagirlek.finance.api.transaction.TransactionHistoryRepository
import com.zagirlek.finance.api.transaction.TransactionPeriod
import com.zagirlek.ui.cmp.MviComponent
import com.zagirlek.ui.formatter.DefaultMoneyFormatter
import com.zagirlek.ui.formatter.MoneyFormatter
import com.zagirlek.ui.formatter.format
import com.zagirlek.analytics.ui.summary.AnalyticsCategorySummary
import com.zagirlek.analytics.ui.resolveAccountEmoji
import com.zagirlek.analytics.ui.resolveCategoryEmoji
import com.zagirlek.finance.api.account.Account
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.coroutineContext

class DefaultAnalyticsComponent(
    componentContext: ComponentContext,
    private val transactionHistoryRepository: TransactionHistoryRepository,
    private val accountsRepository: AccountsRepository,
    private val onBackRequested: () -> Unit,
    private val moneyFormatter: MoneyFormatter = DefaultMoneyFormatter(),
) : MviComponent<AnalyticsState, AnalyticsMutation, AnalyticsIntent, AnalyticsReducer>(
    reducer = AnalyticsReducer,
    componentContext = componentContext,
), AnalyticsComponent {

    private val mutableState = MutableStateFlow<AnalyticsState>(
        AnalyticsState.Loading(
            period = TransactionPeriod.currentMonthToDate(),
            filters = AnalyticsFilters(),
        ),
    )
    private val mutableEffects = MutableSharedFlow<AnalyticsEffect>(extraBufferCapacity = 1)
    private var loadJob: Job? = null
    private var filterJob: Job? = null

    override val state: StateFlow<AnalyticsState> = mutableState.asStateFlow()
    override val effects: Flow<AnalyticsEffect> = mutableEffects.asSharedFlow()

    init {
        loadAnalytics()
    }

    override fun accept(intent: AnalyticsIntent) {
        when (intent) {
            AnalyticsIntent.BackClicked -> onBackRequested()
            AnalyticsIntent.RetryClicked -> loadAnalytics(isRefresh = true)
            AnalyticsIntent.RefreshRequested -> loadAnalytics(isRefresh = true)
            AnalyticsIntent.TypeFilterClicked -> showFilterSheet(AnalyticsFilterSheet.Type)
            AnalyticsIntent.PeriodFilterClicked -> showFilterSheet(AnalyticsFilterSheet.Period)
            AnalyticsIntent.CustomPeriodClicked -> showFilterSheet(AnalyticsFilterSheet.Calendar)
            AnalyticsIntent.CategoryFilterClicked -> showFilterSheet(AnalyticsFilterSheet.Categories)
            AnalyticsIntent.AccountFilterClicked -> showFilterSheet(AnalyticsFilterSheet.Account)
            AnalyticsIntent.ChartClicked -> mutableEffects.tryEmit(AnalyticsEffect.ShowChartDetails)
            is AnalyticsIntent.TypeApplied -> updateFilters(
                mutableState.value.filters.copy(
                    type = intent.type,
                    categoryIds = null,
                ),
            )
            is AnalyticsIntent.PeriodPresetApplied -> loadAnalytics(
                period = intent.preset.toPeriod(LocalDate.now()),
            )
            is AnalyticsIntent.PeriodApplied -> loadAnalytics(period = intent.period)
            is AnalyticsIntent.CategoriesApplied -> updateFilters(
                mutableState.value.filters.copy(categoryIds = intent.categoryIds),
            )
            is AnalyticsIntent.AccountApplied -> updateFilters(
                mutableState.value.filters.copy(accountId = intent.accountId),
            )
        }
    }

    private fun loadAnalytics(
        period: TransactionPeriod = mutableState.value.period,
        isRefresh: Boolean = false,
    ) {
        val currentState = mutableState.value
        val periodChanged = period != currentState.period
        if (periodChanged) {
            loadJob?.cancel()
        } else if (loadJob?.isActive == true) {
            return
        }
        filterJob?.cancel()

        val isOverviewRefresh = isRefresh && (
            currentState is AnalyticsState.Content || currentState is AnalyticsState.Empty
        )
        if (isOverviewRefresh) {
            AnalyticsMutation.Refreshing.reduce(mutableState)
        } else {
            AnalyticsMutation.Loading(
                period = period,
                filters = currentState.filters,
            ).reduce(mutableState)
        }

        loadJob = ioScope.launch {
            val result = try {
                val accounts = accountsRepository.getAccounts()
                val history = transactionHistoryRepository.getHistory(period)
                    .sortedByDescending { it.occurredAt }

                AnalyticsLoadResult.Success(
                    transactions = history,
                    accounts = accounts,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                AnalyticsLoadResult.Failure(
                    mutation = if (isOverviewRefresh) {
                        AnalyticsMutation.RefreshFailed(error.toNetworkError())
                    } else {
                        AnalyticsMutation.Error(
                            error = error.toNetworkError(),
                            period = period,
                            filters = currentState.filters,
                        )
                    },
                )
            }

            componentScope.launch {
                when (result) {
                    is AnalyticsLoadResult.Success -> {
                        if (period != mutableState.value.period) return@launch

                        recalculateContent(
                            period = period,
                            filters = mutableState.value.filters,
                            allTransactions = result.transactions,
                            accounts = result.accounts,
                        )
                    }
                    is AnalyticsLoadResult.Failure -> {
                        result.mutation.reduce(mutableState)
                    }
                }
            }
        }
    }

    private fun updateFilters(updatedFilters: AnalyticsFilters) {
        val currentState = mutableState.value
        AnalyticsMutation.FiltersUpdated(updatedFilters).reduce(mutableState)

        when (currentState) {
            is AnalyticsState.Content -> recalculateContent(
                period = currentState.period,
                filters = updatedFilters,
                allTransactions = currentState.allTransactions,
                accounts = currentState.accounts,
            )
            is AnalyticsState.Empty -> recalculateContent(
                period = currentState.period,
                filters = updatedFilters,
                allTransactions = currentState.allTransactions,
                accounts = currentState.accounts,
            )
            else -> Unit
        }
    }

    private fun recalculateContent(
        period: TransactionPeriod,
        filters: AnalyticsFilters,
        allTransactions: List<TransactionHistoryEntry>,
        accounts: List<Account>,
    ) {
        filterJob?.cancel()

        val periodSnapshot = period
        val filtersSnapshot = filters
        val transactionsSnapshot = allTransactions
        val accountsSnapshot = accounts

        filterJob = ioScope.launch {
            val calculationJob = coroutineContext[Job]
            val mutation = currentContentMutation(
                period = periodSnapshot,
                filters = filtersSnapshot,
                transactionsSource = transactionsSnapshot,
                accounts = accountsSnapshot,
            )

            componentScope.launch {
                if (
                    filterJob === calculationJob &&
                    periodSnapshot == mutableState.value.period &&
                    filtersSnapshot == mutableState.value.filters
                ) {
                    mutation.reduce(mutableState)
                }
            }
        }
    }

    private fun showFilterSheet(sheet: AnalyticsFilterSheet) {
        mutableEffects.tryEmit(AnalyticsEffect.ShowFilterSheet(sheet))
    }

    private fun currentContentMutation(
        period: TransactionPeriod,
        filters: AnalyticsFilters,
        transactionsSource: List<TransactionHistoryEntry>,
        accounts: List<Account>,
    ): AnalyticsMutation {
        val transactions = filteredTransactions(transactionsSource, filters)
        val filterOptions = createFilterOptions(transactionsSource, accounts, filters)
        return if (transactions.isEmpty()) {
            AnalyticsMutation.Empty(
                period = period,
                allTransactions = transactionsSource,
                accounts = accounts,
                filters = filters,
                summary = createSummary(emptyList()),
                filterOptions = filterOptions,
            )
        } else {
            AnalyticsMutation.Content(
                period = period,
                allTransactions = transactionsSource,
                transactions = transactions,
                accounts = accounts,
                filters = filters,
                summary = createSummary(transactions),
                transactionItems = createTransactionItems(transactions, accounts),
                filterOptions = filterOptions,
            )
        }
    }

    private fun filteredTransactions(
        transactions: List<TransactionHistoryEntry>,
        filters: AnalyticsFilters,
    ): List<TransactionHistoryEntry> = transactions
        .asSequence()
        .filter { filters.type == null || it.category.type == filters.type }
        .filter { filters.categoryIds == null || it.category.id in filters.categoryIds!! }
        .filter { filters.accountId == null || it.accountId == filters.accountId }
        .sortedByDescending(TransactionHistoryEntry::occurredAt)
        .toList()

    private fun createFilterOptions(
        transactions: List<TransactionHistoryEntry>,
        accounts: List<Account>,
        filters: AnalyticsFilters,
    ): AnalyticsFilterOptions = AnalyticsFilterOptions(
        categories = transactions
            .asSequence()
            .filter { filters.type == null || it.category.type == filters.type }
            .map(TransactionHistoryEntry::category)
            .distinctBy { it.id }
            .sortedBy { it.name }
            .map { category ->
                AnalyticsCategoryOptionUi(
                    id = category.id,
                    name = category.name,
                    emoji = resolveCategoryEmoji(category.id, category.emoji),
                )
            }
            .toList(),
        accounts = accounts.map { account ->
            AnalyticsAccountOptionUi(
                id = account.id,
                name = account.name,
                emoji = resolveAccountEmoji(account.id.value, account.emoji),
            )
        },
    )

    private fun createTransactionItems(
        transactions: List<TransactionHistoryEntry>,
        accounts: List<Account>,
    ): List<AnalyticsTransactionItemUi> {
        val accountNames = accounts.associateBy({ it.id }, { it.name })
        return transactions.map { transaction ->
            AnalyticsTransactionItemUi(
                id = transaction.id.value,
                title = transaction.description?.takeIf(String::isNotBlank) ?: transaction.category.name,
                subtitle = accountNames[transaction.accountId] ?: UNKNOWN_ACCOUNT_NAME,
                emoji = resolveCategoryEmoji(transaction.category.id, transaction.category.emoji),
                amount = Money(transaction.amount, CurrencyCode.RUB).format(moneyFormatter),
            )
        }
    }

    private sealed interface AnalyticsLoadResult {
        data class Success(
            val transactions: List<TransactionHistoryEntry>,
            val accounts: List<Account>,
        ) : AnalyticsLoadResult

        data class Failure(
            val mutation: AnalyticsMutation,
        ) : AnalyticsLoadResult
    }

    private fun createSummary(transactions: List<TransactionHistoryEntry>): AnalyticsSummaryUi {
        val categories = transactions
            .groupBy { it.category.id }
            .map { (categoryId, categoryTransactions) ->
                val category = categoryTransactions.first().category
                val amount = categoryTransactions.sumOf(TransactionHistoryEntry::amount)
                AnalyticsCategorySummary(
                    categoryId = categoryId,
                    categoryName = category.name,
                    categoryEmoji = category.emoji,
                    amount = amount,
                    amountText = Money(amount, CurrencyCode.RUB).format(moneyFormatter),
                )
            }
            .sortedByDescending(AnalyticsCategorySummary::amount)

        val total = transactions.sumOf(TransactionHistoryEntry::amount)
        return AnalyticsSummaryUi(
            total = Money(total, CurrencyCode.RUB).format(moneyFormatter),
            categories = categories,
        )
    }

    private companion object {
        const val UNKNOWN_ACCOUNT_NAME = "Неизвестный счёт"
    }
}
