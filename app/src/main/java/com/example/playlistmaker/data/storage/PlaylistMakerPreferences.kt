package com.example.playlistmaker.data.storage

import android.content.Context
import android.content.SharedPreferences

object PlaylistMakerPreferences {
    private const val PREFS_NAME = "playlist_maker_prefs"

    fun getSharedPreferences(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
