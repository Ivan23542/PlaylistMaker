package com.example.playlistmaker.presentation.media.playlists

sealed interface PlaylistDetailsEvent {
    object EmptyShare : PlaylistDetailsEvent
    data class SharePlaylist(val text: String) : PlaylistDetailsEvent
    object PlaylistDeleted : PlaylistDetailsEvent
}
