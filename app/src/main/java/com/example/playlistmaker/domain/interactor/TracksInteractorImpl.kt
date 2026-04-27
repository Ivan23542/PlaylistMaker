package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.TracksRepository

class TracksInteractorImpl(
    private val repository: TracksRepository
) : TracksInteractor {

    override fun searchTracks(expression: String, consumer: TracksInteractor.TracksConsumer) {
        repository.searchTracks(expression, object : TracksRepository.TracksConsumer {
            override fun consume(foundTracks: List<Track>?, errorMessage: String?) {
                consumer.consume(foundTracks, errorMessage)
            }
        })
    }
}