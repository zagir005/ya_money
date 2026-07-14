package com.zagirlek.ui.components

import androidx.compose.foundation.background
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
import com.zagirlek.systemdesign.theme.YaMoneyDesign

@Composable
fun FinanceListItem(
    lead: String,
    content: String,
    trail: String,
    trailTag: String,
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
            modifier = modifier
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
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
            text = listOf(trail, trailTag)
                .filter(String::isNotBlank)
                .joinToString(separator = " "),
            modifier = Modifier.widthIn(min = dimensions.space40),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.End,
        )
    }
}
