package com.example.playlistmaker.presentation.media.playlists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistDetailsViewModel(
    private val playlistId: Long,
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _state = MutableLiveData(PlaylistDetailsState())
    val state: LiveData<PlaylistDetailsState> = _state

    private val _event = MutableLiveData<PlaylistDetailsEvent?>()
    val event: LiveData<PlaylistDetailsEvent?> = _event

    private var currentPlaylist: Playlist? = null
    private var currentTracks: List<Track> = emptyList()

    init {
        observePlaylist()
    }

    fun onShareClicked() {
        val playlist = currentPlaylist ?: return
        if (currentTracks.isEmpty()) {
            _event.value = PlaylistDetailsEvent.EmptyShare
            return
        }

        _event.value = PlaylistDetailsEvent.SharePlaylist(
            buildShareText(playlist, currentTracks)
        )
    }

    fun onTrackRemoveConfirmed(track: Track) {
        viewModelScope.launch {
            playlistInteractor.removeTrackFromPlaylist(playlistId, track.trackId)
        }
    }

    fun onPlaylistDeleteConfirmed() {
        viewModelScope.launch {
            playlistInteractor.deletePlaylist(playlistId)
            _event.value = PlaylistDetailsEvent.PlaylistDeleted
        }
    }

    fun onEventHandled() {
        _event.value = null
    }

    private fun observePlaylist() {
        viewModelScope.launch {
            playlistInteractor.getPlaylist(playlistId)
                .flatMapLatest { playlist ->
                    currentPlaylist = playlist
                    if (playlist == null) {
                        flowOf(playlist to emptyList())
                    } else {
                        playlistInteractor.getPlaylistTracks(playlist.trackIds)
                            .map { tracks -> playlist to tracks }
                    }
                }
                .collect { (playlist, tracks) ->
                    currentPlaylist = playlist
                    currentTracks = tracks
                    _state.value = PlaylistDetailsState(
                        playlist = playlist,
                        tracks = tracks,
                        totalMinutes = calculateTotalMinutes(tracks)
                    )
                }
        }
    }

    private fun calculateTotalMinutes(tracks: List<Track>): Int {
        return tracks.sumOf { track ->
            val parts = track.trackTime.split(":")
            val minutes = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val seconds = parts.getOrNull(1)?.toIntOrNull() ?: 0
            minutes * SECONDS_IN_MINUTE + seconds
        } / SECONDS_IN_MINUTE
    }

    private fun buildShareText(playlist: Playlist, tracks: List<Track>): String {
        return buildString {
            appendLine(playlist.name)
            if (playlist.description.isNotBlank()) {
                appendLine(playlist.description)
            }
            appendLine(formatTrackCountForShare(tracks.size))
            tracks.forEachIndexed { index, track ->
                append("${index + 1}. ${track.artistName} - ${track.trackName} (${track.trackTime})")
                if (index != tracks.lastIndex) appendLine()
            }
        }
    }

    private fun formatTrackCountForShare(count: Int): String {
        val mod100 = count % 100
        val mod10 = count % 10
        val suffix = when {
            mod100 in 11..14 -> "\u0442\u0440\u0435\u043a\u043e\u0432"
            mod10 == 1 -> "\u0442\u0440\u0435\u043a"
            mod10 in 2..4 -> "\u0442\u0440\u0435\u043a\u0430"
            else -> "\u0442\u0440\u0435\u043a\u043e\u0432"
        }
        return "$count $suffix"
    }

    private companion object {
        private const val SECONDS_IN_MINUTE = 60
    }
}
