package com.zagirlek.analytics.ui

private val categoryFallbackEmojis = listOf(
    "🛍️", "🏠", "🚕", "🍔", "🎮", "🐶", "✈️", "💊", "📚", "🎁",
    "☕", "🎬", "👕", "🛠️", "🏋️", "🎓", "📱", "💡", "🌿", "🚗",
)

private val accountFallbackEmojis = listOf("💳", "🏦", "🏧", "💰", "🪙", "💵")

fun resolveCategoryEmoji(categoryId: Int, emoji: String): String =
    emoji.takeIf(String::isNotBlank)
        ?: categoryFallbackEmojis[Math.floorMod(categoryId, categoryFallbackEmojis.size)]

fun resolveAccountEmoji(accountId: String, emoji: String): String =
    emoji.takeIf(String::isNotBlank)
        ?: accountFallbackEmojis[Math.floorMod(accountId.hashCode(), accountFallbackEmojis.size)]
