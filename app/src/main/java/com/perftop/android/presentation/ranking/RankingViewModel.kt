package com.perftop.android.presentation.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.perftop.android.data.repository.HardwareRepository
import com.perftop.android.domain.model.Category
import com.perftop.android.domain.model.Hardware
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RankingUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: Category = Category.PC_CPU,
    val searchQuery: String = "",
    val isRefreshing: Boolean = false
)

@HiltViewModel
class RankingViewModel @Inject constructor(
    private val repository: HardwareRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RankingUiState())
    val uiState: StateFlow<RankingUiState> = _uiState.asStateFlow()

    val hardwares: StateFlow<PagingData<Hardware>> by lazy {
        repository.getHardwaresByCategory(_uiState.value.selectedCategory)
            .cachedIn(viewModelScope)
    }

    init {
        refreshData()
    }

    fun onCategoryChange(category: Category) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onRefresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            refreshData()
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    private fun refreshData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                repository.refreshHardwares(_uiState.value.selectedCategory)
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加载失败"
                )
            }
        }
    }

    fun toggleFavorite(hardwareId: Int) {
        viewModelScope.launch {
            val hardware = repository.getHardwareById(hardwareId) ?: return@launch
            repository.updateFavorite(hardwareId, !hardware.isFavorite)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
