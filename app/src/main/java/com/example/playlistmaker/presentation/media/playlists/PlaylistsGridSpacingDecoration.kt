package com.example.playlistmaker.presentation.media.playlists

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class PlaylistsGridSpacingDecoration(
    private val spanCount: Int,
    private val spacing: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        val column = position % spanCount
        outRect.left = if (column == 0) 0 else spacing / 2
        outRect.right = if (column == spanCount - 1) 0 else spacing / 2
        outRect.top = if (position >= spanCount) spacing else 0
    }
}
