package com.example.playlistmaker.presentation.playlist

data class NewPlaylistUiState(
    val name: String = "",
    val description: String = "",
    val coverUri: String? = null,
    val isCreateButtonEnabled: Boolean = false,
    val createdPlaylistName: String? = null
) {
    fun hasUnsavedChanges(): Boolean {
        return name.isNotBlank() || description.isNotBlank() || !coverUri.isNullOrBlank()
    }
}
