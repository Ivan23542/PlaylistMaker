package com.example.playlistmaker.creator

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.data.di.DataModule
import com.example.playlistmaker.domain.interactor.PlayerInteractor
import com.example.playlistmaker.domain.interactor.PlayerInteractorImpl
import com.example.playlistmaker.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.domain.interactor.PlaylistInteractorImpl
import com.example.playlistmaker.domain.interactor.SearchHistoryInteractor
import com.example.playlistmaker.domain.interactor.SearchHistoryInteractorImpl
import com.example.playlistmaker.domain.interactor.SettingsInteractor
import com.example.playlistmaker.domain.interactor.SettingsInteractorImpl
import com.example.playlistmaker.domain.interactor.TracksInteractor
import com.example.playlistmaker.domain.interactor.TracksInteractorImpl
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.player.PlayerViewModel
import com.example.playlistmaker.presentation.playlist.NewPlaylistViewModel
import com.example.playlistmaker.presentation.search.SearchViewModel
import com.example.playlistmaker.presentation.settings.SettingsViewModel

object Creator {

    fun provideTracksInteractor(): TracksInteractor {
        return TracksInteractorImpl(DataModule.provideTracksRepository())
    }

    fun provideSearchHistoryInteractor(context: Context): SearchHistoryInteractor {
        return SearchHistoryInteractorImpl(DataModule.provideSearchHistoryRepository(context))
    }

    fun providePlayerInteractor(): PlayerInteractor {
        return PlayerInteractorImpl(DataModule.providePlayerRepository())
    }

    fun providePlaylistInteractor(context: Context): PlaylistInteractor {
        return PlaylistInteractorImpl(DataModule.providePlaylistRepository(context))
    }

    fun provideSettingsInteractor(context: Context): SettingsInteractor {
        return SettingsInteractorImpl(DataModule.provideSettingsRepository(context))
    }

    fun provideSearchViewModelFactory(context: Context): ViewModelProvider.Factory {
        return factory {
            SearchViewModel(
                tracksInteractor = provideTracksInteractor(),
                searchHistoryInteractor = provideSearchHistoryInteractor(context)
            )
        }
    }

    fun provideSettingsViewModelFactory(context: Context): ViewModelProvider.Factory {
        return factory {
            SettingsViewModel(provideSettingsInteractor(context))
        }
    }

    fun provideNewPlaylistViewModelFactory(context: Context): ViewModelProvider.Factory {
        return factory {
            NewPlaylistViewModel(providePlaylistInteractor(context))
        }
    }

    fun providePlayerViewModelFactory(track: Track): ViewModelProvider.Factory {
        return factory {
            PlayerViewModel(
                track = track,
                playerInteractor = providePlayerInteractor()
            )
        }
    }

    private fun <T : ViewModel> factory(creator: () -> T): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
                @Suppress("UNCHECKED_CAST")
                return creator() as VM
            }
        }
    }
}
