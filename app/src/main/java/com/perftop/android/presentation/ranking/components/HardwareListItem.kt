package com.perftop.android.presentation.ranking.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.perftop.android.core.theme.ScoreHigh
import com.perftop.android.core.theme.ScoreLow
import com.perftop.android.core.theme.ScoreMedium
import com.perftop.android.domain.model.Hardware

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HardwareListItem(
    hardware: Hardware,
    rank: Int,
    isFavorite: Boolean,
    onFavoriteToggle: (Int) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank
            Text(
                text = "$rank",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = getRankColor(rank),
                modifier = Modifier.width(40.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Hardware Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = hardware.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${hardware.brand} · ${hardware.architecture}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Score Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "综合评分: ${hardware.overallScore.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.widthIn(min = 120.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    LinearProgressIndicator(
                        progress = { hardware.overallScore / 10000f },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = getScoreColor(hardware.overallScore),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Favorite Button
            IconButton(onClick = { onFavoriteToggle(hardware.id) }) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "取消收藏" else "收藏",
                    tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun getRankColor(rank: Int): Color {
    return when (rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> MaterialTheme.colorScheme.onSurface
    }
}

private fun getScoreColor(score: Double): Color {
    return when {
        score >= 8000 -> ScoreHigh
        score >= 5000 -> ScoreMedium
        else -> ScoreLow
    }
}
