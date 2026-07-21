package com.zagirlek.analytics.ui.summary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.zagirlek.systemdesign.theme.YaMoneyDesign
import com.zagirlek.systemdesign.theme.YaMoneyTheme

@Composable
fun AnalyticsProgressTrack(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val dimensions = YaMoneyDesign.dimensions
    val trackColor = MaterialTheme.colorScheme.outlineVariant
    val normalizedProgress = progress.coerceIn(0f, 1f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(dimensions.analyticsCategoryProgressHeight),
    ) {
        val cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)

        drawRoundRect(
            color = trackColor,
            cornerRadius = cornerRadius,
        )
        drawRoundRect(
            color = color,
            size = Size(width = size.width * normalizedProgress, height = size.height),
            cornerRadius = cornerRadius,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun AnalyticsProgressTrackPreview() {
    YaMoneyTheme {
        AnalyticsProgressTrack(
            progress = 0.61f,
            color = Color(0xFFA98DF1),
        )
    }
}
