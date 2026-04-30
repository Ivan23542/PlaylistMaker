package com.example.playlistmaker.data.repository

import android.content.SharedPreferences
import com.example.playlistmaker.domain.model.ThemeSettings
import com.example.playlistmaker.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : SettingsRepository {

    override fun getThemeSettings(): ThemeSettings {
        val isDarkTheme = sharedPreferences.getBoolean(DARK_THEME_KEY, false)
        return ThemeSettings(isDarkTheme)
    }

    override fun updateThemeSetting(settings: ThemeSettings) {
        sharedPreferences.edit()
            .putBoolean(DARK_THEME_KEY, settings.isDarkTheme)
            .apply()
    }

    private companion object {
        private const val DARK_THEME_KEY = "dark_theme"
    }
}
