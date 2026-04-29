package com.example.playlistmaker.data.network

import com.example.playlistmaker.data.network.TrackDto

data class SearchResponse(
    val resultCount: Int,
    val results: List<TrackDto>
)