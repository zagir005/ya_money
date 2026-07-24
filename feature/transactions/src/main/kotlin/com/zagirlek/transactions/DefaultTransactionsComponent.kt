package com.zagirlek.transactions

import com.arkivanov.decompose.ComponentContext
import com.zagirlek.finance.api.expense.Expense
import com.zagirlek.finance.api.expense.ExpensesRepository
import com.zagirlek.finance.api.error.toNetworkError
import com.zagirlek.finance.api.income.Income
import com.zagirlek.finance.api.income.IncomesRepository
import com.zagirlek.finance.api.money.CurrencyCode
import com.zagirlek.finance.api.money.Money
import com.zagirlek.finance.api.transaction.TransactionPeriod
import com.zagirlek.ui.cmp.MviComponent
import com.zagirlek.ui.formatter.DefaultMoneyFormatter
import com.zagirlek.ui.formatter.MoneyFormatter
import com.zagirlek.ui.formatter.format
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
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
    override val effects: Flow<TransactionsEffect> = emptyFlow()

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
            TransactionsIntent.RetryClicked -> loadTransactions(isRefresh = true)
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
                    TransactionsMutation.RefreshFailed(error.toNetworkError())
                } else {
                    TransactionsMutation.Error(error.toNetworkError())
                }
            }

            componentScope.launch {
                mutation.reduce(mutableState)
            }
        }
    }

    private fun List<Expense>.toExpensesMutation(): TransactionsMutation = when {
        isEmpty() -> TransactionsMutation.Empty
        else -> TransactionsMutation.Content(
            total = Money(sumOf(Expense::amount), CurrencyCode.RUB).format(moneyFormatter),
            items = map { expense ->
                TransactionItemUi(
                    id = expense.id.value,
                    lead = expense.type.emoji,
                    content = expense.description ?: expense.type.name,
                    trail = Money(expense.amount, CurrencyCode.RUB).format(moneyFormatter),
                )
            },
        )
    }

    private fun List<Income>.toIncomesMutation(): TransactionsMutation = when {
        isEmpty() -> TransactionsMutation.Empty
        else -> TransactionsMutation.Content(
            total = Money(sumOf(Income::amount), CurrencyCode.RUB).format(moneyFormatter),
            items = map { income ->
                TransactionItemUi(
                    id = income.id.value,
                    lead = income.type.emoji,
                    content = income.description ?: income.type.name,
                    trail = Money(income.amount, CurrencyCode.RUB).format(moneyFormatter),
                )
            },
        )
    }

}
