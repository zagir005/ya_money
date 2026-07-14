package com.zagirlek.ui.components.finance

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zagirlek.systemdesign.theme.YaMoneyDesign
import com.zagirlek.systemdesign.theme.YaMoneyTheme

@Composable
fun FinanceListItem(
    lead: String,
    content: String,
    trail: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val dimensions = YaMoneyDesign.dimensions
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = dimensions.listItemMinHeight)
            .then(clickModifier)
            .padding(
                horizontal = dimensions.screenHorizontalPadding,
                vertical = dimensions.space8,
            ),
        horizontalArrangement = Arrangement.spacedBy(dimensions.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape,
                )
                .size(dimensions.listLeadingSize),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = lead)
        }

        Text(
            text = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )

        Text(
            text = trail,
            modifier = Modifier.widthIn(min = dimensions.space40),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.End,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FinanceListItemPreview() {
    YaMoneyTheme {
        FinanceListItem(
            lead = "🛒",
            content = "Продукты",
            trail = "1 280 ₽",
            onClick = null,
        )
    }
}
