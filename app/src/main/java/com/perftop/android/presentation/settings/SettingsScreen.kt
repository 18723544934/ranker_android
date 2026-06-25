package com.perftop.android.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.perftop.android.presentation.settings.components.ThemeSelector
import com.perftop.android.presentation.settings.components.WeightSlider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // About Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "PerfTop",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "版本 ${uiState.version}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Theme Settings
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                ThemeSelector(
                    darkTheme = uiState.darkTheme,
                    onThemeChange = viewModel::updateDarkTheme
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Update Strategy
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "数据更新策略",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SwitchSetting(
                        title = "仅 Wi-Fi 更新",
                        description = "只在连接 Wi-Fi 时自动更新数据",
                        checked = uiState.updateOnWifiOnly,
                        onCheckedChange = viewModel::updateWifiOnly
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weights Settings
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "评分权重设置",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(onClick = viewModel::resetWeights) {
                            Text("重置")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    WeightSlider(
                        title = "Geekbench",
                        weight = uiState.geekbenchWeight,
                        onWeightChange = { viewModel.updateWeight("Geekbench", it) }
                    )

                    WeightSlider(
                        title = "PassMark",
                        weight = uiState.passmarkWeight,
                        onWeightChange = { viewModel.updateWeight("PassMark", it) }
                    )

                    WeightSlider(
                        title = "3DMark",
                        weight = uiState.dmarkWeight,
                        onWeightChange = { viewModel.updateWeight("3DMark", it) }
                    )

                    WeightSlider(
                        title = "安兔兔",
                        weight = uiState.antutuWeight,
                        onWeightChange = { viewModel.updateWeight("Antutu", it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cache Management
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "缓存管理",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CacheItemRow(
                        title = "缓存数据",
                        description = "已缓存 ${uiState.cacheSize} 条记录",
                        actionText = "清除",
                        onClick = viewModel::clearCache,
                        isLoading = uiState.isLoading
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Data Sources
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "数据来源",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DataSourceRow(
                        name = "Geekbench Browser",
                        url = "https://browser.geekbench.com/"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    DataSourceRow(
                        name = "PassMark",
                        url = "https://www.cpubenchmark.net/"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    DataSourceRow(
                        name = "3DMark",
                        url = "https://www.3dmark.com/"
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SwitchSetting(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun CacheItemRow(
    title: String,
    description: String,
    actionText: String,
    onClick: () -> Unit,
    isLoading: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else {
            TextButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(actionText)
            }
        }
    }
}

@Composable
private fun DataSourceRow(
    name: String,
    url: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = url,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(onClick = { /* TODO: Open website */ }) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "刷新数据",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
