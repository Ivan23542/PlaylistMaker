package com.example.playlistmaker.data.mapper

import com.example.playlistmaker.data.network.TrackDto
import com.example.playlistmaker.domain.model.Track
import java.text.SimpleDateFormat
import java.util.Locale

object TrackMapper {
    fun map(dto: TrackDto): Track {
        return Track(
            trackId = dto.trackId ?: 0L,
            trackName = dto.trackName ?: "",
            artistName = dto.artistName ?: "",
            trackTime = SimpleDateFormat("mm:ss", Locale.getDefault())
                .format(dto.trackTimeMillis ?: 0L),
            artworkUrl100 = dto.artworkUrl100 ?: "",
            collectionName = dto.collectionName,
            releaseDate = dto.releaseDate,
            primaryGenreName = dto.primaryGenreName,
            country = dto.country,
            previewUrl = dto.previewUrl
        )
    }
}