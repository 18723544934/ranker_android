package com.perftop.android.presentation.compare.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.perftop.android.core.theme.ScoreHigh
import com.perftop.android.core.theme.ScoreMedium
import com.perftop.android.domain.model.Hardware

@Composable
fun CompareBarChart(
    hardwares: List<Hardware>,
    metric: String = "综合评分",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "性能对比 - $metric",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            hardwares.forEach { hardware ->
                val score = when (metric) {
                    "综合评分" -> hardware.overallScore
                    else -> {
                        hardware.benchmarks.find { it.metric == metric }?.score ?: 0.0
                    }
                }

                val maxValue = hardwares.maxOfOrNull { h ->
                    when (metric) {
                        "综合评分" -> h.overallScore
                        else -> h.benchmarks.find { it.metric == metric }?.score ?: 0.0
                    }
                } ?: 1.0

                CompareBarRow(
                    name = hardware.name,
                    score = score,
                    maxValue = maxValue
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun CompareBarRow(
    name: String,
    score: Double,
    maxValue: Double
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (maxValue > 0) (score / maxValue).toFloat() else 0f,
        label = "progress"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(100.dp),
            maxLines = 1
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = score.toInt().toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(50.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(4.dp)
                )
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(
                        getScoreColor(score),
                        RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

private fun getScoreColor(score: Double): Color {
    return when {
        score >= 8000 -> ScoreHigh
        score >= 5000 -> ScoreMedium
        else -> MaterialTheme.colorScheme.primary
    }
}
