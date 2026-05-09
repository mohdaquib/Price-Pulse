package com.aquib.pricepulse.feature.price.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp

@Composable
fun PriceLineChart(
    prices: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF4CAF50),
) {
    if (prices.size < 2) return

    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

    val animProgress = remember { Animatable(1f) }
    LaunchedEffect(prices.size) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(durationMillis = 600))
    }

    val minPrice = remember(prices) { prices.min() }
    val maxPrice = remember(prices) { prices.max() }
    val priceRange = maxPrice - minPrice

    Canvas(modifier = modifier) {
        if (priceRange == 0f) return@Canvas

        val points = prices.mapIndexed { i, price ->
            Offset(
                x = i * size.width / (prices.size - 1),
                y = size.height * (1f - (price - minPrice) / priceRange)
            )
        }

        // Horizontal grid lines
        repeat(5) { i ->
            val y = size.height * i / 4f
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Smooth cubic Bézier line path
        val linePath = Path()
        linePath.moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            val prev = points[i - 1]
            val curr = points[i]
            val cx = (prev.x + curr.x) / 2f
            linePath.cubicTo(cx, prev.y, cx, curr.y, curr.x, curr.y)
        }

        // Fill path — close the shape along the bottom baseline
        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(points.last().x, size.height)
            lineTo(points.first().x, size.height)
            close()
        }

        clipRect(right = size.width * animProgress.value) {
            // Gradient fill under the line
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = 0.35f),
                        lineColor.copy(alpha = 0.02f)
                    ),
                    startY = 0f,
                    endY = size.height
                )
            )

            // Line stroke
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Dot at the latest price
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = points.last()
            )
            drawCircle(
                color = lineColor.copy(alpha = 0.25f),
                radius = 8.dp.toPx(),
                center = points.last()
            )
        }
    }
}
