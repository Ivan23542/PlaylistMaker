package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.SearchHistoryRepository

class SearchHistoryInteractorImpl(
    private val repository: SearchHistoryRepository
) : SearchHistoryInteractor {

    override fun read(): List<Track> = repository.read()

    override fun add(track: Track) {
        repository.add(track)
    }

    override fun clear() {
        repository.clear()
    }
}