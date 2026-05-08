package com.example.playlistmaker.presentation.media.playlists

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.Playlist
import java.io.File

class PlaylistGridViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.item_playlist_grid, parent, false)
) {

    private val coverImageView: ImageView = itemView.findViewById(R.id.playlistCoverImageView)
    private val titleTextView: TextView = itemView.findViewById(R.id.playlistTitleTextView)
    private val tracksCountTextView: TextView = itemView.findViewById(R.id.playlistTracksCountTextView)

    fun bind(playlist: Playlist) {
        titleTextView.text = playlist.name
        tracksCountTextView.text = formatTrackCount(itemView.context, playlist.tracksCount)

        Glide.with(itemView)
            .load(playlist.coverPath?.takeIf { it.isNotBlank() }?.let(::File))
            .placeholder(R.drawable.vector)
            .error(R.drawable.vector)
            .centerCrop()
            .transform(
                RoundedCorners(
                    itemView.resources.getDimensionPixelSize(R.dimen.track_artwork_corner_radius)
                )
            )
            .into(coverImageView)
    }
}
