package com.perftop.android.presentation.ranking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.perftop.android.presentation.ranking.components.HardwareListItem
import com.perftop.android.core.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(
    viewModel: RankingViewModel = hiltViewModel(),
    onNavigateToDetail: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val hardwares = viewModel.hardwares.collectAsLazyPagingItems()
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("性能排行榜") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                // Category Tabs
                CategoryTabRow(
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = viewModel::onCategoryChange
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Open search */ }
            ) {
                Icon(Icons.Default.Search, contentDescription = "搜索")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && hardwares.itemCount == 0 -> {
                    LoadingView()
                }
                uiState.error != null -> {
                    ErrorView(
                        error = uiState.error,
                        onRetry = viewModel::onRefresh
                    )
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = hardwares,
                            key = { itemKey(it) },
                            contentType = { itemContentType(it) }
                        ) { hardware ->
                            hardware?.let {
                                var isFavorite by remember { mutableStateOf(it.isFavorite) }
                                HardwareListItem(
                                    hardware = it,
                                    rank = hardwares.indexOf(it) + 1,
                                    isFavorite = isFavorite,
                                    onFavoriteToggle = { id ->
                                        isFavorite = !isFavorite
                                        viewModel.toggleFavorite(id)
                                    },
                                    onClick = { onNavigateToDetail(it.id) }
                                )
                            }
                        }
                    }
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
        contentColor = MaterialTheme.colorScheme.onSurface,
        divider = {
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
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

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorView(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Button(onClick = onRetry) {
                Text("重试")
            }
        }
    }
}
