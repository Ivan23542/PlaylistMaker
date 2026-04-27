package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.storage.SearchHistory
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.SearchHistoryRepository

class SearchHistoryRepositoryImpl(
    private val searchHistory: SearchHistory
) : SearchHistoryRepository {

    override fun read(): List<Track> = searchHistory.read()

    override fun add(track: Track) {
        searchHistory.add(track)
    }

    override fun clear() {
        searchHistory.clear()
    }
}