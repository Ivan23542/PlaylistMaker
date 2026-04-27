package com.example.playlistmaker.domain.model

import java.io.Serializable

data class Playlist(
    val name: String,
    val description: String
) : Serializable