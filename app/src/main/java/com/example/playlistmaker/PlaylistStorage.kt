package com.example.playlistmaker

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PlaylistStorage(
    private val sharedPreferences: SharedPreferences
) {
    private val gson = Gson()

    fun savePlaylist(playlist: Playlist) {
        val playlists = getPlaylists().toMutableList()
        playlists.add(playlist)

        sharedPreferences.edit()
            .putString(PLAYLISTS_KEY, gson.toJson(playlists))
            .apply()
    }

    fun getPlaylists(): List<Playlist> {
        val json = sharedPreferences.getString(PLAYLISTS_KEY, null) ?: return emptyList()
        val type = object : TypeToken<List<Playlist>>() {}.type
        return gson.fromJson(json, type)
    }

    companion object {
        private const val PLAYLISTS_KEY = "playlists_key"
    }
}