package com.example.playlistmaker.presentation.media.playlists

import android.content.Context
import com.example.playlistmaker.R

fun formatTrackCount(context: Context, count: Int): String {
    val remainderTen = count % 10
    val remainderHundred = count % 100
    val trackWordRes = when {
        remainderTen == 1 && remainderHundred != 11 -> R.string.track_word_single
        remainderTen in 2..4 && remainderHundred !in 12..14 -> R.string.track_word_few
        else -> R.string.track_word_many
    }

    return context.getString(
        R.string.playlist_tracks_count_format,
        count,
        context.getString(trackWordRes)
    )
}
