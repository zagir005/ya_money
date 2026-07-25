@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.zagirlek.transactions

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.zagirlek.systemdesign.theme.YaMoneyDesign
import com.zagirlek.ui.components.elements.BaseBottomSheet
import com.zagirlek.ui.components.elements.NetworkErrorAlert
import com.zagirlek.ui.components.elements.SelectionListItem
import com.zagirlek.ui.components.elements.SelectionListItemControl
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale

@Composable
fun TransactionEditorScreen(component: TransactionEditorComponent) {
    val state by component.state.collectAsState()

    BaseBottomSheet(
        onDismissRequest = { component.accept(TransactionEditorIntent.Dismissed) },
    ) {
        when (val value = state) {
            TransactionEditorState.Loading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            is TransactionEditorState.Error -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f),
                contentAlignment = Alignment.Center,
            ) {
                NetworkErrorAlert(
                    title = value.error.title,
                    message = value.error.message,
                    onRetryClicked = { component.accept(TransactionEditorIntent.Dismissed) },
                )
            }
            is TransactionEditorState.Content -> TransactionEditorContent(
                state = value,
                onIntent = component::accept,
            )
        }
    }
}

@Composable
private fun TransactionEditorContent(
    state: TransactionEditorState.Content,
    onIntent: (TransactionEditorIntent) -> Unit,
) {
    val dimensions = YaMoneyDesign.dimensions
    val selectedCategory = state.categories.firstOrNull { it.id == state.selectedCategoryId }
    val selectedAccount = state.accounts.firstOrNull { it.id == state.selectedAccountId }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f),
    ) {
        AmountInput(
            value = state.amountInput,
            currency = selectedAccount?.currency,
            onValueChange = { onIntent(TransactionEditorIntent.AmountChanged(it)) },
        )

        Spacer(modifier = Modifier.height(dimensions.space16))

        EditorRow(
            icon = Icons.Outlined.Sell,
            title = stringResource(R.string.transaction_editor_category),
            value = selectedCategory?.name ?: stringResource(R.string.transaction_editor_not_selected),
            onClick = {
                onIntent(
                    TransactionEditorIntent.SelectorOpened(
                        TransactionEditorSelector.Category,
                    ),
                )
            },
        )
        EditorRow(
            icon = Icons.Outlined.CalendarMonth,
            title = stringResource(R.string.transaction_editor_date),
            value = state.date.format(EditorDateFormatter),
            onClick = {
                onIntent(
                    TransactionEditorIntent.SelectorOpened(TransactionEditorSelector.Date),
                )
            },
        )
        EditorRow(
            icon = Icons.Outlined.CalendarMonth,
            title = stringResource(R.string.transaction_editor_time),
            value = state.time.format(EditorTimeFormatter),
            onClick = {
                onIntent(
                    TransactionEditorIntent.SelectorOpened(TransactionEditorSelector.Time),
                )
            },
        )
        EditorRow(
            icon = Icons.Outlined.CreditCard,
            title = stringResource(R.string.transaction_editor_account),
            value = selectedAccount?.name ?: stringResource(R.string.transaction_editor_not_selected),
            onClick = {
                onIntent(
                    TransactionEditorIntent.SelectorOpened(
                        TransactionEditorSelector.Account,
                    ),
                )
            },
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensions.fabSize)
                .offset(y = -(dimensions.fabSize / 2))
                .padding(end = dimensions.space16),
            contentAlignment = Alignment.CenterEnd,
        ) {
            FloatingActionButton(
                onClick = { onIntent(TransactionEditorIntent.SaveClicked) },
                modifier = Modifier.size(dimensions.fabSize),
                containerColor = MaterialTheme.colorScheme.onSurface,
                contentColor = MaterialTheme.colorScheme.surface,
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(dimensions.iconSize),
                        color = MaterialTheme.colorScheme.surface,
                        strokeWidth = dimensions.space2,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = stringResource(R.string.transaction_editor_save),
                    )
                }
            }
        }

        state.saveError?.let { message ->
            Text(
                text = message,
                modifier = Modifier.padding(horizontal = dimensions.space16),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }

    when (state.activeSelector) {
        TransactionEditorSelector.Category -> CategorySelectionSheet(state, onIntent)
        TransactionEditorSelector.Account -> AccountSelectionSheet(state, onIntent)
        TransactionEditorSelector.Date -> EditorDatePicker(state.date, onIntent)
        TransactionEditorSelector.Time -> EditorTimePicker(state.time, onIntent)
        null -> Unit
    }
}

