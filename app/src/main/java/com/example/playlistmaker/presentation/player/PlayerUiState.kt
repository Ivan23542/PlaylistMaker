package com.example.playlistmaker.presentation.player

import com.example.playlistmaker.domain.model.Track

data class PlayerUiState(
    val track: Track,
    val progress: String = START_PROGRESS,
    val isPlayButtonEnabled: Boolean = false,
    val isPlaying: Boolean = false,
    val isFavorite: Boolean = false,
    val createdPlaylistName: String? = null,
    val artworkUrl: String = track.artworkUrl100.replaceAfterLast("/", "512x512bb.jpg"),
    val album: String = track.collectionName.orEmpty(),
    val isAlbumVisible: Boolean = !track.collectionName.isNullOrBlank(),
    val year: String = track.releaseDate?.take(4).orEmpty(),
    val isYearVisible: Boolean = !track.releaseDate?.take(4).isNullOrBlank()
) {
    companion object {
        const val START_PROGRESS = "00:00"
    }
}
