package com.example.playlistmaker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {

    companion object {
        const val PREFS_NAME = "playlist_maker_prefs"
        const val DARK_THEME_KEY = "dark_theme"
    }

    override fun onCreate() {
        super.onCreate()

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val darkThemeEnabled = prefs.getBoolean(DARK_THEME_KEY, false)

        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}
