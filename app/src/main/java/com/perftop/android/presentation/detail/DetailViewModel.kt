package com.perftop.android.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perftop.android.data.repository.FavoriteRepository
import com.perftop.android.data.repository.HardwareRepository
import com.perftop.android.data.repository.HistoryRepository
import com.perftop.android.domain.model.Hardware
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val isLoading: Boolean = false,
    val hardware: Hardware? = null,
    val isFavorite: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val hardwareRepository: HardwareRepository,
    private val favoriteRepository: FavoriteRepository,
    private val historyRepository: HistoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val hardwareId: Int = savedStateHandle.get<String>("hardwareId")?.toIntOrNull() ?: 0

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadHardwareDetail()
        addToHistory()
    }

    private fun loadHardwareDetail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val hardware = hardwareRepository.getHardwareById(hardwareId)
                val isFavorite = favoriteRepository.isFavorite(hardwareId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    hardware = hardware,
                    isFavorite = isFavorite
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加载失败"
                )
            }
        }
    }

    private fun addToHistory() {
        viewModelScope.launch {
            historyRepository.addToHistory(hardwareId)
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val currentFavorite = _uiState.value.isFavorite
            if (currentFavorite) {
                favoriteRepository.removeFavorite(hardwareId)
            } else {
                favoriteRepository.addFavorite(hardwareId)
            }
            _uiState.value = _uiState.value.copy(isFavorite = !currentFavorite)
        }
    }

    fun refresh() {
        loadHardwareDetail()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
