package com.perftop.android.presentation.detail.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.perftop.android.domain.model.Specs

@Composable
fun SpecsTable(
    specs: Specs?,
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
                text = "规格参数",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (specs != null) {
                SpecRow("核心数", specs.cores?.toString() ?: "N/A")
                SpecRow("线程数", specs.threads?.toString() ?: "N/A")
                SpecRow("基础频率", "${specs.baseClockGHz?.toString() ?: "N/A"} GHz")
                SpecRow("加速频率", "${specs.boostClockGHz?.toString() ?: "N/A"} GHz")
                SpecRow("TDP", "${specs.tdpWatts?.toString() ?: "N/A"} W")
                SpecRow("制程", specs.lithography ?: "N/A")
                SpecRow("显存", "${specs.vramGB?.toString() ?: "N/A"} GB")
                SpecRow("显存类型", specs.memoryType ?: "N/A")
                SpecRow("带宽", "${specs.bandwidthGBs?.toString() ?: "N/A"} GB/s")
                SpecRow("缓存", specs.cache ?: "N/A")
            } else {
                Text(
                    text = "暂无规格数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SpecRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
