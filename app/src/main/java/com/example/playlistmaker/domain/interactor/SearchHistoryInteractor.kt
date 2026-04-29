package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.Track

interface SearchHistoryInteractor {
    fun read(): List<Track>
    fun add(track: Track)
    fun clear()
}