package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow

class PlaylistInteractorImpl(
    private val repository: PlaylistRepository
) : PlaylistInteractor {

    override suspend fun savePlaylist(playlist: Playlist) = repository.savePlaylist(playlist)

    override suspend fun updatePlaylist(playlist: Playlist) = repository.updatePlaylist(playlist)

    override fun getPlaylists(): Flow<List<Playlist>> = repository.getPlaylists()

    override fun getPlaylist(playlistId: Long): Flow<Playlist?> = repository.getPlaylist(playlistId)

    override fun getPlaylistTracks(trackIds: List<Long>): Flow<List<Track>> {
        return repository.getPlaylistTracks(trackIds)
    }

    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist): Boolean {
        return repository.addTrackToPlaylist(track, playlist)
    }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        repository.removeTrackFromPlaylist(playlistId, trackId)
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        repository.deletePlaylist(playlistId)
    }
}
