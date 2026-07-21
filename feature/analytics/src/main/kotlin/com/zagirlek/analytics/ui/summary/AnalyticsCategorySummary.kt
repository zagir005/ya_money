package com.zagirlek.analytics.ui.summary

import androidx.compose.runtime.Immutable
import com.zagirlek.analytics.ui.chart.AnalyticsChartSegment
import java.math.BigDecimal

@Immutable
data class AnalyticsCategorySummary(
    val categoryId: Int,
    val categoryName: String,
    val categoryEmoji: String,
    val amount: BigDecimal,
    val amountText: String,
) {
    init {
        require(amount >= BigDecimal.ZERO) { "Category amount cannot be negative." }
    }
}

val AnalyticsCategorySummary.color
    get() = AnalyticsCategoryColorResolver.resolve(categoryId)

fun AnalyticsCategorySummary.toChartSegment(): AnalyticsChartSegment = AnalyticsChartSegment(
    categoryId = categoryId,
    categoryName = categoryName,
    amount = amount,
    color = color,
)
