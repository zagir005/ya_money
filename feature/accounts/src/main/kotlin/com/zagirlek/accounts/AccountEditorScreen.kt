@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.zagirlek.accounts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import com.zagirlek.finance.api.money.CurrencyCode
import com.zagirlek.systemdesign.theme.YaMoneyDesign
import com.zagirlek.ui.components.elements.BaseBottomSheet
import com.zagirlek.ui.components.elements.SelectionListItem
import com.zagirlek.ui.components.elements.SelectionListItemControl

@Composable
fun AccountEditorScreen(component: AccountEditorComponent) {
    val state by component.state.collectAsState()

    BaseBottomSheet(
        onDismissRequest = { component.accept(AccountEditorIntent.Dismissed) },
    ) {
        when (val value = state) {
            AccountEditorState.Loading -> EditorLoading()
            is AccountEditorState.Content -> AccountEditorContent(
                state = value,
                onIntent = component::accept,
            )
        }
    }
}

@Composable
private fun AccountEditorContent(
    state: AccountEditorState.Content,
    onIntent: (AccountEditorIntent) -> Unit,
) {
    val dimensions = YaMoneyDesign.dimensions
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f)
            .navigationBarsPadding()
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = dimensions.space16,
                    end = dimensions.space16,
                    bottom = dimensions.fabSize + dimensions.space32,
                ),
        ) {
            Text(
                text = stringResource(
                    if (state.isCreating) {
                        R.string.account_editor_create_title
                    } else {
                        R.string.account_editor_edit_title
                    },
                ),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(dimensions.space16))

            Text(
                text = stringResource(R.string.account_editor_balance),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AccountAmountInput(
                value = state.balanceInput,
                currency = state.currency.value,
                onValueChange = { onIntent(AccountEditorIntent.BalanceChanged(it)) },
                allowNegative = true,
            )
            Spacer(modifier = Modifier.height(dimensions.space16))

            val isEmojiError =
                state.emojiInput.isNotEmpty() && !state.emojiInput.isSingleEmoji()
            OutlinedTextField(
                value = state.emojiInput,
                onValueChange = { onIntent(AccountEditorIntent.EmojiChanged(it)) },
                label = { Text(stringResource(R.string.account_editor_emoji)) },
                isError = isEmojiError,
                supportingText = if (isEmojiError) {
                    {
                        Text(stringResource(R.string.account_editor_emoji_error))
                    }
                } else {
                    null
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(dimensions.space12))
            OutlinedTextField(
                value = state.nameInput,
                onValueChange = { onIntent(AccountEditorIntent.NameChanged(it)) },
                label = { Text(stringResource(R.string.account_editor_name)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(dimensions.space16))

            AccountEditorRow(
                icon = Icons.Outlined.AccountBalanceWallet,
                title = stringResource(R.string.account_editor_currency),
                value = state.currency.value,
                onClick = {
                    onIntent(
                        AccountEditorIntent.SelectorOpened(AccountEditorSelector.Currency),
                    )
                },
            )

            state.saveError?.let { message ->
                Text(
                    text = message,
                    modifier = Modifier.padding(vertical = dimensions.space8),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        AccountSaveButton(
            enabled = state.isSaveEnabled,
            isSaving = state.isSaving,
            onClick = { onIntent(AccountEditorIntent.SaveClicked) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = dimensions.space16,
                    bottom = dimensions.space16,
                ),
        )
    }

    if (state.activeSelector == AccountEditorSelector.Currency) {
        CurrencySelectionSheet(state, onIntent)
    }
}

@Composable
private fun CurrencySelectionSheet(
    state: AccountEditorState.Content,
    onIntent: (AccountEditorIntent) -> Unit,
) {
    val dimensions = YaMoneyDesign.dimensions
    BaseBottomSheet(
        onDismissRequest = { onIntent(AccountEditorIntent.SelectorDismissed) },
        title = stringResource(R.string.account_editor_currency),
    ) {
        SupportedCurrencies.forEach { currency ->
            SelectionListItem(
                title = currency.value,
                subtitle = currencySymbol(currency.value),
                control = SelectionListItemControl.Checkmark(
                    isSelected = currency == state.currency,
                ),
                onClick = { onIntent(AccountEditorIntent.CurrencySelected(currency)) },
            )
        }
        Spacer(modifier = Modifier.height(dimensions.space24))
    }
}

@Composable
private fun EditorLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

private val SupportedCurrencies = listOf(
    CurrencyCode.RUB,
    CurrencyCode.USD,
    CurrencyCode.EUR,
)
