package com.zagirlek.analytics

import com.arkivanov.decompose.ComponentContext
import com.zagirlek.analytics.ui.resolveAccountEmoji
import com.zagirlek.analytics.ui.resolveCategoryEmoji
import com.zagirlek.analytics.ui.summary.AnalyticsCategorySummary
import com.zagirlek.finance.api.account.Account
import com.zagirlek.finance.api.account.AccountsRepository
import com.zagirlek.finance.api.error.toNetworkError
import com.zagirlek.finance.api.money.CurrencyCode
import com.zagirlek.finance.api.money.Money
import com.zagirlek.finance.api.transaction.Transaction
import com.zagirlek.finance.api.transaction.TransactionPeriod
import com.zagirlek.finance.api.transaction.TransactionsRepository
import com.zagirlek.ui.cmp.MviComponent
import com.zagirlek.ui.formatter.DefaultMoneyFormatter
import com.zagirlek.ui.formatter.MoneyFormatter
import com.zagirlek.ui.formatter.format
import java.time.LocalDate
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class DefaultAnalyticsComponent(
    componentContext: ComponentContext,
    private val transactionsRepository: TransactionsRepository,
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
    private var observationJob: Job? = null
    private var refreshJob: Job? = null
    private var filterJob: Job? = null

    override val state: StateFlow<AnalyticsState> = mutableState.asStateFlow()
    override val effects: Flow<AnalyticsEffect> = mutableEffects.asSharedFlow()

    init {
        observePeriod(mutableState.value.period)
        refreshPeriod(
            period = mutableState.value.period,
            showRefreshing = false,
        )
    }

    override fun accept(intent: AnalyticsIntent) {
        when (intent) {
            AnalyticsIntent.BackClicked -> onBackRequested()
            AnalyticsIntent.RetryClicked,
            AnalyticsIntent.RefreshRequested,
            -> refreshPeriod(
                period = mutableState.value.period,
                showRefreshing = true,
            )
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
            is AnalyticsIntent.PeriodPresetApplied -> changePeriod(
                intent.preset.toPeriod(LocalDate.now()),
            )
            is AnalyticsIntent.PeriodApplied -> changePeriod(intent.period)
            is AnalyticsIntent.CategoriesApplied -> updateFilters(
                mutableState.value.filters.copy(categoryIds = intent.categoryIds),
            )
            is AnalyticsIntent.AccountApplied -> updateFilters(
                mutableState.value.filters.copy(accountId = intent.accountId),
            )
        }
    }

    private fun changePeriod(period: TransactionPeriod) {
        if (period == mutableState.value.period) {
            refreshPeriod(period = period, showRefreshing = true)
            return
        }

        filterJob?.cancel()
        refreshJob?.cancel()
        AnalyticsMutation.Loading(
            period = period,
            filters = mutableState.value.filters,
        ).reduce(mutableState)
        observePeriod(period)
        refreshPeriod(period = period, showRefreshing = false)
    }

    private fun observePeriod(period: TransactionPeriod) {
        observationJob?.cancel()
        observationJob = componentScope.launch {
            try {
                combine(
                    transactionsRepository.observeTransactions(period),
                    accountsRepository.observeAccounts(),
                ) { transactions, accounts ->
                    AnalyticsData(
                        transactions = transactions.sortedByDescending(Transaction::occurredAt),
                        accounts = accounts,
                    )
                }.collect { data ->
                    if (period != mutableState.value.period) return@collect
                    recalculateContent(
                        period = period,
                        filters = mutableState.value.filters,
                        allTransactions = data.transactions,
                        accounts = data.accounts,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                AnalyticsMutation.Error(
                    error = error.toNetworkError(),
                    period = period,
                    filters = mutableState.value.filters,
                ).reduce(mutableState)
            }
        }
    }

    private fun refreshPeriod(
        period: TransactionPeriod,
        showRefreshing: Boolean,
    ) {
        if (refreshJob?.isActive == true) return
        if (showRefreshing) {
            AnalyticsMutation.Refreshing.reduce(mutableState)
        }

        refreshJob = ioScope.launch {
            try {
                transactionsRepository.refreshTransactions(period)
                componentScope.launch {
                    if (period == mutableState.value.period) {
                        AnalyticsMutation.RefreshCompleted.reduce(mutableState)
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                componentScope.launch {
                    if (period == mutableState.value.period) {
                        AnalyticsMutation.RefreshFailed(error.toNetworkError())
                            .reduce(mutableState)
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
        allTransactions: List<Transaction>,
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
        transactionsSource: List<Transaction>,
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
        transactions: List<Transaction>,
        filters: AnalyticsFilters,
    ): List<Transaction> = transactions
        .asSequence()
        .filter { filters.type == null || it.category.type == filters.type }
        .filter {
            filters.categoryIds == null ||
                it.category.id.value in requireNotNull(filters.categoryIds)
        }
        .filter { filters.accountId == null || it.accountId == filters.accountId }
        .sortedByDescending(Transaction::occurredAt)
        .toList()

    private fun createFilterOptions(
        transactions: List<Transaction>,
        accounts: List<Account>,
        filters: AnalyticsFilters,
    ): AnalyticsFilterOptions = AnalyticsFilterOptions(
        categories = transactions
            .asSequence()
            .filter { filters.type == null || it.category.type == filters.type }
            .map(Transaction::category)
            .distinctBy { category -> category.id }
            .sortedBy { category -> category.name }
            .map { category ->
                AnalyticsCategoryOptionUi(
                    id = category.id.value,
                    name = category.name,
                    emoji = resolveCategoryEmoji(category.id.value, category.emoji),
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
        transactions: List<Transaction>,
        accounts: List<Account>,
    ): List<AnalyticsTransactionItemUi> {
        val accountNames = accounts.associateBy(Account::id, Account::name)
        return transactions.map { transaction ->
            AnalyticsTransactionItemUi(
                id = transaction.id.value,
                title = transaction.comment?.takeIf(String::isNotBlank)
                    ?: transaction.category.name,
                subtitle = accountNames[transaction.accountId] ?: UNKNOWN_ACCOUNT_NAME,
                emoji = resolveCategoryEmoji(
                    transaction.category.id.value,
                    transaction.category.emoji,
                ),
                amount = transaction.money.format(moneyFormatter),
            )
        }
    }

    private fun createSummary(transactions: List<Transaction>): AnalyticsSummaryUi {
        val categories = transactions
            .groupBy { transaction -> transaction.category.id }
            .map { (categoryId, categoryTransactions) ->
                val category = categoryTransactions.first().category
                val amount = categoryTransactions.sumOf { transaction ->
                    transaction.money.amount
                }
                AnalyticsCategorySummary(
                    categoryId = categoryId.value,
                    categoryName = category.name,
                    categoryEmoji = category.emoji,
                    amount = amount,
                    amountText = Money(amount, CurrencyCode.RUB).format(moneyFormatter),
                )
            }
            .sortedByDescending(AnalyticsCategorySummary::amount)

        val total = transactions.sumOf { transaction -> transaction.money.amount }
        return AnalyticsSummaryUi(
            total = Money(total, CurrencyCode.RUB).format(moneyFormatter),
            categories = categories,
        )
    }

    private data class AnalyticsData(
        val transactions: List<Transaction>,
        val accounts: List<Account>,
    )

    private companion object {
        const val UNKNOWN_ACCOUNT_NAME = "Неизвестный счёт"
    }
}
