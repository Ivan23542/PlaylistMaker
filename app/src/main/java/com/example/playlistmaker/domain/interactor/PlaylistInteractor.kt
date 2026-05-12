package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistInteractor {
    suspend fun savePlaylist(playlist: Playlist)
    suspend fun updatePlaylist(playlist: Playlist)
    fun getPlaylists(): Flow<List<Playlist>>
    fun getPlaylist(playlistId: Long): Flow<Playlist?>
    fun getPlaylistTracks(trackIds: List<Long>): Flow<List<Track>>
    suspend fun addTrackToPlaylist(track: Track, playlist: Playlist): Boolean
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long)
    suspend fun deletePlaylist(playlistId: Long)
}
