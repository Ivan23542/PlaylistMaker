package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.repository.PlayerRepository

class PlayerInteractorImpl(
    private val repository: PlayerRepository
) : PlayerInteractor {

    override fun preparePlayer(
        url: String,
        onPrepared: () -> Unit,
        onCompletion: () -> Unit,
        onError: () -> Unit
    ) {
        repository.preparePlayer(url, onPrepared, onCompletion, onError)
    }

    override fun startPlayer() {
        repository.startPlayer()
    }

    override fun pausePlayer() {
        repository.pausePlayer()
    }

    override fun stopPlayer() {
        repository.stopPlayer()
    }

    override fun release() {
        repository.release()
    }

    override fun getCurrentPosition(): Int {
        return repository.getCurrentPosition()
    }
}
