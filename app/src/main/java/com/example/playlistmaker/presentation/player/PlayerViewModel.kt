package com.example.playlistmaker.presentation.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactor.PlayerInteractor
import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerViewModel(
    track: Track,
    private val playerInteractor: PlayerInteractor
) : ViewModel() {

    private enum class PlayerState {
        DEFAULT,
        PREPARED,
        PLAYING,
        PAUSED
    }

    private val _uiState = MutableLiveData(PlayerUiState(track = track))
    val uiState: LiveData<PlayerUiState> = _uiState

    private var playerState = PlayerState.DEFAULT
    private var progressJob: Job? = null

    init {
        preparePlayer()
    }

    fun onPlayButtonClicked() {
        when (playerState) {
            PlayerState.PLAYING -> pausePlayer()
            PlayerState.PREPARED, PlayerState.PAUSED -> startPlayer()
            PlayerState.DEFAULT -> Unit
        }
    }

    fun onFavoriteClicked() {
        updateState { copy(isFavorite = !isFavorite) }
    }

    fun onPause() {
        if (playerState == PlayerState.PLAYING) {
            pausePlayer()
        }
    }

    fun onPlaylistCreated(playlistName: String) {
        updateState { copy(createdPlaylistName = playlistName) }
    }

    fun onPlaylistCreatedHandled() {
        updateState { copy(createdPlaylistName = null) }
    }

    override fun onCleared() {
        super.onCleared()
        stopProgressUpdates()
        playerInteractor.release()
    }

    private fun preparePlayer() {
        val track = _uiState.value?.track ?: return
        val previewUrl = track.previewUrl

        if (previewUrl.isNullOrBlank()) {
            updateState {
                copy(
                    isPlayButtonEnabled = false,
                    isPlaying = false,
                    progress = PlayerUiState.START_PROGRESS
                )
            }
            return
        }

        updateState {
            copy(
                isPlayButtonEnabled = false,
                isPlaying = false,
                progress = PlayerUiState.START_PROGRESS
            )
        }

        playerInteractor.preparePlayer(
            url = previewUrl,
            onPrepared = {
                playerState = PlayerState.PREPARED
                updateState { copy(isPlayButtonEnabled = true) }
            },
            onCompletion = {
                stopProgressUpdates()
                playerState = PlayerState.PREPARED
                updateState {
                    copy(
                        isPlaying = false,
                        progress = PlayerUiState.START_PROGRESS
                    )
                }
            },
            onError = {
                stopProgressUpdates()
                playerState = PlayerState.DEFAULT
                updateState {
                    copy(
                        isPlayButtonEnabled = false,
                        isPlaying = false,
                        progress = PlayerUiState.START_PROGRESS
                    )
                }
            }
        )
    }

    private fun startPlayer() {
        playerInteractor.startPlayer()
        playerState = PlayerState.PLAYING
        updateState { copy(isPlaying = true) }
        startProgressUpdates()
    }

    private fun pausePlayer() {
        playerInteractor.pausePlayer()
        playerState = PlayerState.PAUSED
        stopProgressUpdates()
        updateState {
            copy(
                isPlaying = false,
                progress = formatTime(playerInteractor.getCurrentPosition())
            )
        }
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressJob = viewModelScope.launch {
            while (isActive && playerState == PlayerState.PLAYING) {
                updateState {
                    copy(progress = formatTime(playerInteractor.getCurrentPosition()))
                }
                delay(PROGRESS_DELAY)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun updateState(update: PlayerUiState.() -> PlayerUiState) {
        val currentState = _uiState.value ?: return
        _uiState.value = currentState.update()
    }

    private fun formatTime(position: Int): String {
        return SimpleDateFormat("mm:ss", Locale.getDefault()).format(position.toLong())
    }

    private companion object {
        private const val PROGRESS_DELAY = 300L
    }
}
