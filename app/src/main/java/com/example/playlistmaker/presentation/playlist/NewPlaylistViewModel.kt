package com.example.playlistmaker.presentation.playlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.domain.model.Playlist

class NewPlaylistViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _uiState = MutableLiveData(NewPlaylistUiState())
    val uiState: LiveData<NewPlaylistUiState> = _uiState

    fun onNameChanged(name: String) {
        updateState {
            copy(
                name = name,
                isCreateButtonEnabled = name.isNotBlank()
            )
        }
    }

    fun onDescriptionChanged(description: String) {
        updateState { copy(description = description) }
    }

    fun onCreateButtonClicked() {
        val currentState = _uiState.value ?: return
        val playlistName = currentState.name.trim()
        if (playlistName.isBlank()) return

        playlistInteractor.savePlaylist(
            Playlist(
                name = playlistName,
                description = currentState.description.trim()
            )
        )

        updateState {
            copy(
                name = playlistName,
                description = currentState.description.trim(),
                isCreateButtonEnabled = true,
                createdPlaylistName = playlistName
            )
        }
    }

    fun onPlaylistCreatedHandled() {
        updateState { copy(createdPlaylistName = null) }
    }

    private fun updateState(update: NewPlaylistUiState.() -> NewPlaylistUiState) {
        val currentState = _uiState.value ?: NewPlaylistUiState()
        _uiState.value = currentState.update()
    }
}
