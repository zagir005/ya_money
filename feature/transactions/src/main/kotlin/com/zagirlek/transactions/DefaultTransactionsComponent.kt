package com.zagirlek.transactions

import com.arkivanov.decompose.ComponentContext
import com.zagirlek.finance.api.expense.Expense
import com.zagirlek.finance.api.expense.ExpensesRepository
import com.zagirlek.finance.api.income.Income
import com.zagirlek.finance.api.income.IncomesRepository
import com.zagirlek.finance.api.transaction.TransactionPeriod
import com.zagirlek.ui.cmp.MviComponent
import com.zagirlek.ui.formatter.Currency
import com.zagirlek.ui.formatter.DefaultMoneyFormatter
import com.zagirlek.ui.formatter.Money
import com.zagirlek.ui.formatter.MoneyFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class DefaultTransactionsComponent(
    componentContext: ComponentContext,
    private val type: TransactionType,
    private val expensesRepository: ExpensesRepository,
    private val incomesRepository: IncomesRepository,
    private val moneyFormatter: MoneyFormatter = DefaultMoneyFormatter(),
) : MviComponent<TransactionsState, TransactionsMutation, TransactionsIntent, TransactionsReducer>(
    reducer = TransactionsReducer,
    componentContext = componentContext,
), TransactionsComponent {

    private val mutableState = MutableStateFlow<TransactionsState>(TransactionsState.Loading)

    override val state: StateFlow<TransactionsState> = mutableState.asStateFlow()
    private val mutableEffects = MutableSharedFlow<TransactionsEffect>(extraBufferCapacity = 1)

    override val effects: Flow<TransactionsEffect> = mutableEffects.asSharedFlow()

    private val period = TransactionPeriod.currentMonthToDate()
    private var loadJob: Job? = null

    init {
        loadTransactions()
    }

    override fun accept(intent: TransactionsIntent) {
        when (intent) {
            is TransactionsIntent.TransactionClicked -> Unit
            TransactionsIntent.DateClicked -> Unit
            TransactionsIntent.AnalyticsClicked -> Unit
            TransactionsIntent.SettingsClicked -> Unit
            TransactionsIntent.AddClicked -> Unit
            TransactionsIntent.RetryClicked -> loadTransactions()
            TransactionsIntent.RefreshRequested -> loadTransactions(isRefresh = true)
        }
    }

    private fun loadTransactions(isRefresh: Boolean = false) {
        if (loadJob?.isActive == true) return

        val isContentRefresh = isRefresh && mutableState.value is TransactionsState.Content
        if (isContentRefresh) {
            TransactionsMutation.Refreshing.reduce(mutableState)
        } else {
            TransactionsMutation.Loading.reduce(mutableState)
        }

        loadJob = ioScope.launch {
            val mutation = try {
                when (type) {
                    TransactionType.Expense -> expensesRepository.getExpenses(period)
                        .sortedByDescending(Expense::occurredAt)
                        .toExpensesMutation()
                    TransactionType.Income -> incomesRepository.getIncomes(period)
                        .sortedByDescending(Income::occurredAt)
                        .toIncomesMutation()
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                if (isContentRefresh) {
                    TransactionsMutation.RefreshFailed(error.toErrorMessage())
                } else {
                    TransactionsMutation.Error(error.toErrorMessage())
                }
            }

            componentScope.launch {
                if (mutation is TransactionsMutation.Error) {
                    mutableEffects.tryEmit(TransactionsEffect.ShowRetryableError(mutation.message))
                }
                if (mutation is TransactionsMutation.RefreshFailed) {
                    mutableEffects.tryEmit(TransactionsEffect.ShowRetryableError(mutation.message))
                }
                mutation.reduce(mutableState)
            }
        }
    }

    private fun List<Expense>.toExpensesMutation(): TransactionsMutation = when {
        isEmpty() -> TransactionsMutation.Empty
        else -> TransactionsMutation.Content(
            total = Money(sumOf(Expense::amount), Currency.Ruble).format(moneyFormatter),
            items = map { expense ->
                TransactionItemUi(
                    id = expense.id.value,
                    lead = expense.type.emoji,
                    content = expense.description ?: expense.type.name,
                    trail = Money(expense.amount, Currency.Ruble).format(moneyFormatter),
                )
            },
        )
    }

    private fun List<Income>.toIncomesMutation(): TransactionsMutation = when {
        isEmpty() -> TransactionsMutation.Empty
        else -> TransactionsMutation.Content(
            total = Money(sumOf(Income::amount), Currency.Ruble).format(moneyFormatter),
            items = map { income ->
                TransactionItemUi(
                    id = income.id.value,
                    lead = income.type.emoji,
                    content = income.description ?: income.type.name,
                    trail = Money(income.amount, Currency.Ruble).format(moneyFormatter),
                )
            },
        )
    }

    private fun Exception.toErrorMessage(): String = message ?: DEFAULT_ERROR_MESSAGE

    private companion object {
        const val DEFAULT_ERROR_MESSAGE = "Не удалось загрузить операции."
    }
}
