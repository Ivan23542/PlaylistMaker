package com.example.playlistmaker.presentation.playlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.domain.model.Playlist
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NewPlaylistViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _uiState = MutableLiveData(NewPlaylistUiState())
    val uiState: LiveData<NewPlaylistUiState> = _uiState

    private var editingPlaylist: Playlist? = null
    private var loadedPlaylistId: Long? = null

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

    fun onCoverSelected(coverUri: String) {
        updateState { copy(coverUri = coverUri) }
    }

    fun restoreState(name: String, description: String, coverUri: String?) {
        if (_uiState.value?.isEditMode == true) return
        updateState {
            copy(
                name = name,
                description = description,
                coverUri = coverUri,
                isCreateButtonEnabled = name.isNotBlank()
            )
        }
    }

    fun loadPlaylistForEdit(playlistId: Long) {
        if (loadedPlaylistId == playlistId) return
        loadedPlaylistId = playlistId

        viewModelScope.launch {
            val playlist = playlistInteractor.getPlaylist(playlistId).first() ?: return@launch
            editingPlaylist = playlist
            updateState {
                copy(
                    name = playlist.name,
                    description = playlist.description,
                    coverUri = playlist.coverPath,
                    isCreateButtonEnabled = playlist.name.isNotBlank(),
                    isEditMode = true
                )
            }
        }
    }

    fun onCreateButtonClicked() {
        val currentState = _uiState.value ?: return
        val playlistName = currentState.name.trim()
        if (playlistName.isBlank()) return

        viewModelScope.launch {
            val editedPlaylist = editingPlaylist

            if (currentState.isEditMode && editedPlaylist != null) {
                playlistInteractor.updatePlaylist(
                    editedPlaylist.copy(
                        name = playlistName,
                        description = currentState.description.trim(),
                        coverPath = currentState.coverUri
                    )
                )
                updateState {
                    copy(
                        name = playlistName,
                        description = currentState.description.trim(),
                        isCreateButtonEnabled = true,
                        savedPlaylistName = playlistName
                    )
                }
                return@launch
            }

            playlistInteractor.savePlaylist(
                Playlist(
                    name = playlistName,
                    description = currentState.description.trim(),
                    coverPath = currentState.coverUri
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
    }

    fun onPlaylistCreatedHandled() {
        updateState { copy(createdPlaylistName = null, savedPlaylistName = null) }
    }

    private fun updateState(update: NewPlaylistUiState.() -> NewPlaylistUiState) {
        val currentState = _uiState.value ?: NewPlaylistUiState()
        _uiState.value = currentState.update()
    }
}
