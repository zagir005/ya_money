package com.zagirlek.ui.listitem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.zagirlek.systemdesign.foundation.FinanceDesign

@Composable
fun FinanceListItem(
    item: ListItem,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val dimensions = FinanceDesign.dimensions
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
        ListItemLeadContent(
            lead = item.lead,
            modifier = Modifier.size(dimensions.listLeadingSize),
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(dimensions.space2),
        ) {
            Text(
                text = item.content.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
            )
            item.content.subtitle?.let { subtitle ->
                Text(
                    text = subtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Text(
            text = listOfNotNull(item.trail.text, item.trail.tag).joinToString(" "),
            modifier = Modifier.widthIn(min = dimensions.space40),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun ListItemLeadContent(
    lead: ListItemLead,
    modifier: Modifier = Modifier,
) {
    when (lead) {
        is ListItemLead.Emoji -> Box(
            modifier = modifier.background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape,
            ),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = lead.value)
        }
    }
}
