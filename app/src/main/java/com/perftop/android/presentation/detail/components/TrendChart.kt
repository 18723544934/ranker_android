package com.perftop.android.presentation.detail.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

data class TrendDataPoint(
    val timestamp: Long,
    val value: Float
)

@Composable
fun TrendChart(
    data: List<TrendDataPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
    ) {
        if (data.size < 2) return@Canvas

        val width = size.width
        val height = size.height
        val padding = 20.dp.toPx()

        val minValue = data.minOf { it.value }
        val maxValue = data.maxOf { it.value }
        val range = maxValue - minValue

        // Draw line
        val linePath = Path().apply {
            data.forEachIndexed { index, point ->
                val x = padding + (index.toFloat() / (data.size - 1)) * (width - 2 * padding)
                val y = height - padding - ((point.value - minValue) / range) * (height - 2 * padding)

                if (index == 0) {
                    moveTo(x, y)
                } else {
                    lineTo(x, y)
                }
            }
        }

        // Draw fill area
        val fillPath = Path().apply {
            linePath.forEach { linePathSegment ->
                // This is a simplified approach
            }
            lineTo(width - padding, height - padding)
            lineTo(padding, height - padding)
            close()
        }

        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(3.dp.toPx())
        )

        // Draw points
        data.forEachIndexed { index, point ->
            val x = padding + (index.toFloat() / (data.size - 1)) * (width - 2 * padding)
            val y = height - padding - ((point.value - minValue) / range) * (height - 2 * padding)

            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}
