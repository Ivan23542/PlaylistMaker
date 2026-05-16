package com.example.playlistmaker.presentation.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactor.FavoriteTracksInteractor
import com.example.playlistmaker.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerViewModel(
    track: Track,
    private val favoriteTracksInteractor: FavoriteTracksInteractor,
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _uiState = MutableLiveData(PlayerUiState(track = track))
    val uiState: LiveData<PlayerUiState> = _uiState

    private val _playlists = MutableLiveData<List<Playlist>>(emptyList())
    val playlists: LiveData<List<Playlist>> = _playlists

    private val _playlistEvent = MutableLiveData<PlayerPlaylistEvent?>()
    val playlistEvent: LiveData<PlayerPlaylistEvent?> = _playlistEvent

    private var playbackServiceController: PlaybackServiceController? = null
    private var playerStateJob: Job? = null
    private var isPlayerScreenVisible = true
    private var canShowPlaybackNotification = false

    init {
        observeFavoriteState()
        observePlaylists()
    }

    fun onPlayButtonClicked() {
        val controller = playbackServiceController ?: return

        if (controller.getPlayerState().isPlaying) {
            controller.pausePlayer()
        } else {
            controller.startPlayer()
        }
        updateForegroundNotification()
    }

    fun onFavoriteClicked() {
        viewModelScope.launch {
            val currentState = _uiState.value ?: return@launch
            val updatedIsFavorite = !currentState.isFavorite

            if (currentState.isFavorite) {
                favoriteTracksInteractor.removeTrack(currentState.track)
            } else {
                favoriteTracksInteractor.addTrack(currentState.track)
            }

            currentState.track.isFavorite = updatedIsFavorite
            updateState { copy(isFavorite = updatedIsFavorite) }
        }
    }

    fun onPlaylistSelected(playlist: Playlist) {
        val currentTrack = _uiState.value?.track ?: return

        if (currentTrack.trackId in playlist.trackIds) {
            _playlistEvent.value = PlayerPlaylistEvent.TrackAlreadyAdded(playlist.name)
            return
        }

        viewModelScope.launch {
            val added = playlistInteractor.addTrackToPlaylist(currentTrack, playlist)
            _playlistEvent.value = if (added) {
                PlayerPlaylistEvent.TrackAdded(playlist.name)
            } else {
                PlayerPlaylistEvent.TrackAlreadyAdded(playlist.name)
            }
        }
    }

    fun onPlaylistCreated(playlistName: String) {
        _playlistEvent.value = PlayerPlaylistEvent.PlaylistCreated(playlistName)
    }

    fun onPlaylistEventHandled() {
        _playlistEvent.value = null
    }

    fun onPlayerServiceConnected(controller: PlaybackServiceController) {
        playbackServiceController = controller
        playerStateJob?.cancel()
        playerStateJob = viewModelScope.launch {
            controller.observePlayerState().collect { serviceState ->
                updateState {
                    copy(
                        progress = formatTime(serviceState.progressMillis),
                        isPlayButtonEnabled = serviceState.isPlayButtonEnabled,
                        isPlaying = serviceState.isPlaying
                    )
                }
                updateForegroundNotification()
            }
        }
    }

    fun onPlayerServiceDisconnected() {
        playerStateJob?.cancel()
        playerStateJob = null
        playbackServiceController = null
    }

    fun onPlayerScreenVisible() {
        isPlayerScreenVisible = true
        playbackServiceController?.hideForegroundNotification()
    }

    fun onPlayerScreenHidden(canShowNotification: Boolean) {
        isPlayerScreenVisible = false
        canShowPlaybackNotification = canShowNotification
        updateForegroundNotification()
    }

    fun onNotificationPermissionChanged(canShowNotification: Boolean) {
        canShowPlaybackNotification = canShowNotification
        updateForegroundNotification()
    }

    fun onPlayerScreenClosed() {
        playbackServiceController?.stopPlayer()
        playbackServiceController?.hideForegroundNotification()
    }

    override fun onCleared() {
        playerStateJob?.cancel()
        playbackServiceController?.hideForegroundNotification()
        super.onCleared()
    }

    private fun observeFavoriteState() {
        viewModelScope.launch {
            val currentTrack = _uiState.value?.track ?: return@launch
            favoriteTracksInteractor.getFavoriteTracks().collect { favoriteTracks ->
                val isFavorite = favoriteTracks.any { it.trackId == currentTrack.trackId }
                currentTrack.isFavorite = isFavorite
                updateState { copy(isFavorite = isFavorite) }
            }
        }
    }

    private fun observePlaylists() {
        viewModelScope.launch {
            playlistInteractor.getPlaylists().collect { playlists ->
                _playlists.value = playlists
            }
        }
    }

    private fun updateForegroundNotification() {
        val controller = playbackServiceController ?: return
        if (!isPlayerScreenVisible &&
            canShowPlaybackNotification &&
            controller.getPlayerState().isPlaying
        ) {
            controller.showForegroundNotification()
        } else {
            controller.hideForegroundNotification()
        }
    }

    private fun updateState(update: PlayerUiState.() -> PlayerUiState) {
        val currentState = _uiState.value ?: return
        _uiState.value = currentState.update()
    }

    private fun formatTime(position: Int): String {
        return SimpleDateFormat("mm:ss", Locale.getDefault()).format(position.toLong())
    }
}
