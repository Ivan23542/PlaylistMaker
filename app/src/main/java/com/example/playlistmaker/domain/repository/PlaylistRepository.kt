package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.model.Playlist

interface PlaylistRepository {
    fun savePlaylist(playlist: Playlist)
}