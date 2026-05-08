package com.example.playlistmaker.presentation.player

sealed interface PlayerPlaylistEvent {
    data class PlaylistCreated(val playlistName: String) : PlayerPlaylistEvent
    data class TrackAdded(val playlistName: String) : PlayerPlaylistEvent
    data class TrackAlreadyAdded(val playlistName: String) : PlayerPlaylistEvent
}
