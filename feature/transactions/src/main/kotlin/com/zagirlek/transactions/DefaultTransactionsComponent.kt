package com.zagirlek.transactions

import com.arkivanov.decompose.ComponentContext
import com.zagirlek.finance.api.money.Money
import com.zagirlek.finance.api.transaction.Transaction
import com.zagirlek.finance.api.transaction.TransactionId
import com.zagirlek.finance.api.transaction.TransactionPeriod
import com.zagirlek.finance.api.transaction.TransactionsRepository
import com.zagirlek.finance.api.error.toNetworkError
import com.zagirlek.ui.cmp.MviComponent
import com.zagirlek.ui.formatter.DefaultMoneyFormatter
import com.zagirlek.ui.formatter.MoneyFormatter
import com.zagirlek.ui.formatter.format
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

class DefaultTransactionsComponent(
    componentContext: ComponentContext,
    private val type: TransactionType,
    private val transactionsRepository: TransactionsRepository,
    private val onAddRequested: (TransactionType) -> Unit,
    private val onEditRequested: (TransactionId) -> Unit,
    private val moneyFormatter: MoneyFormatter = DefaultMoneyFormatter(),
) : MviComponent<TransactionsState, TransactionsMutation, TransactionsIntent, TransactionsReducer>(
    reducer = TransactionsReducer,
    componentContext = componentContext,
), TransactionsComponent {

    private val mutableState = MutableStateFlow<TransactionsState>(TransactionsState.Loading)

    override val state: StateFlow<TransactionsState> = mutableState.asStateFlow()
    override val effects: Flow<TransactionsEffect> = emptyFlow()

    private val period = TransactionPeriod.currentMonthToDate()
    private var refreshJob: Job? = null

    init {
        observeTransactions()
        refreshTransactions()
    }

    override fun accept(intent: TransactionsIntent) {
        when (intent) {
            is TransactionsIntent.TransactionClicked ->
                onEditRequested(TransactionId(intent.id))
            TransactionsIntent.AddClicked -> onAddRequested(type)
            TransactionsIntent.RetryClicked,
            TransactionsIntent.RefreshRequested,
            -> refreshTransactions()
            TransactionsIntent.DateClicked,
            TransactionsIntent.AnalyticsClicked,
            TransactionsIntent.SettingsClicked,
            -> Unit
        }
    }

    private fun observeTransactions() {
        componentScope.launch {
            transactionsRepository.observeTransactions(period).collect { transactions ->
                transactions
                    .filter { transaction -> transaction.category.type == type }
                    .toMutation()
                    .reduce(mutableState)
            }
        }
    }

    private fun refreshTransactions() {
        if (refreshJob?.isActive == true) return

        if (mutableState.value is TransactionsState.Content) {
            TransactionsMutation.Refreshing.reduce(mutableState)
        }

        refreshJob = ioScope.launch {
            try {
                transactionsRepository.refreshTransactions(period)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                componentScope.launch {
                    val mutation = if (mutableState.value is TransactionsState.Content) {
                        TransactionsMutation.RefreshFailed(error.toNetworkError())
                    } else {
                        TransactionsMutation.Error(error.toNetworkError())
                    }
                    mutation.reduce(mutableState)
                }
            }
        }
    }

    private fun List<Transaction>.toMutation(): TransactionsMutation = when {
        isEmpty() -> TransactionsMutation.Empty
        else -> TransactionsMutation.Content(
            total = groupBy { transaction -> transaction.money.currency }
                .values
                .map { transactions ->
                    Money(
                        amount = transactions.sumOf { it.money.amount },
                        currency = transactions.first().money.currency,
                    ).format(moneyFormatter)
                }
                .joinToString(separator = " · "),
            items = map { transaction ->
                TransactionItemUi(
                    id = transaction.id.value,
                    lead = transaction.category.emoji,
                    content = transaction.comment ?: transaction.category.name,
                    trail = transaction.money.format(moneyFormatter),
                )
            },
        )
    }
}
