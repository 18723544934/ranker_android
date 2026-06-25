package com.perftop.android.presentation.compare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perftop.android.data.repository.HardwareRepository
import com.perftop.android.domain.model.Hardware
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CompareUiState(
    val isLoading: Boolean = false,
    val selectedHardwareIds: Set<Int> = emptySet(),
    val hardwares: List<Hardware> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class CompareViewModel @Inject constructor(
    private val hardwareRepository: HardwareRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompareUiState())
    val uiState: StateFlow<CompareUiState> = _uiState.asStateFlow()

    init {
        loadCompareData()
    }

    fun toggleHardwareSelection(hardwareId: Int) {
        val currentSelection = _uiState.value.selectedHardwareIds
        val newSelection = if (hardwareId in currentSelection) {
            currentSelection - hardwareId
        } else {
            if (currentSelection.size >= 5) {
                return // Max 5 items
            }
            currentSelection + hardwareId
        }
        _uiState.value = _uiState.value.copy(selectedHardwareIds = newSelection)
    }

    fun removeFromComparison(hardwareId: Int) {
        val currentSelection = _uiState.value.selectedHardwareIds
        _uiState.value = _uiState.value.copy(
            selectedHardwareIds = currentSelection - hardwareId
        )
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedHardwareIds = emptySet())
    }

    private fun loadCompareData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val selectedIds = _uiState.value.selectedHardwareIds
                if (selectedIds.isNotEmpty()) {
                    val hardwares = selectedIds.mapNotNull { id ->
                        hardwareRepository.getHardwareById(id)
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        hardwares = hardwares
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

    fun refresh() {
        loadCompareData()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
