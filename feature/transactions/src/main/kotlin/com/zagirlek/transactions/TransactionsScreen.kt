package com.zagirlek.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.zagirlek.systemdesign.theme.YaMoneyDesign
import com.zagirlek.systemdesign.theme.YaMoneyTheme
import com.zagirlek.transactions.R
import com.zagirlek.ui.components.elements.CenteredMessage
import com.zagirlek.ui.components.elements.LoadingContent
import com.zagirlek.ui.components.finance.BalanceCard
import com.zagirlek.ui.components.finance.FinanceListItem
import com.zagirlek.ui.components.finance.FinanceScaffold

@Composable
fun TransactionsScreen(
    type: TransactionType,
    component: TransactionsComponent,
    onAnalyticsClick: () -> Unit = {},
) {
    val state by component.state.collectAsState()
    TransactionsContent(
        type = type,
        state = state,
        onIntent = component::accept,
        onAnalyticsClick = onAnalyticsClick,
    )
}

@Composable
fun TransactionsContent(
    type: TransactionType,
    state: TransactionsState,
    onIntent: (TransactionsIntent) -> Unit,
    onAnalyticsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    FinanceScaffold(
        date = stringResource(R.string.example_date),
        onDateClick = { onIntent(TransactionsIntent.DateClicked) },
        onAnalyticsClick = onAnalyticsClick,
        onSettingsClick = { onIntent(TransactionsIntent.SettingsClicked) },
        onAddClick = { onIntent(TransactionsIntent.AddClicked) },
        modifier = modifier,
    ) { scaffoldPadding ->
        when (state) {
            is TransactionsState.Content -> TransactionsList(
                state = state,
                summaryTitle = stringResource(type.summaryTitleRes),
                scaffoldPadding = scaffoldPadding,
                onTransactionClicked = { id ->
                    onIntent(TransactionsIntent.TransactionClicked(id))
                },
                onRefresh = { onIntent(TransactionsIntent.RefreshRequested) },
            )

            TransactionsState.Loading -> FinanceStateContent(scaffoldPadding) {
                LoadingContent()
            }

            TransactionsState.Empty -> FinanceStateContent(scaffoldPadding) {
                CenteredMessage(stringResource(type.emptyMessageRes))
            }

            is TransactionsState.Error -> FinanceStateContent(scaffoldPadding) {
                CenteredMessage(stringResource(type.emptyMessageRes))
            }
        }
    }
}

@Composable
private fun TransactionsList(
    state: TransactionsState.Content,
    summaryTitle: String,
    scaffoldPadding: PaddingValues,
    onTransactionClicked: (String) -> Unit,
    onRefresh: () -> Unit,
) {
    val dimensions = YaMoneyDesign.dimensions

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier
            .fillMaxSize()
            .padding(scaffoldPadding),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = dimensions.fabSize + dimensions.space32),
            verticalArrangement = Arrangement.spacedBy(dimensions.space4),
        ) {
            item {
                BalanceCard(
                    title = summaryTitle,
                    balance = state.total,
                )
            }
            items(items = state.items, key = TransactionItemUi::id) { item ->
                FinanceListItem(
                    lead = item.lead,
                    content = item.title,
                    trail = item.trail,
                    onClick = { onTransactionClicked(item.id) },
                )
            }
        }
    }
}

@Composable
private fun FinanceStateContent(
    scaffoldPadding: PaddingValues,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(scaffoldPadding),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

private val TransactionType.summaryTitleRes: Int
    get() = when (this) {
        TransactionType.Expense -> R.string.expenses_summary_title
        TransactionType.Income -> R.string.income_summary_title
    }

private val TransactionType.emptyMessageRes: Int
    get() = when (this) {
        TransactionType.Expense -> R.string.expenses_empty
        TransactionType.Income -> R.string.income_empty
    }

@Preview(showBackground = true)
@Composable
private fun ExpenseTransactionsPreview() {
    YaMoneyTheme {
        TransactionsContent(
            type = TransactionType.Expense,
            state = TransactionsState.Content(
                total = "1 765,50 ₽",
                items = listOf(
                    TransactionItemUi(
                        id = "expense-preview",
                        lead = "🛒",
                        title = "Продукты",
                        trail = "1 280,50 ₽",
                    ),
                ),
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun IncomeTransactionsPreview() {
    YaMoneyTheme {
        TransactionsContent(
            type = TransactionType.Income,
            state = TransactionsState.Content(
                total = "120 385,42 ₽",
                items = listOf(
                    TransactionItemUi(
                        id = "income-preview",
                        lead = "💼",
                        title = "Зарплата",
                        trail = "120 000 ₽",
                    ),
                ),
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionsLoadingPreview() {
    YaMoneyTheme {
        TransactionsContent(
            type = TransactionType.Expense,
            state = TransactionsState.Loading,
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionsEmptyPreview() {
    YaMoneyTheme {
        TransactionsContent(
            type = TransactionType.Income,
            state = TransactionsState.Empty,
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionsErrorPreview() {
    YaMoneyTheme {
        TransactionsContent(
            type = TransactionType.Expense,
            state = TransactionsState.Error(
                com.zagirlek.finance.api.error.NetworkError.Unknown,
            ),
            onIntent = {},
        )
    }
}
