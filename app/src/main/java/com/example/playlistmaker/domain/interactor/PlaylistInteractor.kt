package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.Playlist

interface PlaylistInteractor {
    fun savePlaylist(playlist: Playlist)
}