@Composable
private fun AmountInput(
    value: String,
    currency: String?,
    onValueChange: (String) -> Unit,
) {
    val dimensions = YaMoneyDesign.dimensions
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensions.transactionEditorAmountHeight),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimensions.space2),
    ) {
        Row(
            modifier = Modifier.width(dimensions.transactionEditorAmountWidth),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = value,
                onValueChange = { changed ->
                    val filtered = changed.filter { it.isDigit() || it == '.' || it == ',' }
                    if (filtered.length <= MAX_AMOUNT_LENGTH) onValueChange(filtered)
                },
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.displayLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                singleLine = true,
            )
            Text(
                text = currencySymbol(currency),
                style = MaterialTheme.typography.displayLarge,
            )
        }
        HorizontalDivider(
            modifier = Modifier.width(dimensions.transactionEditorAmountWidth),
            thickness = dimensions.navigationDividerThickness,
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

@Composable
private fun EditorRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit,
) {
    val dimensions = YaMoneyDesign.dimensions
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensions.transactionEditorRowHeight)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(
                horizontal = dimensions.space16,
                vertical = dimensions.space12,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(dimensions.listLeadingSize)
                .border(
                    width = dimensions.navigationDividerThickness,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = MaterialTheme.shapes.extraLarge,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(dimensions.smallIconSize),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            text = title,
            modifier = Modifier
                .padding(start = dimensions.space16)
                .weight(1f),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = value,
            modifier = Modifier
                .border(
                    width = dimensions.navigationDividerThickness,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = MaterialTheme.shapes.extraLarge,
                )
                .padding(
                    horizontal = dimensions.space12,
                    vertical = dimensions.space4 + dimensions.space2,
                ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge,
        )
    }
    HorizontalDivider(
        thickness = dimensions.navigationDividerThickness,
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

@Composable
private fun CategorySelectionSheet(
    state: TransactionEditorState.Content,
    onIntent: (TransactionEditorIntent) -> Unit,
) {
    val dimensions = YaMoneyDesign.dimensions
    BaseBottomSheet(
        onDismissRequest = { onIntent(TransactionEditorIntent.SelectorDismissed) },
        title = stringResource(R.string.transaction_editor_category),
    ) {
        state.categories.forEach { category ->
            SelectionListItem(
                title = category.name,
                leadingEmoji = category.emoji,
                control = SelectionListItemControl.Checkmark(
                    isSelected = category.id == state.selectedCategoryId,
                ),
                onClick = {
                    onIntent(TransactionEditorIntent.CategorySelected(category.id))
                },
            )
        }
        Spacer(modifier = Modifier.height(dimensions.space24))
    }
}

@Composable
private fun AccountSelectionSheet(
    state: TransactionEditorState.Content,
    onIntent: (TransactionEditorIntent) -> Unit,
) {
    val dimensions = YaMoneyDesign.dimensions
    BaseBottomSheet(
        onDismissRequest = { onIntent(TransactionEditorIntent.SelectorDismissed) },
        title = stringResource(R.string.transaction_editor_account),
    ) {
        state.accounts.forEach { account ->
            SelectionListItem(
                title = account.name,
                subtitle = account.currency,
                leadingEmoji = account.emoji,
                control = SelectionListItemControl.Checkmark(
                    isSelected = account.id == state.selectedAccountId,
                ),
                onClick = {
                    onIntent(TransactionEditorIntent.AccountSelected(account.id))
                },
            )
        }
        Spacer(modifier = Modifier.height(dimensions.space24))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorDatePicker(
    date: LocalDate,
    onIntent: (TransactionEditorIntent) -> Unit,
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
    )
    DatePickerDialog(
        onDismissRequest = { onIntent(TransactionEditorIntent.SelectorDismissed) },
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedMillis = pickerState.selectedDateMillis ?: return@TextButton
                    onIntent(
                        TransactionEditorIntent.DateSelected(
                            Instant.ofEpochMilli(selectedMillis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate(),
                        ),
                    )
                },
            ) {
                Text(stringResource(R.string.transaction_editor_done))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onIntent(TransactionEditorIntent.SelectorDismissed) },
            ) {
                Text(stringResource(R.string.transaction_editor_cancel))
            }
        },
    ) {
        DatePicker(state = pickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorTimePicker(
    time: LocalTime,
    onIntent: (TransactionEditorIntent) -> Unit,
) {
    val pickerState = rememberTimePickerState(
        initialHour = time.hour,
        initialMinute = time.minute,
        is24Hour = true,
    )
    AlertDialog(
        onDismissRequest = { onIntent(TransactionEditorIntent.SelectorDismissed) },
        confirmButton = {
            TextButton(
                onClick = {
                    onIntent(
                        TransactionEditorIntent.TimeSelected(
                            LocalTime.of(pickerState.hour, pickerState.minute),
                        ),
                    )
                },
            ) {
                Text(stringResource(R.string.transaction_editor_done))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onIntent(TransactionEditorIntent.SelectorDismissed) },
            ) {
                Text(stringResource(R.string.transaction_editor_cancel))
            }
        },
        text = {
            TimePicker(state = pickerState)
        },
    )
}

private fun currencySymbol(code: String?): String = code?.let {
    runCatching {
        Currency.getInstance(it).getSymbol(Locale.forLanguageTag("ru-RU"))
    }.getOrDefault(it)
}.orEmpty()

private val EditorDateFormatter =
    DateTimeFormatter.ofPattern("d MMMM", Locale.forLanguageTag("ru-RU"))
private val EditorTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private const val MAX_AMOUNT_LENGTH = 18
