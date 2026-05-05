package com.example.playlistmaker.presentation.media

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.playlistmaker.presentation.media.favorites.FavoriteTracksFragment
import com.example.playlistmaker.presentation.media.playlists.PlaylistsFragment

class MediatekaPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = TAB_COUNT

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            FAVORITES_POSITION -> FavoriteTracksFragment()
            PLAYLISTS_POSITION -> PlaylistsFragment()
            else -> error("Unsupported tab position: $position")
        }
    }

    private companion object {
        private const val TAB_COUNT = 2
        private const val FAVORITES_POSITION = 0
        private const val PLAYLISTS_POSITION = 1
    }
}
