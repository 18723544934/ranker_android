package com.perftop.android.presentation.detail.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.perftop.android.domain.model.BenchmarkMetric
import kotlin.math.cos
import kotlin.math.sin

data class RadarData(
    val metric: String,
    val value: Float,
    val maxValue: Float = 100f
)

@Composable
fun RadarChart(
    data: List<RadarData>,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    gridColor: Color = MaterialTheme.colorScheme.outlineVariant,
    dataColor: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(
        modifier = modifier.size(300.dp)
    ) {
        val size = size.width
        val center = Offset(size / 2, size / 2)
        val radius = size / 2 - 40.dp.toPx()

        // Draw grid
        drawRadarGrid(center, radius, data.size, gridColor)

        // Draw data
        drawRadarData(center, radius, data, dataColor)
    }
}

private fun androidx.compose.ui.graphics.drawscope.Canvas.drawRadarGrid(
    center: Offset,
    radius: Float,
    sides: Int,
    color: Color
) {
    val angleStep = (2 * Math.PI / sides).toFloat()

    // Draw concentric pentagons
    for (level in 1..4) {
        val levelRadius = radius * level / 4
        val path = Path().apply {
            for (i in 0 until sides) {
                val angle = angleStep * i - Math.PI / 2
                val x = center.x + levelRadius * cos(angle).toFloat()
                val y = center.y + levelRadius * sin(angle).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        drawPath(path, color = color, style = Stroke(1.dp.toPx()))
    }

    // Draw lines from center
    for (i in 0 until sides) {
        val angle = angleStep * i - Math.PI / 2
        val x = center.x + radius * cos(angle).toFloat()
        val y = center.y + radius * sin(angle).toFloat()
        drawLine(center, Offset(x, y), color = color, style = Stroke(1.dp.toPx()))
    }
}

private fun androidx.compose.ui.graphics.drawscope.Canvas.drawRadarData(
    center: Offset,
    radius: Float,
    data: List<RadarData>,
    color: Color
) {
    if (data.isEmpty()) return

    val sides = data.size
    val angleStep = (2 * Math.PI / sides).toFloat()

    val path = Path().apply {
        data.forEachIndexed { index, item ->
            val normalizedValue = (item.value / item.maxValue).coerceIn(0f, 1f)
            val currentRadius = radius * normalizedValue
            val angle = angleStep * index - Math.PI / 2
            val x = center.x + currentRadius * cos(angle).toFloat()
            val y = center.y + currentRadius * sin(angle).toFloat()

            if (index == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }

    drawPath(
        path = path,
        color = color.copy(alpha = 0.3f),
        style = Stroke(2.dp.toPx())
    )

    // Draw points
    data.forEachIndexed { index, item ->
        val normalizedValue = (item.value / item.maxValue).coerceIn(0f, 1f)
        val currentRadius = radius * normalizedValue
        val angle = angleStep * index - Math.PI / 2
        val x = center.x + currentRadius * cos(angle).toFloat()
        val y = center.y + currentRadius * sin(angle).toFloat()

        drawCircle(
            color = color,
            radius = 6.dp.toPx(),
            center = Offset(x, y)
        )
    }
}
