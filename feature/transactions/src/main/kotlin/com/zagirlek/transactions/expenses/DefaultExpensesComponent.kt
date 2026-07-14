package com.zagirlek.transactions.expenses

import com.zagirlek.finance.api.expense.Expense
import com.zagirlek.finance.api.expense.ExpensesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import java.math.BigDecimal
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

class DefaultExpensesComponent(
    private val expensesRepository: ExpensesRepository,
) : ExpensesComponent {
    private val mutableState = MutableStateFlow<ExpensesState>(ExpensesState.Loading)

    override val state: StateFlow<ExpensesState> = mutableState
    override val effects: Flow<Nothing> = emptyFlow()

    init {
        loadExpenses()
    }

    override fun accept(intent: ExpensesIntent) {
        when (intent) {
            is ExpensesIntent.ExpenseClicked -> Unit
            ExpensesIntent.DateClicked -> Unit
            ExpensesIntent.AnalyticsClicked -> Unit
            ExpensesIntent.SettingsClicked -> Unit
            ExpensesIntent.RetryClicked -> loadExpenses()
        }
    }

    private fun loadExpenses() {
        mutableState.value = try {
            val expenses = expensesRepository.getExpenses()
            when {
                expenses.isEmpty() -> ExpensesState.Empty
                else -> ExpensesState.Content(
                    total = expenses.sumOf(Expense::amount).asRubles(),
                    items = expenses.map(Expense::toExpenseItemUi),
                )
            }
        } catch (_: Exception) {
            ExpensesState.Error
        }
    }
}

private fun Expense.toExpenseItemUi(): ExpenseItemUi = ExpenseItemUi(
    id = id,
    lead = type.emoji,
    content = description ?: type.name,
    trail = amount.asRubles(),
    trailTag = "₽",
)
