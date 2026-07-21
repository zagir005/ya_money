package com.zagirlek.analytics.ui.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zagirlek.systemdesign.theme.YaMoneyDesign
import com.zagirlek.systemdesign.theme.YaMoneyTheme
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

@Composable
fun CategoryProgressItem(
    category: AnalyticsCategorySummary,
    totalAmount: BigDecimal,
    modifier: Modifier = Modifier,
) {
    val dimensions = YaMoneyDesign.dimensions
    val progress = category.amount.toProgressOf(totalAmount)
    val percentage = category.amount.toPercentageOf(totalAmount)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensions.space8),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensions.space12),
        ) {
            Box(
                modifier = Modifier
                    .size(dimensions.analyticsCategoryColorSize)
                    .clip(CircleShape)
                    .background(category.color),
            )

            Text(
                text = category.categoryName,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
            )

            Text(
                text = category.amountText,
                style = MaterialTheme.typography.titleLarge,
            )

            Text(
                text = "($percentage%)",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleLarge,
            )
        }

        AnalyticsProgressTrack(
            progress = progress,
            color = category.color,
        )
    }
}

private fun BigDecimal.toProgressOf(total: BigDecimal): Float {
    if (total.signum() <= 0) return 0f

    return divide(total, MathContext.DECIMAL64)
        .toFloat()
        .coerceIn(0f, 1f)
}

private fun BigDecimal.toPercentageOf(total: BigDecimal): Int {
    if (total.signum() <= 0) return 0

    return multiply(BigDecimal(100))
        .divide(total, 0, RoundingMode.HALF_UP)
        .toInt()
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun CategoryProgressItemPreview() {
    YaMoneyTheme {
        CategoryProgressItem(
            category = AnalyticsCategorySummary(
                categoryId = 1,
                categoryName = "Ремонт",
                categoryEmoji = "🔧",
                amount = BigDecimal("80200"),
                amountText = "80 200 ₽",
                color = Color(0xFFA98DF1),
            ),
            totalAmount = BigDecimal("132244"),
            modifier = Modifier.padding(16.dp),
        )
    }
}
