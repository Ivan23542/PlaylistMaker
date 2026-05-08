package com.example.playlistmaker.presentation.media.playlists

import com.example.playlistmaker.domain.model.Playlist

sealed interface PlaylistsState {
    data object Empty : PlaylistsState
    data class Content(val playlists: List<Playlist>) : PlaylistsState
}
