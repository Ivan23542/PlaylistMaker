package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.ThemeSettings

interface SettingsInteractor {
    fun getThemeSettings(): ThemeSettings
    fun updateThemeSetting(settings: ThemeSettings)
}