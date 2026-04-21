package com.example.playlistmaker

import java.text.SimpleDateFormat
import java.util.Locale

object TrackMapper {
    fun map(dto: TrackDto): Track {
        return Track(
            trackName = dto.trackName ?: "",
            artistName = dto.artistName ?: "",
            trackTime = SimpleDateFormat("mm:ss", Locale.getDefault())
                .format(dto.trackTimeMillis ?: 0L),
            artworkUrl100 = dto.artworkUrl100 ?: ""
        )
    }
}