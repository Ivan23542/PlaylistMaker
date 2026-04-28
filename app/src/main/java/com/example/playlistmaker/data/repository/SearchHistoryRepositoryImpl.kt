package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.storage.SearchHistory
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.SearchHistoryRepository

class SearchHistoryRepositoryImpl(
    private val searchHistory: SearchHistory
) : SearchHistoryRepository {

    override fun read(): List<Track> {
        return searchHistory.read()
    }

    override fun save(tracks: List<Track>) {
        searchHistory.save(tracks)
    }

    override fun clear() {
        searchHistory.clear()
    }
}