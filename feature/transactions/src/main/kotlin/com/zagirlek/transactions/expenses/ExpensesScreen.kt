package com.zagirlek.transactions.expenses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.zagirlek.finance.api.expense.ExpenseId
import com.zagirlek.systemdesign.theme.YaMoneyDesign
import com.zagirlek.systemdesign.theme.YaMoneyTheme
import com.zagirlek.transactions.R
import com.zagirlek.ui.components.BalanceCard
import com.zagirlek.ui.components.FinanceListItem
import com.zagirlek.ui.components.FinanceTopAppBar
import com.zagirlek.ui.formatter.Currency
import com.zagirlek.ui.formatter.DefaultMoneyFormatter
import com.zagirlek.ui.formatter.Money
import java.math.BigDecimal

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
            date = stringResource(R.string.expenses_selected_date),
            analyticsContentDescription = stringResource(R.string.expenses_analytics_content_description),
            settingsContentDescription = stringResource(R.string.expenses_settings_content_description),
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
            val moneyFormatter = remember { DefaultMoneyFormatter() }

            BalanceCard(
                title = stringResource(R.string.expenses_summary_title),
                balance = state.total,
                moneyFormatter = moneyFormatter,
            )
        }
        items(items = state.items, key = { it.id.value }) { item ->
            FinanceListItem(
                lead = item.lead,
                content = item.content,
                trail = item.trail,
                trailTag = item.trailTag,
                onClick = { onExpenseClicked(item.id) },
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun CenteredMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = message)
    }
}

@Composable
private fun ErrorContent(onRetryClicked: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(YaMoneyDesign.dimensions.space12),
        ) {
            Text(text = stringResource(R.string.expenses_error))
            Button(onClick = onRetryClicked) {
                Text(text = stringResource(R.string.expenses_retry))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpensesContentPreview() {
    YaMoneyTheme {
        ExpensesContent(
            state = ExpensesState.Content(
                total = Money(
                    amount = BigDecimal("1765.50"),
                    currency = Currency.Ruble,
                ),
                items = listOf(
                    ExpenseItemUi(
                        id = ExpenseId("preview"),
                        lead = "🛒",
                        content = stringResource(R.string.expenses_preview_item_title),
                        trail = "1 280,50",
                        trailTag = "₽",
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
