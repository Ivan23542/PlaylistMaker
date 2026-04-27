package com.example.playlistmaker.data.storage

import android.content.SharedPreferences
import com.example.playlistmaker.domain.model.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistory(private val sharedPreferences: SharedPreferences) {

    companion object {
        private const val SEARCH_HISTORY_KEY = "search_history"
        private const val MAX_HISTORY_SIZE = 10
    }

    private val gson = Gson()

    fun read(): ArrayList<Track> {
        val json = sharedPreferences.getString(SEARCH_HISTORY_KEY, null) ?: return arrayListOf()
        val type = object : TypeToken<ArrayList<Track>>() {}.type
        return gson.fromJson(json, type)
    }

    fun add(track: Track) {
        val tracks = read()

        tracks.removeAll { it.trackId == track.trackId }
        tracks.add(0, track)

        if (tracks.size > MAX_HISTORY_SIZE) {
            tracks.removeAt(tracks.lastIndex)
        }

        save(tracks)
    }

    fun clear() {
        sharedPreferences.edit().remove(SEARCH_HISTORY_KEY).apply()
    }

    private fun save(tracks: ArrayList<Track>) {
        val json = gson.toJson(tracks)
        sharedPreferences.edit().putString(SEARCH_HISTORY_KEY, json).apply()
    }
}