package com.zagirlek.transactions.expenses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.zagirlek.finance.api.expense.ExpenseId
import com.zagirlek.systemdesign.theme.YaMoneyDesign
import com.zagirlek.systemdesign.theme.YaMoneyTheme
import com.zagirlek.transactions.R
import com.zagirlek.ui.components.finance.BalanceCard
import com.zagirlek.ui.components.elements.CenteredMessage
import com.zagirlek.ui.components.ErrorContent
import com.zagirlek.ui.components.finance.FinanceListItem
import com.zagirlek.ui.components.finance.FinanceTopAppBar
import com.zagirlek.ui.components.elements.LoadingContent

@Composable
fun ExpensesScreen(component: ExpensesComponent) {
    val state by component.state.collectAsState()

    ExpensesContent(
        state = state,
        onIntent = component::accept,
    )
}

@Composable
fun ExpensesContent(
    state: ExpensesState,
    onIntent: (ExpensesIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        FinanceTopAppBar(
            date = stringResource(R.string.example_date),
            onDateClick = { onIntent(ExpensesIntent.DateClicked) },
            onAnalyticsClick = { onIntent(ExpensesIntent.AnalyticsClicked) },
            onSettingsClick = { onIntent(ExpensesIntent.SettingsClicked) },
        )

        when (state) {
            ExpensesState.Loading -> LoadingContent()
            ExpensesState.Empty -> CenteredMessage(stringResource(R.string.expenses_empty))
            ExpensesState.Error -> ErrorContent(
                onRetryClicked = { onIntent(ExpensesIntent.RetryClicked) },
            )
            is ExpensesState.Content -> ExpensesList(
                state = state,
                onExpenseClicked = { id -> onIntent(ExpensesIntent.ExpenseClicked(id)) },
            )
        }
    }
}

@Composable
private fun ExpensesList(
    state: ExpensesState.Content,
    onExpenseClicked: (ExpenseId) -> Unit,
) {
    val dimensions = YaMoneyDesign.dimensions

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(dimensions.space4),
    ) {
        item {
            BalanceCard(
                title = stringResource(R.string.expenses_summary_title),
                balance = state.total
            )
        }
        items(items = state.items, key = { it.id.value }) { item ->
            FinanceListItem(
                lead = item.lead,
                content = item.content,
                trail = item.trail,
                onClick = { onExpenseClicked(item.id) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpensesContentPreview() {
    YaMoneyTheme {
        ExpensesContent(
            state = ExpensesState.Content(
                total = "1 432 ₽",
                items = listOf(
                    ExpenseItemUi(
                        id = ExpenseId("preview"),
                        lead = "🛒",
                        content = stringResource(R.string.expenses_preview_item_title),
                        trail = "1 280,50 ₽"
                    ),
                ),
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpensesLoadingPreview() {
    YaMoneyTheme {
        ExpensesContent(
            state = ExpensesState.Loading,
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpensesEmptyPreview() {
    YaMoneyTheme {
        ExpensesContent(
            state = ExpensesState.Empty,
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpensesErrorPreview() {
    YaMoneyTheme {
        ExpensesContent(
            state = ExpensesState.Error,
            onIntent = {},
        )
    }
}
