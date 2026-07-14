package com.zagirlek.transactions.expenses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.zagirlek.ui.components.BalanceCard
import com.zagirlek.ui.components.FinanceListItem

@Composable
fun ExpensesScreen(component: ExpensesComponent) {
    val state by component.state.collectAsState()

    ExpensesContent(
        state = state,
        onIntent = component::accept,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesContent(
    state: ExpensesState,
    onIntent: (ExpensesIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        ExpensesTopAppBar(
            onDateClicked = { onIntent(ExpensesIntent.DateClicked) },
            onAnalyticsClicked = { onIntent(ExpensesIntent.AnalyticsClicked) },
            onSettingsClicked = { onIntent(ExpensesIntent.SettingsClicked) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpensesTopAppBar(
    onDateClicked: () -> Unit,
    onAnalyticsClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
) {
    val dimensions = YaMoneyDesign.dimensions

    TopAppBar(
        modifier = Modifier.height(dimensions.topBarHeight),
        title = {
            Text(
                text = stringResource(R.string.expenses_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        actions = {
            TextButton(onClick = onDateClicked) {
                Text(text = stringResource(R.string.expenses_selected_date))
            }
            IconButton(onClick = onAnalyticsClicked) {
                Icon(
                    imageVector = Icons.Outlined.Analytics,
                    contentDescription = stringResource(R.string.expenses_analytics_content_description),
                )
            }
            IconButton(onClick = onSettingsClicked) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = stringResource(R.string.expenses_settings_content_description),
                )
            }
        },
    )
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
                balance = state
            )
            Column(
                modifier = Modifier.padding(
                    horizontal = dimensions.screenHorizontalPadding,
                    vertical = dimensions.screenVerticalPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(dimensions.space4),
            ) {
                Text(
                    text = stringResource(R.string.expenses_summary_title),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(R.string.expenses_total, state.total),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        }
        items(items = state.items, key = ExpenseItemUi::id) { item ->
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

@Preview(showBackground = true)
@Composable
private fun ExpensesContentPreview() {
    YaMoneyTheme {
        ExpensesContent(
            state = ExpensesState.Content(
                total = "1 765,50",
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
