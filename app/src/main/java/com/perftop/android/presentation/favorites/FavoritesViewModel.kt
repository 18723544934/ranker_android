package com.perftop.android.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.perftop.android.data.repository.FavoriteRepository
import com.perftop.android.data.repository.HardwareRepository
import com.perftop.android.domain.model.Hardware
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val isLoading: Boolean = false,
    val selectedGroup: String = "全部",
    val availableGroups: List<String> = listOf("全部", "默认"),
    val error: String? = null
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val hardwareRepository: HardwareRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    val favorites: Flow<PagingData<Hardware>> by lazy {
        favoriteRepository.getAllFavorites()
            .cachedIn(viewModelScope)
    }

    init {
        loadGroups()
    }

    fun onGroupChange(group: String) {
        _uiState.value = _uiState.value.copy(selectedGroup = group)
    }

    fun removeFavorite(hardwareId: Int) {
        viewModelScope.launch {
            favoriteRepository.removeFavorite(hardwareId)
        }
    }

    fun removeGroup(group: String) {
        viewModelScope.launch {
            favoriteRepository.removeFavoritesByGroup(group)
            loadGroups()
        }
    }

    private fun loadGroups() {
        viewModelScope.launch {
            try {
                val groups = favoriteRepository.getAllGroups()
                _uiState.value = _uiState.value.copy(
                    availableGroups = listOf("全部") + groups
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "加载分组失败"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
