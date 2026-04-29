package com.example.playlistmaker.domain.interactor

interface PlayerInteractor {

    fun preparePlayer(
        url: String,
        onPrepared: () -> Unit,
        onCompletion: () -> Unit,
        onError: () -> Unit
    )

    fun startPlayer()
    fun pausePlayer()
    fun stopPlayer()
    fun release()
    fun getCurrentPosition(): Int
}