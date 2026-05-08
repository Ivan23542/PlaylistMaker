package com.example.playlistmaker.di

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.main.MainViewModel
import com.example.playlistmaker.presentation.media.MediatekaViewModel
import com.example.playlistmaker.presentation.media.favorites.FavoriteTracksViewModel
import com.example.playlistmaker.presentation.media.playlists.PlaylistsViewModel
import com.example.playlistmaker.presentation.player.PlayerViewModel
import com.example.playlistmaker.presentation.playlist.NewPlaylistViewModel
import com.example.playlistmaker.presentation.search.SearchViewModel
import com.example.playlistmaker.presentation.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel { MainViewModel() }

    viewModel { MediatekaViewModel() }

    viewModel { FavoriteTracksViewModel(get()) }

    viewModel { PlaylistsViewModel(get()) }

    viewModel { SearchViewModel(get(), get(), get()) }

    viewModel { SettingsViewModel(get()) }

    viewModel { NewPlaylistViewModel(get()) }

    viewModel { (track: Track) ->
        PlayerViewModel(track, get(), get(), get())
    }
}
