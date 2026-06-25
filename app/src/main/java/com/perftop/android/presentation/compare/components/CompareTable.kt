package com.perftop.android.presentation.compare.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.perftop.android.core.theme.ScoreHigh
import com.perftop.android.core.theme.ScoreLow
import com.perftop.android.domain.model.Hardware

@Composable
fun CompareTable(
    hardwares: List<Hardware>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "规格对比",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.width(100.dp))
                hardwares.forEach { hardware ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = hardware.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Divider()

            // Data Rows
            CompareRow("品牌", hardwares.map { it.brand })
            CompareRow("架构", hardwares.map { it.architecture })
            CompareRow("综合评分", hardwares.map { it.overallScore.toInt().toString() }, highlightBest = true)
            CompareRow("核心数", hardwares.map { it.specs?.cores?.toString() ?: "N/A" })
            CompareRow("线程数", hardwares.map { it.specs?.threads?.toString() ?: "N/A" })
            CompareRow("基础频率", hardwares.map { "${it.specs?.baseClockGHz ?: "N/A"} GHz" })
            CompareRow("加速频率", hardwares.map { "${it.specs?.boostClockGHz ?: "N/A"} GHz" })
            CompareRow("TDP", hardwares.map { "${it.specs?.tdpWatts ?: "N/A"} W" })
            CompareRow("制程", hardwares.map { it.specs?.lithography ?: "N/A" })

            // Benchmark comparisons
            hardwares.flatMap { it.benchmarks }.map { it.metric }.distinct().forEach { metric ->
                val values = hardwares.map { hardware ->
                    hardware.benchmarks.find { it.metric == metric }?.score?.toInt()?.toString() ?: "N/A"
                }
                CompareRow(metric, values, highlightBest = true)
            }
        }
    }
}

@Composable
private fun CompareRow(
    label: String,
    values: List<String>,
    highlightBest: Boolean = false
) {
    val bestValueIndex = if (highlightBest && values.isNotEmpty()) {
        values.mapNotNull { it.toIntOrNull() }.indices.maxByOrNull { values[it].toIntOrNull() ?: 0 }
    } else null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label
        Box(
            modifier = Modifier.width(100.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Values
        values.forEachIndexed { index, value ->
            val isBest = index == bestValueIndex
            val isWorst = if (highlightBest && bestValueIndex != null) {
                values.mapNotNull { it.toIntOrNull() }.indices.minByOrNull { values[it].toIntOrNull() ?: Int.MAX_VALUE } == index
            } else false

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .then(
                        if (isBest) {
                            Modifier.background(
                                ScoreHigh.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                        } else if (isWorst) {
                            Modifier.background(
                                ScoreLow.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isBest) FontWeight.Bold else FontWeight.Normal,
                    color = if (isBest) ScoreHigh else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 8.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}
