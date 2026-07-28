package com.zagirlek.analytics.ui.summary

import androidx.compose.ui.graphics.Color

object AnalyticsCategoryColorResolver {
    private const val paletteSize = 30
    private const val goldenAngle = 137.508f
    private const val firstHue = 134.492f

    private val palette = List(paletteSize) { index ->
        Color.hsv(
            hue = (firstHue + index * goldenAngle) % 360f,
            saturation = 0.58f + (index % 3) * 0.04f,
            value = 0.82f + (index % 2) * 0.06f,
        )
    }

    fun resolve(categoryId: Int): Color = palette[Math.floorMod(categoryId, palette.size)]
}
