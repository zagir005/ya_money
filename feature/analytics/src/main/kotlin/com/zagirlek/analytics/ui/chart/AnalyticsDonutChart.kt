package com.zagirlek.analytics.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import com.zagirlek.systemdesign.theme.YaMoneyDesign
import com.zagirlek.systemdesign.theme.YaMoneyTheme
import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.min

@Immutable
data class AnalyticsChartSegment(
    val categoryId: Int,
    val categoryName: String,
    val amount: BigDecimal,
    val color: Color,
) {
    init {
        require(amount >= BigDecimal.ZERO) { "Размер сегмента не может быть меньше нуля" }
    }
}

@Composable
fun AnalyticsDonutChart(
    segments: List<AnalyticsChartSegment>,
    modifier: Modifier = Modifier,
    centerContent: @Composable BoxScope.() -> Unit,
) {
    val dimensions = YaMoneyDesign.dimensions
    val emptyRingColor = MaterialTheme.colorScheme.outlineVariant
    val totalAmount = segments.fold(BigDecimal.ZERO) { total, segment -> total + segment.amount }

    Box(
        modifier = modifier
            .size(dimensions.analyticsChartDiameter)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidth = min(
                dimensions.analyticsChartStrokeWidth.toPx(),
                size.minDimension,
            )
            val inset = strokeWidth / 2f

            if (totalAmount.signum() == 0) {
                drawArc(
                    color = emptyRingColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = Size(
                        width = size.width - strokeWidth,
                        height = size.height - strokeWidth,
                    ),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                )
                return@Canvas
            }

            var startAngle = -90f
            segments.forEach { segment ->
                if (segment.amount.signum() == 0) return@forEach

                val sweepAngle = segment.amount
                    .divide(totalAmount, MathContext.DECIMAL64)
                    .toFloat() * 360f

                drawArc(
                    color = segment.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = Size(
                        width = size.width - strokeWidth,
                        height = size.height - strokeWidth,
                    ),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                )
                startAngle += sweepAngle
            }
        }

        centerContent()
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 360)
@Composable
private fun AnalyticsDonutChartPreview() {
    YaMoneyTheme {
        AnalyticsDonutChart(
            segments = listOf(
                AnalyticsChartSegment(1, "Ремонт", BigDecimal("80200"), Color(0xFFA98DF1)),
                AnalyticsChartSegment(2, "Транспорт", BigDecimal("33744"), Color(0xFF56CBDC)),
                AnalyticsChartSegment(3, "Продукты", BigDecimal("18300"), Color(0xFFF18AB2)),
            ),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Всего за период",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = "132 244 ₽",
                    style = MaterialTheme.typography.displayLarge,
                )
            }
        }
    }
}
