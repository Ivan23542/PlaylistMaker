package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface FavoriteTracksRepository {
    suspend fun addTrack(track: Track)
    suspend fun removeTrack(track: Track)
    fun getFavoriteTracks(): Flow<List<Track>>
}
