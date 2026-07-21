package com.zagirlek.analytics

import com.arkivanov.decompose.ComponentContext
import com.zagirlek.finance.api.account.AccountsRepository
import com.zagirlek.finance.api.transaction.TransactionHistoryEntry
import com.zagirlek.finance.api.transaction.TransactionHistoryRepository
import com.zagirlek.finance.api.transaction.TransactionPeriod
import com.zagirlek.ui.cmp.MviComponent
import com.zagirlek.ui.formatter.Currency
import com.zagirlek.ui.formatter.DefaultMoneyFormatter
import com.zagirlek.ui.formatter.Money
import com.zagirlek.ui.formatter.MoneyFormatter
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
import kotlin.coroutines.cancellation.CancellationException

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

    private val mutableState = MutableStateFlow<AnalyticsState>(AnalyticsState.Loading)
    private val mutableEffects = MutableSharedFlow<AnalyticsEffect>(extraBufferCapacity = 1)
    private var loadJob: Job? = null
    private var period = TransactionPeriod.currentMonthToDate()
    private var filters = AnalyticsFilters()
    private var loadedTransactions: List<TransactionHistoryEntry> = emptyList()
    private var loadedAccounts = emptyList<Account>()

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
            is AnalyticsIntent.TypeApplied -> updateFilters(
                filters.copy(
                    type = intent.type,
                    categoryIds = emptySet(),
                ),
            )
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

                loadedTransactions = history
                loadedAccounts = accounts
                currentContentMutation()
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
        currentContentMutation().reduce(mutableState)
    }

    private fun showFilterSheet(sheet: AnalyticsFilterSheet) {
        mutableEffects.tryEmit(AnalyticsEffect.ShowFilterSheet(sheet))
    }

    private fun currentContentMutation(): AnalyticsMutation {
        val transactions = filteredTransactions()
        val filterOptions = createFilterOptions()
        return if (transactions.isEmpty()) {
            AnalyticsMutation.Empty(
                period = period,
                accounts = loadedAccounts,
                filters = filters,
                summary = createSummary(emptyList()),
                filterOptions = filterOptions,
            )
        } else {
            AnalyticsMutation.Content(
                period = period,
                transactions = transactions,
                accounts = loadedAccounts,
                filters = filters,
                summary = createSummary(transactions),
                transactionItems = createTransactionItems(transactions),
                filterOptions = filterOptions,
            )
        }
    }

    private fun filteredTransactions(): List<TransactionHistoryEntry> = loadedTransactions
        .asSequence()
        .filter { filters.type == null || it.category.type == filters.type }
        .filter { filters.categoryIds.isEmpty() || it.category.id in filters.categoryIds }
        .filter { filters.accountId == null || it.accountId == filters.accountId }
        .sortedByDescending(TransactionHistoryEntry::occurredAt)
        .toList()

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
                    amountText = Money(amount, Currency.Ruble).format(moneyFormatter),
                )
            }
            .sortedByDescending(AnalyticsCategorySummary::amount)

        val total = transactions.sumOf(TransactionHistoryEntry::amount)
        return AnalyticsSummaryUi(
            total = Money(total, Currency.Ruble).format(moneyFormatter),
            categories = categories,
        )
    }

    private fun createFilterOptions(): AnalyticsFilterOptions = AnalyticsFilterOptions(
        categories = loadedTransactions
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
        accounts = loadedAccounts.map { account ->
            AnalyticsAccountOptionUi(
                id = account.id,
                name = account.name,
                emoji = resolveAccountEmoji(account.id.value, account.emoji),
            )
        },
    )

    private fun createTransactionItems(
        transactions: List<TransactionHistoryEntry>,
    ): List<AnalyticsTransactionItemUi> {
        val accountNames = loadedAccounts.associateBy({ it.id }, { it.name })
        return transactions.map { transaction ->
            AnalyticsTransactionItemUi(
                id = transaction.id.value,
                title = transaction.description?.takeIf(String::isNotBlank) ?: transaction.category.name,
                subtitle = accountNames[transaction.accountId] ?: UNKNOWN_ACCOUNT_NAME,
                emoji = resolveCategoryEmoji(transaction.category.id, transaction.category.emoji),
                amount = Money(transaction.amount, Currency.Ruble).format(moneyFormatter),
            )
        }
    }

    private fun Exception.toErrorMessage(): String = message ?: DEFAULT_ERROR_MESSAGE

    private companion object {
        const val DEFAULT_ERROR_MESSAGE = "Не удалось загрузить аналитику."
        const val UNKNOWN_ACCOUNT_NAME = "Неизвестный счёт"
    }
}
