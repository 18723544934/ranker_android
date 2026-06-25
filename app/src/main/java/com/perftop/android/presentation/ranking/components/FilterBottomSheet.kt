package com.perftop.android.presentation.ranking.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class FilterOption(
    val id: String,
    val name: String,
    val isSelected: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    onDismiss: () -> Unit,
    brands: List<String>,
    selectedBrands: Set<String>,
    onBrandToggle: (String) -> Unit,
    architectures: List<String>,
    selectedArchitectures: Set<String>,
    onArchitectureToggle: (String) -> Unit,
    coreMin: Int?,
    coreMax: Int?,
    onCoreRangeChange: (Int?, Int?) -> Unit,
    yearMin: Int?,
    yearMax: Int?,
    onYearRangeChange: (Int?, Int?) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        windowInsets = WindowInsets(0),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "筛选",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "关闭")
                }
            }

            HorizontalDivider()

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Brand Filter
                item {
                    FilterSection(
                        title = "品牌",
                        options = brands.map { brand ->
                            FilterOption(
                                id = brand,
                                name = brand,
                                isSelected = brand in selectedBrands
                            )
                        },
                        onOptionToggle = { onBrandToggle(it.id) }
                    )
                }

                // Architecture Filter
                item {
                    FilterSection(
                        title = "架构",
                        options = architectures.map { arch ->
                            FilterOption(
                                id = arch,
                                name = arch,
                                isSelected = arch in selectedArchitectures
                            )
                        },
                        onOptionToggle = { onArchitectureToggle(it.id) }
                    )
                }

                // Core Range Filter
                item {
                    RangeFilterSection(
                        title = "核心数",
                        minValue = coreMin,
                        maxValue = coreMax,
                        onRangeChange = onCoreRangeChange,
                        options = listOf(
                            "不限", "1-4", "4-8", "8-16", "16+"
                        )
                    )
                }

                // Year Range Filter
                item {
                    RangeFilterSection(
                        title = "年份",
                        minValue = yearMin,
                        maxValue = yearMax,
                        onRangeChange = onYearRangeChange,
                        options = (2020..2026).reversed().map { it.toString() }
                    )
                }
            }

            HorizontalDivider()

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("重置")
                }
                Button(
                    onClick = onApply,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("应用")
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    options: List<FilterOption>,
    onOptionToggle: (FilterOption) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = option.isSelected,
                    onClick = { onOptionToggle(option) },
                    label = { Text(option.name) }
                )
            }
        }
    }
}

@Composable
private fun RangeFilterSection(
    title: String,
    minValue: Int?,
    maxValue: Int?,
    onRangeChange: (Int?, Int?) -> Unit,
    options: List<String>
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = minValue?.toString() ?: "",
                onValueChange = { newValue ->
                    onRangeChange(newValue.toIntOrNull(), maxValue)
                },
                label = { Text("最小值") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            Text("至")

            OutlinedTextField(
                value = maxValue?.toString() ?: "",
                onValueChange = { newValue ->
                    onRangeChange(minValue, newValue.toIntOrNull())
                },
                label = { Text("最大值") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
    }
}

@Composable
private fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else null
    )
}
