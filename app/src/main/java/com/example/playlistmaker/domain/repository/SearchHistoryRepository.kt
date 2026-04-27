package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.model.Track

interface SearchHistoryRepository {
    fun read(): List<Track>
    fun add(track: Track)
    fun clear()
}