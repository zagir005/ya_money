package com.zagirlek.systemdesign.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class YaMoneyDimensions(
    val space2: Dp = 2.dp,
    val space4: Dp = 4.dp,
    val space8: Dp = 8.dp,
    val space12: Dp = 12.dp,
    val space16: Dp = 16.dp,
    val space20: Dp = 20.dp,
    val space24: Dp = 24.dp,
    val space32: Dp = 32.dp,
    val space40: Dp = 40.dp,
    val screenHorizontalPadding: Dp = 16.dp,
    val screenVerticalPadding: Dp = 16.dp,
    val topBarHeight: Dp = 64.dp,
    val listItemMinHeight: Dp = 64.dp,
    val listLeadingSize: Dp = 40.dp,
    val navigationBarHeight: Dp = 80.dp,
    val navigationItemTouchTarget: Dp = 48.dp,
    val fabSize: Dp = 56.dp,
    val iconSize: Dp = 24.dp,
    val smallIconSize: Dp = 20.dp,
    val touchTargetMinSize: Dp = 48.dp,
)

internal val LocalYaMoneyDimensions = staticCompositionLocalOf { YaMoneyDimensions() }

object YaMoneyDesign {
    val dimensions: YaMoneyDimensions
        @Composable
        get() = LocalYaMoneyDimensions.current
}
