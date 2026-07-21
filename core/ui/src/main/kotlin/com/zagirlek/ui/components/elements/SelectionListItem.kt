package com.zagirlek.ui.components.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zagirlek.systemdesign.theme.YaMoneyDesign
import com.zagirlek.systemdesign.theme.YaMoneyTheme

sealed interface SelectionListItemControl {
    val isSelected: Boolean

    data class Checkbox(override val isSelected: Boolean) : SelectionListItemControl

    data class Checkmark(override val isSelected: Boolean) : SelectionListItemControl

    data class CircularCheckmark(override val isSelected: Boolean) : SelectionListItemControl
}

@Composable
fun SelectionListItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingEmoji: String? = null,
    control: SelectionListItemControl? = null,
) {
    val dimensions = YaMoneyDesign.dimensions

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = dimensions.listItemMinHeight)
            .clickable(onClick = onClick)
            .padding(
                horizontal = dimensions.screenHorizontalPadding,
                vertical = dimensions.space8,
            ),
        horizontalArrangement = Arrangement.spacedBy(dimensions.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingEmoji?.let { emoji ->
            Box(
                modifier = Modifier
                    .size(dimensions.listLeadingSize)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = emoji)
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
            )

            subtitle?.let { value ->
                Text(
                    text = value,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        control?.let { SelectionControl(it, onClick) }
    }
}

@Composable
private fun SelectionControl(
    control: SelectionListItemControl,
    onClick: () -> Unit,
) {
    val dimensions = YaMoneyDesign.dimensions

    when (control) {
        is SelectionListItemControl.Checkbox -> Checkbox(
            checked = control.isSelected,
            onCheckedChange = { onClick() },
        )

        is SelectionListItemControl.Checkmark -> {
            if (control.isSelected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            } else {
                Spacer(modifier = Modifier.width(dimensions.iconSize))
            }
        }

        is SelectionListItemControl.CircularCheckmark -> Box(
            modifier = Modifier
                .size(dimensions.listLeadingSize)
                .clip(CircleShape)
                .then(
                    if (control.isSelected) {
                        Modifier.background(MaterialTheme.colorScheme.primary)
                    } else {
                        Modifier.border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (control.isSelected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun SelectionListItemCheckboxPreview() {
    YaMoneyTheme {
        SelectionListItem(
            title = "Ремонт",
            leadingEmoji = "🔧",
            control = SelectionListItemControl.Checkbox(isSelected = true),
            onClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun SelectionListItemAccountPreview() {
    YaMoneyTheme {
        SelectionListItem(
            title = "Сбербанк",
            subtitle = "Дебетовая карта",
            leadingEmoji = "🏦",
            control = SelectionListItemControl.Checkmark(isSelected = true),
            onClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun SelectionListItemCircularCheckmarkPreview() {
    YaMoneyTheme {
        SelectionListItem(
            title = "Расходы",
            control = SelectionListItemControl.CircularCheckmark(isSelected = true),
            onClick = {},
        )
    }
}
