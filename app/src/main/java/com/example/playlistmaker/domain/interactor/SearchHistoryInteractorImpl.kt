package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.SearchHistoryRepository

class SearchHistoryInteractorImpl(
    private val repository: SearchHistoryRepository
) : SearchHistoryInteractor {

    override fun read(): List<Track> {
        return repository.read()
    }

    override fun add(track: Track) {
        val tracks = repository.read().toMutableList()

        tracks.removeAll { it.trackId == track.trackId }
        tracks.add(0, track)

        if (tracks.size > MAX_HISTORY_SIZE) {
            tracks.removeAt(tracks.lastIndex)
        }

        repository.save(tracks)
    }

    override fun clear() {
        repository.clear()
    }

    companion object {
        private const val MAX_HISTORY_SIZE = 10
    }
}