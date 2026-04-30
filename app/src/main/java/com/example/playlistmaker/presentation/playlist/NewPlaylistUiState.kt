package com.example.playlistmaker.presentation.playlist

data class NewPlaylistUiState(
    val name: String = "",
    val description: String = "",
    val isCreateButtonEnabled: Boolean = false,
    val createdPlaylistName: String? = null
)
