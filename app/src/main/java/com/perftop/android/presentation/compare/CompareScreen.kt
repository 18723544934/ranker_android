package com.perftop.android.presentation.compare

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.perftop.android.presentation.compare.components.CompareBarChart
import com.perftop.android.presentation.compare.components.CompareSelector
import com.perftop.android.presentation.compare.components.CompareTable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    onNavigateToDetail: (Int) -> Unit,
    viewModel: CompareViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("硬件对比") },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Navigate back */ }) {
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
        ) {
            // Compare Selector
            CompareSelector(
                selectedHardwareIds = uiState.selectedHardwareIds,
                hardwares = uiState.hardwares,
                onRemove = viewModel::removeFromComparison,
                onClearAll = viewModel::clearSelection,
                modifier = Modifier.padding(16.dp)
            )

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = uiState.error,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = viewModel::refresh) {
                                Text("重试")
                            }
                        }
                    }
                }
                uiState.hardwares.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "请选择要对比的硬件",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(onClick = { /* TODO: Open hardware selector */ }) {
                                Text("选择硬件")
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            CompareTable(
                                hardwares = uiState.hardwares
                            )
                        }

                        item {
                            CompareBarChart(
                                hardwares = uiState.hardwares,
                                metric = "综合评分"
                            )
                        }

                        // Add benchmark charts for each metric
                        uiState.hardwares.firstOrNull()?.benchmarks?.map { it.metric }?.distinct()?.forEach { metric ->
                            item {
                                CompareBarChart(
                                    hardwares = uiState.hardwares,
                                    metric = metric
                                )
                            }
                        }

                        item {
                            // Hardware list for selection
                            Text(
                                text = "点击添加更多硬件到对比",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            // TODO: Add hardware selection list
                        }
                    }
                }
            }
        }
    }
}
