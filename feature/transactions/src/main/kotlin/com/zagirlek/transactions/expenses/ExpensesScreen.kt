package com.zagirlek.transactions.expenses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.zagirlek.systemdesign.foundation.FinanceDesign
import com.zagirlek.systemdesign.theme.FinanceTheme
import com.zagirlek.ui.listitem.FinanceListItem
import com.zagirlek.ui.listitem.ListItem
import com.zagirlek.ui.listitem.ListItemContent
import com.zagirlek.ui.listitem.ListItemLead
import com.zagirlek.ui.listitem.ListItemTrail

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
    val dimensions = FinanceDesign.dimensions

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        Text(
            text = "Расходы",
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensions.topBarHeight)
                .padding(horizontal = dimensions.screenHorizontalPadding),
            style = MaterialTheme.typography.titleLarge,
        )

        when (state) {
            ExpensesState.Loading -> CenteredMessage("Загрузка…")
            ExpensesState.Empty -> CenteredMessage("Расходов пока нет")
            is ExpensesState.Error -> ErrorContent(
                message = state.message,
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
    onExpenseClicked: (com.zagirlek.finance.api.expense.ExpenseId) -> Unit,
) {
    val dimensions = FinanceDesign.dimensions

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(dimensions.space4),
    ) {
        item {
            Column(
                modifier = Modifier.padding(
                    horizontal = dimensions.screenHorizontalPadding,
                    vertical = dimensions.screenVerticalPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(dimensions.space4),
            ) {
                Text(
                    text = state.summaryTitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "${state.total} ₽",
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        }
        items(
            items = state.items,
            key = ListItem::id,
        ) { item ->
            FinanceListItem(
                item = item,
                onClick = { onExpenseClicked(com.zagirlek.finance.api.expense.ExpenseId(item.id)) },
            )
        }
    }
}

@Composable
private fun CenteredMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetryClicked: () -> Unit,
) {
    val dimensions = FinanceDesign.dimensions

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.space12),
        ) {
            Text(text = message, style = MaterialTheme.typography.bodyLarge)
            Button(onClick = onRetryClicked) {
                Text("Повторить")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpensesContentPreview() {
    FinanceTheme {
        ExpensesContent(
            state = ExpensesState.Content(
                summaryTitle = "Расходы, всего",
                total = "1 765,50",
                items = listOf(
                    ListItem(
                        id = "preview",
                        lead = ListItemLead.Emoji("🛒"),
                        content = ListItemContent("Продукты", "Перекрёсток"),
                        trail = ListItemTrail("₽", "1 280,50"),
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
    FinanceTheme {
        ExpensesContent(
            state = ExpensesState.Loading,
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpensesEmptyPreview() {
    FinanceTheme {
        ExpensesContent(
            state = ExpensesState.Empty,
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpensesErrorPreview() {
    FinanceTheme {
        ExpensesContent(
            state = ExpensesState.Error("Не удалось загрузить расходы"),
            onIntent = {},
        )
    }
}
