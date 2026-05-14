package com.example.playlistmaker.presentation.search

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.search.TrackViewHolder

class TrackAdapter(
    private var tracks: List<Track>,
    private val onTrackLongClick: ((Track) -> Unit)? = null,
    private val onTrackClick: (Track) -> Unit
) : RecyclerView.Adapter<TrackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        return TrackViewHolder(parent)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.bind(track)
        holder.itemView.setOnClickListener {
            onTrackClick(track)
        }
        holder.itemView.setOnLongClickListener {
            onTrackLongClick?.invoke(track)
            onTrackLongClick != null
        }
    }

    override fun getItemCount(): Int = tracks.size

    fun updateTracks(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }
}
