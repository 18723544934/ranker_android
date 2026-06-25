package com.perftop.android.presentation.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.perftop.android.presentation.favorites.components.FavoriteGroupSelector
import com.perftop.android.presentation.favorites.components.FavoriteItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onNavigateToDetail: (Int) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val favorites = viewModel.favorites.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的收藏") },
                actions = {
                    IconButton(onClick = { /* TODO: Open add to favorite dialog */ }) {
                        Icon(Icons.Default.Add, contentDescription = "添加收藏")
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
            // Group Selector
            FavoriteGroupSelector(
                selectedGroup = uiState.selectedGroup,
                groups = uiState.availableGroups,
                onGroupSelected = viewModel::onGroupChange
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Favorites List
            when {
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = uiState.error ?: "加载失败",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = viewModel::clearError) {
                                Text("重试")
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = favorites,
                            key = { itemKey(it) }
                        ) { hardware ->
                            hardware?.let {
                                FavoriteItem(
                                    hardware = it,
                                    groupName = if (uiState.selectedGroup == "全部") "默认" else uiState.selectedGroup,
                                    onDelete = { viewModel.removeFavorite(it.id) },
                                    onClick = { onNavigateToDetail(it.id) }
                                )
                            }
                        }

                        if (favorites.itemCount == 0) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Text(
                                            text = "暂无收藏",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Button(onClick = { /* TODO: Navigate to ranking */ }) {
                                            Text("去排行榜看看")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
