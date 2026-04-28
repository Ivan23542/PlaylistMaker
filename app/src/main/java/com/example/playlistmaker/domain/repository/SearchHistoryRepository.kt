package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.model.Track

interface SearchHistoryRepository {
    fun read(): List<Track>
    fun save(tracks: List<Track>)
    fun clear()
}