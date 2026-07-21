package com.zagirlek.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.zagirlek.accounts.R
import com.zagirlek.systemdesign.theme.YaMoneyDesign
import com.zagirlek.systemdesign.theme.YaMoneyTheme
import com.zagirlek.ui.components.elements.CenteredMessage
import com.zagirlek.ui.components.elements.ErrorContent
import com.zagirlek.ui.components.elements.LoadingContent
import com.zagirlek.ui.components.finance.BalanceCard
import com.zagirlek.ui.components.finance.FinanceListItem
import com.zagirlek.ui.components.finance.FinanceScaffold

@Composable
fun AccountsScreen(
    component: AccountsComponent,
    onAnalyticsClick: () -> Unit = {},
) {
    val state by component.state.collectAsState()

    AccountsContent(
        state = state,
        onIntent = component::accept,
        onAnalyticsClick = onAnalyticsClick,
    )
}

@Composable
fun AccountsContent(
    state: AccountsState,
    onIntent: (AccountsIntent) -> Unit,
    onAnalyticsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    FinanceScaffold(
        date = stringResource(R.string.accounts_example_date),
        onDateClick = { onIntent(AccountsIntent.DateClicked) },
        onAnalyticsClick = onAnalyticsClick,
        onSettingsClick = { onIntent(AccountsIntent.SettingsClicked) },
        onAddClick = { onIntent(AccountsIntent.AddClicked) },
        modifier = modifier,
    ) { scaffoldPadding ->
        when (state) {
            is AccountsState.Content -> AccountsList(
                state = state,
                scaffoldPadding = scaffoldPadding,
                onAccountClicked = { id -> onIntent(AccountsIntent.AccountClicked(id)) },
            )

            AccountsState.Loading -> AccountsStateContent(scaffoldPadding) {
                LoadingContent()
            }

            AccountsState.Empty -> AccountsStateContent(scaffoldPadding) {
                CenteredMessage(stringResource(R.string.accounts_empty))
            }

            AccountsState.Error -> AccountsStateContent(scaffoldPadding) {
                ErrorContent(onRetryClicked = { onIntent(AccountsIntent.RetryClicked) })
            }
        }
    }
}

@Composable
private fun AccountsList(
    state: AccountsState.Content,
    scaffoldPadding: PaddingValues,
    onAccountClicked: (String) -> Unit,
) {
    val dimensions = YaMoneyDesign.dimensions

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(scaffoldPadding),
        contentPadding = PaddingValues(bottom = dimensions.fabSize + dimensions.space32),
        verticalArrangement = Arrangement.spacedBy(dimensions.space4),
    ) {
        item {
            BalanceCard(
                title = stringResource(R.string.accounts_summary_title),
                balance = state.total,
            )
        }
        items(items = state.items, key = AccountItemUi::id) { item ->
            FinanceListItem(
                lead = item.lead,
                content = item.content,
                trail = item.trail,
                onClick = { onAccountClicked(item.id) },
            )
        }
    }
}

@Composable
private fun AccountsStateContent(
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

@Preview(showBackground = true)
@Composable
private fun AccountsContentPreview() {
    YaMoneyTheme {
        AccountsContent(
            state = AccountsState.Content(
                total = "157 350 ₽",
                items = listOf(
                    AccountItemUi(
                        id = "main",
                        lead = "💳",
                        content = "Основной счёт",
                        trail = "145 000 ₽",
                    ),
                    AccountItemUi(
                        id = "cash",
                        lead = "💵",
                        content = "Наличные",
                        trail = "12 350 ₽",
                    ),
                ),
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountsLoadingPreview() {
    YaMoneyTheme {
        AccountsContent(
            state = AccountsState.Loading,
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountsEmptyPreview() {
    YaMoneyTheme {
        AccountsContent(
            state = AccountsState.Empty,
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountsErrorPreview() {
    YaMoneyTheme {
        AccountsContent(
            state = AccountsState.Error,
            onIntent = {},
        )
    }
}
