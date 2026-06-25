package com.perftop.android.presentation.ladder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perftop.android.data.repository.HardwareRepository
import com.perftop.android.domain.model.Category
import com.perftop.android.domain.model.Hardware
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LadderUiState(
    val isLoading: Boolean = false,
    val hardwares: List<Hardware> = emptyList(),
    val selectedCategory: Category = Category.PC_CPU,
    val scale: Float = 1f,
    val error: String? = null
)

@HiltViewModel
class LadderViewModel @Inject constructor(
    private val hardwareRepository: HardwareRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LadderUiState())
    val uiState: StateFlow<LadderUiState> = _uiState.asStateFlow()

    init {
        loadLadderData()
    }

    fun onCategoryChange(category: Category) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        loadLadderData()
    }

    fun onScaleChange(newScale: Float) {
        _uiState.value = _uiState.value.copy(scale = newScale.coerceIn(0.5f, 2f))
    }

    fun refresh() {
        loadLadderData()
    }

    private fun loadLadderData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Get all hardwares for the selected category
                val hardwares = mutableListOf<Hardware>()
                // Note: In a real app, you'd fetch all data or use pagination
                // For now, we'll use the repository to get data
                val count = hardwareRepository.getHardwareCount()
                if (count > 0) {
                    // This is a simplified approach - in production you'd want proper pagination
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        hardwares = emptyList() // Would load from repository
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加载失败"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
