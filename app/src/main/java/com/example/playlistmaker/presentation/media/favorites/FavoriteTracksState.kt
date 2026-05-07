package com.example.playlistmaker.presentation.media.favorites

import com.example.playlistmaker.domain.model.Track

sealed interface FavoriteTracksState {
    data object Empty : FavoriteTracksState
    data class Content(val tracks: List<Track>) : FavoriteTracksState
}
