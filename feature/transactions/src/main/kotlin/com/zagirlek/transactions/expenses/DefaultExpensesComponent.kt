package com.zagirlek.transactions.expenses

import com.zagirlek.finance.api.expense.Expense
import com.zagirlek.finance.api.expense.ExpensesRepository
import com.zagirlek.ui.listitem.ListItem
import com.zagirlek.ui.listitem.ListItemContent
import com.zagirlek.ui.listitem.ListItemLead
import com.zagirlek.ui.listitem.ListItemTrail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import java.math.BigDecimal
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

/** MVI component backed by the fake repository during the first iteration. */
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
            ExpensesIntent.RetryClicked -> loadExpenses()
        }
    }

    private fun loadExpenses() {
        mutableState.value = try {
            val expenses = expensesRepository.getExpenses()
            when {
                expenses.isEmpty() -> ExpensesState.Empty
                else -> ExpensesState.Content(
                    summaryTitle = "Расходы, всего",
                    total = expenses.sumOf(Expense::amount).asRubles(),
                    items = expenses.map(Expense::toListItem),
                )
            }
        } catch (_: Exception) {
            ExpensesState.Error("Не удалось загрузить расходы")
        }
    }

}

private fun Expense.toListItem(): ListItem = ListItem(
    id = id.value,
    lead = ListItemLead.Emoji(type.emoji),
    content = ListItemContent(
        title = type.name,
        subtitle = description ?: occurredOn.format(DateTimeFormatter.ISO_LOCAL_DATE),
    ),
    trail = ListItemTrail(
        tag = "₽",
        text = amount.asRubles(),
    ),
)

private fun BigDecimal.asRubles(): String = DecimalFormat(
    "#,##0.##",
    java.text.DecimalFormatSymbols(Locale("ru", "RU")).apply { groupingSeparator = ' ' },
).format(this)
