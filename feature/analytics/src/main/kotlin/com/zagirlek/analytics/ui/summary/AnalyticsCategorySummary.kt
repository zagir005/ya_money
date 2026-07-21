package com.zagirlek.analytics.ui.summary

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.zagirlek.analytics.ui.chart.AnalyticsChartSegment
import java.math.BigDecimal

@Immutable
data class AnalyticsCategorySummary(
    val categoryId: Int,
    val categoryName: String,
    val categoryEmoji: String,
    val amount: BigDecimal,
    val amountText: String,
    val color: Color,
) {
    init {
        require(amount >= BigDecimal.ZERO) { "Category amount cannot be negative." }
    }
}

fun AnalyticsCategorySummary.toChartSegment(): AnalyticsChartSegment = AnalyticsChartSegment(
    categoryId = categoryId,
    categoryName = categoryName,
    amount = amount,
    color = color,
)
