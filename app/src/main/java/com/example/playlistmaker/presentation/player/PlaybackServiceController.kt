package com.example.playlistmaker.presentation.player

import kotlinx.coroutines.flow.StateFlow

interface PlaybackServiceController {

    fun startPlayer()

    fun pausePlayer()

    fun stopPlayer()

    fun getPlayerState(): PlaybackServiceState

    fun observePlayerState(): StateFlow<PlaybackServiceState>

    fun showForegroundNotification()

    fun hideForegroundNotification()
}
