package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow

class PlaylistInteractorImpl(
    private val repository: PlaylistRepository
) : PlaylistInteractor {

    override suspend fun savePlaylist(playlist: Playlist) = repository.savePlaylist(playlist)

    override fun getPlaylists(): Flow<List<Playlist>> = repository.getPlaylists()

    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist): Boolean {
        return repository.addTrackToPlaylist(track, playlist)
    }
}
