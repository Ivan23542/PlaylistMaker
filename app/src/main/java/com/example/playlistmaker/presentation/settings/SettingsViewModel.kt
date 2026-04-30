package com.example.playlistmaker.presentation.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.interactor.SettingsInteractor
import com.example.playlistmaker.domain.model.ThemeSettings

class SettingsViewModel(
    private val settingsInteractor: SettingsInteractor
) : ViewModel() {

    private val _uiState = MutableLiveData(
        SettingsUiState(settingsInteractor.getThemeSettings().isDarkTheme)
    )
    val uiState: LiveData<SettingsUiState> = _uiState

    fun onThemeCheckedChanged(isChecked: Boolean) {
        settingsInteractor.updateThemeSetting(ThemeSettings(isChecked))
        _uiState.value = SettingsUiState(isChecked)
    }
}
