package com.zagirlek.systemdesign.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zagirlek.systemdesign.foundation.YaMoneyDimensions
import com.zagirlek.systemdesign.foundation.LocalYaMoneyDimensions

private val LightColors = lightColorScheme(
    primary = YaMoneyLightPalette.Primary,
    onPrimary = YaMoneyLightPalette.OnPrimary,
    primaryContainer = YaMoneyLightPalette.PrimaryContainer,
    onPrimaryContainer = YaMoneyLightPalette.OnPrimaryContainer,
    secondary = YaMoneyLightPalette.Secondary,
    onSecondary = YaMoneyLightPalette.OnSecondary,
    secondaryContainer = YaMoneyLightPalette.SecondaryContainer,
    onSecondaryContainer = YaMoneyLightPalette.OnSecondaryContainer,
    background = YaMoneyLightPalette.Background,
    onBackground = YaMoneyLightPalette.OnBackground,
    surface = YaMoneyLightPalette.Surface,
    onSurface = YaMoneyLightPalette.OnSurface,
    surfaceVariant = YaMoneyLightPalette.SurfaceVariant,
    onSurfaceVariant = YaMoneyLightPalette.OnSurfaceVariant,
    outline = YaMoneyLightPalette.Outline,
    error = YaMoneyLightPalette.Error,
    onError = YaMoneyLightPalette.OnError,
    errorContainer = YaMoneyLightPalette.ErrorContainer,
    onErrorContainer = YaMoneyLightPalette.OnErrorContainer,
)

private val FinanceTypography = Typography(
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
)

private val FinanceShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/**
 * Application theme for the first release.
 *
 * Dynamic colours are deliberately disabled: the visual design must remain stable.
 * The function has no dark-theme switch until dark tokens and layouts are designed.
 */
@Composable
fun FinanceTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalYaMoneyDimensions provides YaMoneyDimensions()) {
        MaterialTheme(
            colorScheme = LightColors,
            typography = FinanceTypography,
            shapes = FinanceShapes,
            content = content,
        )
    }
}
