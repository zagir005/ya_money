package com.zagirlek.accounts

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.zagirlek.systemdesign.theme.YaMoneyDesign
import java.util.Currency
import java.util.Locale

@Composable
internal fun AccountSaveButton(
    enabled: Boolean,
    isSaving: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimensions = YaMoneyDesign.dimensions
    val containerColor = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (enabled) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        modifier = modifier.size(dimensions.fabSize),
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = if (enabled) dimensions.space4 else dimensions.space2,
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(dimensions.iconSize),
                    color = contentColor,
                    strokeWidth = dimensions.space2,
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = stringResource(R.string.account_editor_save),
                )
            }
        }
    }
}

@Composable
internal fun AccountAmountInput(
    value: String,
    currency: String,
    onValueChange: (String) -> Unit,
    allowNegative: Boolean,
) {
    val dimensions = YaMoneyDesign.dimensions
    val focusManager = LocalFocusManager.current
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
                    val filtered = changed.filterIndexed { index, character ->
                        character.isDigit() ||
                            character == '.' ||
                            character == ',' ||
                            (allowNegative && character == '-' && index == 0)
                    }
                    if (filtered.length <= MaxAmountLength) onValueChange(filtered)
                },
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.displayLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() },
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                singleLine = true,
            )
            Spacer(modifier = Modifier.width(dimensions.space8))
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
internal fun AccountEditorRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: (() -> Unit)? = null,
) {
    val dimensions = YaMoneyDesign.dimensions
    val interactionSource = remember { MutableInteractionSource() }
    val focusManager = LocalFocusManager.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensions.transactionEditorRowHeight)
            .then(
                if (onClick == null) {
                    Modifier
                } else {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {
                            focusManager.clearFocus()
                            onClick()
                        },
                    )
                },
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

internal fun currencySymbol(code: String): String = runCatching {
    Currency.getInstance(code).getSymbol(Locale.forLanguageTag("ru-RU"))
}.getOrDefault(code)

private const val MaxAmountLength = 18
