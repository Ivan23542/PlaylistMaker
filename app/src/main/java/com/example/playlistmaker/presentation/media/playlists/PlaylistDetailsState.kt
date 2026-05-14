package com.example.playlistmaker.presentation.media.playlists

import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.model.Track

data class PlaylistDetailsState(
    val playlist: Playlist? = null,
    val tracks: List<Track> = emptyList(),
    val totalMinutes: Int = 0,
    val isPlaylistDeleted: Boolean = false
)
