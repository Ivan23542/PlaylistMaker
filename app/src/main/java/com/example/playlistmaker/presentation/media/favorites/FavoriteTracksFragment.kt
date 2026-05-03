package com.example.playlistmaker.presentation.media.favorites

import androidx.fragment.app.Fragment
import com.example.playlistmaker.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoriteTracksFragment : Fragment(R.layout.fragment_favorite_tracks) {

    private val viewModel: FavoriteTracksViewModel by viewModel()
}
