package com.zagirlek.analytics.ui.chart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.tooling.preview.Preview
import com.zagirlek.analytics.ui.summary.AnalyticsCategoryColorResolver
import com.zagirlek.systemdesign.theme.YaMoneyDesign
import com.zagirlek.systemdesign.theme.YaMoneyTheme
import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private val AcidChartColors = listOf(Color.Magenta, Color.Cyan, Color.Yellow, Color.Green)
private val OrbitEmoji = listOf("💸", "🔥", "🤑", "🚀", "💀", "📈")
private val PartyWords = listOf("ДЕНЬГИ!!!", "СТАТИСТИКА!!!", "ВАУ!!!")

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
    val revealProgress = remember { Animatable(0f) }
    val partyTransition = rememberInfiniteTransition(label = "analyticsChartParty")
    val idleRotation by partyTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_600, easing = LinearEasing),
        ),
        label = "analyticsChartRotation",
    )
    val pulse by partyTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 520, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "analyticsChartPulse",
    )
    val glowAlpha by partyTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.32f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "analyticsChartGlow",
    )
    val horizontalSquash by partyTransition.animateFloat(
        initialValue = 0.68f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 730, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "analyticsChartHorizontalSquash",
    )
    val verticalSquash by partyTransition.animateFloat(
        initialValue = 1.24f,
        targetValue = 0.72f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 910, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "analyticsChartVerticalSquash",
    )
    val flipX by partyTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3_700, easing = LinearEasing),
        ),
        label = "analyticsChartFlipX",
    )
    val flipY by partyTransition.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4_900, easing = LinearEasing),
        ),
        label = "analyticsChartFlipY",
    )
    val chaosPhase by partyTransition.animateFloat(
        initialValue = 0f,
        targetValue = (PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_900, easing = LinearEasing),
        ),
        label = "analyticsChartChaosPhase",
    )
    val acidColorMix by partyTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 680, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "analyticsChartAcidColors",
    )
    val earthquake by partyTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 260, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "analyticsChartEarthquake",
    )
    val catastropheScale by partyTransition.animateFloat(
        initialValue = 0.52f,
        targetValue = 1.48f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 640, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "analyticsChartCatastropheScale",
    )

    LaunchedEffect(segments) {
        revealProgress.snapTo(0f)
        revealProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.32f,
                stiffness = 55f,
            ),
        )
    }

    Box(
        modifier = modifier
            .size(dimensions.analyticsChartDiameter)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    rotationX = flipX
                    rotationY = flipY
                    scaleX = pulse * horizontalSquash * catastropheScale
                    scaleY = pulse * verticalSquash * catastropheScale
                    translationX = (
                        sin(chaosPhase) * dimensions.space32.toPx() +
                            earthquake * dimensions.space12.toPx()
                        )
                    translationY = (
                        cos(chaosPhase * 1.7f) * dimensions.space32.toPx() -
                            earthquake * dimensions.space12.toPx()
                        )
                    cameraDistance = 6f * density
                },
        ) {
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

            repeat(18) { index ->
                val beamAngle = chaosPhase * 4f + (PI.toFloat() * 2f / 18f) * index
                val beamColor = AcidChartColors[index % AcidChartColors.size]
                val innerRadius = size.minDimension * 0.08f
                val outerRadius = size.minDimension * (0.38f + (sin(beamAngle * 3f) + 1f) * 0.28f)
                val beamStart = center + Offset(
                    x = cos(beamAngle) * innerRadius,
                    y = sin(beamAngle) * innerRadius,
                )
                val beamEnd = center + Offset(
                    x = cos(beamAngle) * outerRadius,
                    y = sin(beamAngle) * outerRadius,
                )
                drawLine(
                    color = beamColor.copy(alpha = 0.5f + glowAlpha / 2f),
                    start = beamStart,
                    end = beamEnd,
                    strokeWidth = strokeWidth * 0.22f,
                )
                drawCircle(
                    color = beamColor,
                    radius = strokeWidth * (0.16f + index % 3 * 0.12f),
                    center = beamEnd,
                )
            }

            var startAngle = -90f
            segments.forEachIndexed { index, segment ->
                if (segment.amount.signum() == 0) return@forEachIndexed

                val targetSweepAngle = segment.amount
                    .divide(totalAmount, MathContext.DECIMAL64)
                    .toFloat() * 360f
                val segmentProgress = (
                    revealProgress.value * (segments.size + 1) - index
                ).coerceIn(0f, 1f)
                val sweepAngle = targetSweepAngle * segmentProgress
                val animatedColor = lerp(
                    start = segment.color,
                    stop = AcidChartColors[index % AcidChartColors.size],
                    fraction = acidColorMix,
                )

                if (sweepAngle > 0f) {
                    drawArc(
                        color = animatedColor.copy(alpha = glowAlpha),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = Size(
                            width = size.width - strokeWidth,
                            height = size.height - strokeWidth,
                        ),
                        style = Stroke(
                            width = strokeWidth * 1.45f,
                            cap = StrokeCap.Round,
                        ),
                    )

                    drawArc(
                        color = animatedColor,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = Size(
                            width = size.width - strokeWidth,
                            height = size.height - strokeWidth,
                        ),
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                        ),
                    )
                }
                startAngle += targetSweepAngle
            }
        }

        Box(
            modifier = Modifier.graphicsLayer {
                val centerScale = 0.72f + revealProgress.value.coerceIn(0f, 1f) * 0.28f
                rotationZ = -idleRotation * 1.7f
                scaleX = centerScale * verticalSquash
                scaleY = centerScale * horizontalSquash
            },
            contentAlignment = Alignment.Center,
            content = centerContent,
        )

        OrbitEmoji.forEachIndexed { index, emoji ->
            val angle = chaosPhase + (PI.toFloat() * 2f / OrbitEmoji.size) * index
            Text(
                text = emoji,
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        val orbitRadius = dimensions.analyticsChartDiameter.toPx() * 0.58f
                        translationX = cos(angle) * orbitRadius
                        translationY = sin(angle * 1.35f) * orbitRadius
                        rotationZ = idleRotation * (if (index % 2 == 0) 2f else -3f)
                        val emojiPulse = 0.65f + (sin(angle * 3f) + 1f) * 0.45f
                        scaleX = emojiPulse
                        scaleY = emojiPulse
                    },
                style = MaterialTheme.typography.headlineMedium,
            )
        }

        PartyWords.forEachIndexed { index, word ->
            Text(
                text = word,
                color = AcidChartColors[index % AcidChartColors.size],
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        val wordAngle = chaosPhase * (index + 2) + index * 1.7f
                        translationX = cos(wordAngle) * dimensions.analyticsChartDiameter.toPx() * 0.72f
                        translationY = sin(wordAngle * 1.8f) * dimensions.analyticsChartDiameter.toPx() * 0.72f
                        rotationZ = idleRotation * (index + 1) * -2f
                        rotationX = flipY
                        rotationY = flipX
                        scaleX = catastropheScale
                        scaleY = pulse * 1.4f
                    },
                style = MaterialTheme.typography.headlineSmall,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 360)
@Composable
private fun AnalyticsDonutChartPreview() {
    YaMoneyTheme {
        AnalyticsDonutChart(
            segments = listOf(
                AnalyticsChartSegment(
                    1,
                    "Ремонт",
                    BigDecimal("80200"),
                    AnalyticsCategoryColorResolver.resolve(1),
                ),
                AnalyticsChartSegment(
                    2,
                    "Транспорт",
                    BigDecimal("33744"),
                    AnalyticsCategoryColorResolver.resolve(2),
                ),
                AnalyticsChartSegment(
                    3,
                    "Продукты",
                    BigDecimal("18300"),
                    AnalyticsCategoryColorResolver.resolve(3),
                ),
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
