package com.zagirlek.transactions.expenses

import com.arkivanov.decompose.ComponentContext
import com.zagirlek.finance.api.expense.Expense
import com.zagirlek.finance.api.expense.ExpensesRepository
import com.zagirlek.ui.cmp.MviComponent
import com.zagirlek.ui.formatter.Currency
import com.zagirlek.ui.formatter.DefaultMoneyFormatter
import com.zagirlek.ui.formatter.Money
import com.zagirlek.ui.formatter.MoneyFormatter
import com.zagirlek.ui.mvi.MviReducer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DefaultExpensesComponent(
    componentContext: ComponentContext,
    private val expensesRepository: ExpensesRepository,
    private val moneyFormatter: MoneyFormatter = DefaultMoneyFormatter(),
) : MviComponent<ExpensesState, ExpensesMutation, ExpensesIntent, ExpensesReducer>(
    reducer = ExpensesReducer,
    componentContext = componentContext,
), ExpensesComponent {
    private val mutableState = MutableStateFlow<ExpensesState>(ExpensesState.Loading)

    override val state: StateFlow<ExpensesState> = mutableState.asStateFlow()
    override val effects: Flow<ExpensesEffect> = emptyFlow()

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
        ExpensesMutation.Loading.reduce(mutableState)

        componentScope.launch {
            val mutation = runCatching {
                val expenses = expensesRepository.getExpenses()

                if (expenses.isEmpty()) {
                    ExpensesMutation.Empty
                } else {
                    ExpensesMutation.Content(
                        total = Money(
                            amount = expenses.sumOf(Expense::amount),
                            currency = Currency.Ruble,
                        ).format(moneyFormatter),
                        items = expenses.map(::toExpenseItemUi),
                    )
                }
            }.getOrElse {
                ExpensesMutation.Error
            }

            mutation.reduce(mutableState)
        }
    }

    private fun toExpenseItemUi(expense: Expense): ExpenseItemUi = ExpenseItemUi(
        id = expense.id,
        lead = expense.type.emoji,
        content = expense.description ?: expense.type.name,
        trail = Money(amount = expense.amount, currency = Currency.Ruble).format(moneyFormatter)
    )
}
