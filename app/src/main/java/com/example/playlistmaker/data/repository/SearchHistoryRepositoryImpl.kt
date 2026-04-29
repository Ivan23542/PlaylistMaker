package com.example.playlistmaker.data.repository

import android.content.SharedPreferences
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.SearchHistoryRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistoryRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : SearchHistoryRepository {

    private val gson = Gson()

    override fun read(): List<Track> {
        val json = sharedPreferences.getString(SEARCH_HISTORY_KEY, null)
            ?: return emptyList()

        val type = object : TypeToken<List<Track>>() {}.type
        return gson.fromJson(json, type)
    }

    override fun save(tracks: List<Track>) {
        val json = gson.toJson(tracks)

        sharedPreferences.edit()
            .putString(SEARCH_HISTORY_KEY, json)
            .apply()
    }

    override fun clear() {
        sharedPreferences.edit()
            .remove(SEARCH_HISTORY_KEY)
            .apply()
    }

    companion object {
        private const val SEARCH_HISTORY_KEY = "search_history"
    }
}