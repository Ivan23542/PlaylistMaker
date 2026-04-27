package com.example.playlistmaker.data.player

import android.media.MediaPlayer
import com.example.playlistmaker.domain.interactor.PlayerInteractor

class PlayerRepositoryImpl : PlayerInteractor {

    private var mediaPlayer: MediaPlayer? = null

    override fun preparePlayer(
        url: String,
        onPrepared: () -> Unit,
        onCompletion: () -> Unit,
        onError: () -> Unit
    ) {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            prepareAsync()

            setOnPreparedListener {
                onPrepared()
            }

            setOnCompletionListener {
                seekTo(0)
                onCompletion()
            }

            setOnErrorListener { _, _, _ ->
                onError()
                true
            }
        }
    }

    override fun startPlayer() {
        mediaPlayer?.start()
    }

    override fun pausePlayer() {
        mediaPlayer?.pause()
    }

    override fun stopPlayer() {
        mediaPlayer?.pause()
        mediaPlayer?.seekTo(0)
    }

    override fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }
}