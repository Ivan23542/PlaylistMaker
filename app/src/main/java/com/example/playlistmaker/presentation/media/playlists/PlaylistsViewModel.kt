package com.example.playlistmaker.presentation.media.playlists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactor.PlaylistInteractor
import kotlinx.coroutines.launch

class PlaylistsViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _state = MutableLiveData<PlaylistsState>(PlaylistsState.Empty)
    val state: LiveData<PlaylistsState> = _state

    init {
        viewModelScope.launch {
            playlistInteractor.getPlaylists().collect { playlists ->
                _state.value = if (playlists.isEmpty()) {
                    PlaylistsState.Empty
                } else {
                    PlaylistsState.Content(playlists)
                }
            }
        }
    }
}
