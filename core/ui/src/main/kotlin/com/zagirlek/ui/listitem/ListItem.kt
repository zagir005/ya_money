package com.zagirlek.ui.listitem

import androidx.compose.runtime.Immutable

/**
 * UI model for a row consisting of lead, content and trail blocks.
 * It is intentionally presentation-specific and is not a finance domain model.
 */
@Immutable
data class ListItem(
    val id: String,
    val lead: ListItemLead,
    val content: ListItemContent,
    val trail: ListItemTrail,
)

@Immutable
sealed interface ListItemLead {
    data class Emoji(val value: String) : ListItemLead
}

@Immutable
data class ListItemContent(
    val title: String,
    val subtitle: String?,
)

@Immutable
data class ListItemTrail(
    val tag: String?,
    val text: String,
)
