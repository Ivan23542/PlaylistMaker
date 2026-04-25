package com.example.playlistmaker

import java.io.Serializable

data class Playlist(
    val name: String,
    val description: String
) : Serializable