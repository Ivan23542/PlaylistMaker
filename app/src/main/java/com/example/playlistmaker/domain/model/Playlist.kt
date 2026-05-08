package com.example.playlistmaker.domain.model

import java.io.Serializable

data class Playlist(
    val id: Long = 0L,
    val name: String,
    val description: String,
    val coverPath: String? = null,
    val trackIds: List<Long> = emptyList(),
    val tracksCount: Int = 0
) : Serializable
