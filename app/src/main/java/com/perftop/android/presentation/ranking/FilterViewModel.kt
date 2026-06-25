package com.perftop.android.presentation.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perftop.android.data.repository.HardwareRepository
import com.perftop.android.domain.model.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FilterUiState(
    val selectedBrands: Set<String> = emptySet(),
    val selectedArchitectures: Set<String> = emptySet(),
    val coreMin: Int? = null,
    val coreMax: Int? = null,
    val yearMin: Int? = null,
    val yearMax: Int? = null,
    val availableBrands: List<String> = emptyList(),
    val availableArchitectures: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FilterViewModel @Inject constructor(
    private val hardwareRepository: HardwareRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FilterUiState())
    val uiState: StateFlow<FilterUiState> = _uiState.asStateFlow()

    fun loadFilterOptions(category: Category) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val brands = hardwareRepository.getBrandsByCategory(category)
                val architectures = hardwareRepository.getArchitecturesByCategory(category)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    availableBrands = brands,
                    availableArchitectures = architectures
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加载筛选选项失败"
                )
            }
        }
    }

    fun toggleBrand(brand: String) {
        val current = _uiState.value.selectedBrands
        _uiState.value = _uiState.value.copy(
            selectedBrands = if (brand in current) current - brand else current + brand
        )
    }

    fun toggleArchitecture(architecture: String) {
        val current = _uiState.value.selectedArchitectures
        _uiState.value = _uiState.value.copy(
            selectedArchitectures = if (architecture in current) current - architecture else current + architecture
        )
    }

    fun setCoreRange(min: Int?, max: Int?) {
        _uiState.value = _uiState.value.copy(
            coreMin = min,
            coreMax = max
        )
    }

    fun setYearRange(min: Int?, max: Int?) {
        _uiState.value = _uiState.value.copy(
            yearMin = min,
            yearMax = max
        )
    }

    fun resetFilters() {
        _uiState.value = _uiState.value.copy(
            selectedBrands = emptySet(),
            selectedArchitectures = emptySet(),
            coreMin = null,
            coreMax = null,
            yearMin = null,
            yearMax = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun hasActiveFilters(): Boolean {
        val state = _uiState.value
        return state.selectedBrands.isNotEmpty() ||
                state.selectedArchitectures.isNotEmpty() ||
                state.coreMin != null ||
                state.coreMax != null ||
                state.yearMin != null ||
                state.yearMax != null
    }
}
