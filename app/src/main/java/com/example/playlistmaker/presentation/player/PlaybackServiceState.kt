package com.example.playlistmaker.presentation.player

data class PlaybackServiceState(
    val playerState: PlaybackPlayerState = PlaybackPlayerState.DEFAULT,
    val progressMillis: Int = 0,
    val isPlayButtonEnabled: Boolean = false
) {
    val isPlaying: Boolean
        get() = playerState == PlaybackPlayerState.PLAYING
}

enum class PlaybackPlayerState {
    DEFAULT,
    PREPARED,
    PLAYING,
    PAUSED
}
