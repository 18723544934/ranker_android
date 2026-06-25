package com.perftop.android.presentation.ladder

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.ArrowBack
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.perftop.android.presentation.ladder.components.InteractiveLadderChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LadderScreen(
    onNavigateToDetail: (Int) -> Unit,
    viewModel: LadderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("性能天梯图") },
                    navigationIcon = {
                        IconButton(onClick = { /* TODO: Navigate back */ }) {
                            Icon(Icons.AutoMirrored.ArrowBack, contentDescription = "返回")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.onScaleChange(uiState.scale * 1.1f) }) {
                            Icon(Icons.Default.ZoomIn, contentDescription = "放大")
                        }
                        IconButton(onClick = { viewModel.onScaleChange(uiState.scale * 0.9f) }) {
                            Icon(Icons.Default.ZoomOut, contentDescription = "缩小")
                        }
                    }
                )

                // Category Tabs
                CategoryTabRow(
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = viewModel::onCategoryChange
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                        Text(
                            text = "暂无数据",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    InteractiveLadderChart(
                        hardwares = uiState.hardwares,
                        initialScale = uiState.scale,
                        onHardwareClick = onNavigateToDetail
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryTabRow(
    selectedCategory: com.perftop.android.domain.model.Category,
    onCategorySelected: (com.perftop.android.domain.model.Category) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = com.perftop.android.domain.model.Category.values().indexOf(selectedCategory),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        com.perftop.android.domain.model.Category.values().forEach { category ->
            Tab(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                text = {
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            )
        }
    }
}
