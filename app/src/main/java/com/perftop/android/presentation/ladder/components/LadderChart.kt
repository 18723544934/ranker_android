package com.perftop.android.presentation.ladder.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.perftop.android.core.theme.ScoreHigh
import com.perftop.android.core.theme.ScoreLow
import com.perftop.android.core.theme.ScoreMedium
import com.perftop.android.domain.model.Hardware
import kotlin.math.max

@Composable
fun LadderChart(
    hardwares: List<Hardware>,
    scale: Float = 1f,
    onHardwareClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val maxScore = hardwares.maxOfOrNull { it.overallScore } ?: 1.0

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(hardwares) { hardware ->
            LadderItem(
                hardware = hardware,
                maxScore = maxScore,
                scale = scale,
                onClick = { onHardwareClick(hardware.id) }
            )
        }
    }
}

@Composable
fun LadderItem(
    hardware: Hardware,
    maxScore: Double,
    scale: Float = 1f,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val itemWidth = (hardware.overallScore / maxScore * 400 * scale).coerceAtLeast(100f)

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(8.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            // Name
            Text(
                text = hardware.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(150.dp),
                maxLines = 1
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        getBarColor(hardware.overallScore),
                        RoundedCornerShape(4.dp)
                    )
            ) {
                Text(
                    text = hardware.overallScore.toInt().toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxHeight(),
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Brand
            Text(
                text = hardware.brand,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getBarColor(score: Double): Color {
    return when {
        score >= 8000 -> ScoreHigh
        score >= 5000 -> ScoreMedium
        else -> ScoreLow
    }
}

@Composable
fun InteractiveLadderChart(
    hardwares: List<Hardware>,
    initialScale: Float = 1f,
    onHardwareClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(initialScale) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom ->
                    scale *= zoom
                    scale = scale.coerceIn(0.5f, 2f)
                }
            }
    ) {
        LadderChart(
            hardwares = hardwares,
            scale = scale,
            onHardwareClick = onHardwareClick,
            modifier = Modifier.fillMaxSize()
        )
    }
}
