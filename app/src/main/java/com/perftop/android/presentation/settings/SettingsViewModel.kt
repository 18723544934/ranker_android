package com.perftop.android.presentation.settings

import android.content.Context
import android.content.pm.PackageManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perftop.android.data.repository.FavoriteRepository
import com.perftop.android.data.repository.HardwareRepository
import com.perftop.android.data.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val darkTheme: Boolean = true,
    val updateOnWifiOnly: Boolean = true,
    val geekbenchWeight: Float = 1.0f,
    val passmarkWeight: Float = 1.0f,
    val dmarkWeight: Float = 1.0f,
    val antutuWeight: Float = 1.0f,
    val cacheSize: Long = 0L,
    val version: String = "",
    val isLoading: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val favoriteRepository: FavoriteRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        loadVersion()
        loadCacheSize()
    }

    // Preferences Keys
    private object PreferencesKeys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val UPDATE_WIFI_ONLY = booleanPreferencesKey("update_wifi_only")
        val GEEKBENCH_WEIGHT = floatPreferencesKey("geekbench_weight")
        val PASSMARK_WEIGHT = floatPreferencesKey("passmark_weight")
        val DMARK_WEIGHT = floatPreferencesKey("dmark_weight")
        val ANTUTU_WEIGHT = floatPreferencesKey("antutu_weight")
    }

    private fun loadSettings() {
        viewModelScope.launch {
            context.dataStore.data.first().collect { preferences ->
                val darkTheme = preferences[PreferencesKeys.DARK_THEME] ?: true
                val wifiOnly = preferences[PreferencesKeys.UPDATE_WIFI_ONLY] ?: true
                val geekbenchWeight = preferences[PreferencesKeys.GEEKBENCH_WEIGHT] ?: 1.0f
                val passmarkWeight = preferences[PreferencesKeys.PASSMARK_WEIGHT] ?: 1.0f
                val dmarkWeight = preferences[PreferencesKeys.DMARK_WEIGHT] ?: 1.0f
                val antutuWeight = preferences[PreferencesKeys.ANTUTU_WEIGHT] ?: 1.0f

                _uiState.value = _uiState.value.copy(
                    darkTheme = darkTheme,
                    updateOnWifiOnly = wifiOnly,
                    geekbenchWeight = geekbenchWeight,
                    passmarkWeight = passmarkWeight,
                    dmarkWeight = dmarkWeight,
                    antutuWeight = antutuWeight
                )
            }
        }
    }

    private suspend fun loadVersion() {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            _uiState.value = _uiState.value.copy(
                version = packageInfo.versionName ?: "Unknown"
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(version = "Unknown")
        }
    }

    private suspend fun loadCacheSize() {
        try {
            val count = historyRepository.getCount()
            _uiState.value = _uiState.value.copy(
                cacheSize = count.toLong()
            )
        } catch (e: Exception) {
            // Ignore error
        }
    }

    fun updateDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.DARK_THEME] = enabled
            }
        }
    }

    fun updateWifiOnly(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.UPDATE_WIFI_ONLY] = enabled
            }
        }
    }

    fun updateWeight(source: String, weight: Float) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                when (source) {
                    "Geekbench" -> preferences[PreferencesKeys.GEEKBENCH_WEIGHT] = weight
                    "PassMark" -> preferences[PreferencesKeys.PASSMARK_WEIGHT] = weight
                    "3DMark" -> preferences[PreferencesKeys.DMARK_WEIGHT] = weight
                    "Antutu" -> preferences[PreferencesKeys.ANTUTU_WEIGHT] = weight
                }
            }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                favoriteRepository.clearAll()
                historyRepository.clearAll()
                loadCacheSize()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun resetWeights() {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.GEEKBENCH_WEIGHT] = 1.0f
                preferences[PreferencesKeys.PASSMARK_WEIGHT] = 1.0f
                preferences[PreferencesKeys.DMARK_WEIGHT] = 1.0f
                preferences[PreferencesKeys.ANTUTU_WEIGHT] = 1.0f
            }
        }
    }
}
