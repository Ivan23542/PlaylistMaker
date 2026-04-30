package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.storage.PlaylistStorage
import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.repository.PlaylistRepository

class PlaylistRepositoryImpl(
    private val playlistStorage: PlaylistStorage
) : PlaylistRepository {

    override fun savePlaylist(playlist: Playlist) {
        playlistStorage.savePlaylist(playlist)
    }
}
