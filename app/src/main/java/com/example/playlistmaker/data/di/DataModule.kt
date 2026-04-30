package com.example.playlistmaker.data.di

import android.content.Context
import com.example.playlistmaker.data.network.NetworkClient
import com.example.playlistmaker.data.player.PlayerRepositoryImpl
import com.example.playlistmaker.data.repository.PlaylistRepositoryImpl
import com.example.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.example.playlistmaker.data.repository.SettingsRepositoryImpl
import com.example.playlistmaker.data.repository.TracksRepositoryImpl
import com.example.playlistmaker.data.storage.PlaylistMakerPreferences
import com.example.playlistmaker.data.storage.PlaylistStorage
import com.example.playlistmaker.domain.repository.PlayerRepository
import com.example.playlistmaker.domain.repository.PlaylistRepository
import com.example.playlistmaker.domain.repository.SearchHistoryRepository
import com.example.playlistmaker.domain.repository.SettingsRepository
import com.example.playlistmaker.domain.repository.TracksRepository

object DataModule {

    fun provideTracksRepository(): TracksRepository {
        return TracksRepositoryImpl(NetworkClient.iTunesApi)
    }

    fun provideSearchHistoryRepository(context: Context): SearchHistoryRepository {
        return SearchHistoryRepositoryImpl(PlaylistMakerPreferences.getSharedPreferences(context))
    }

    fun provideSettingsRepository(context: Context): SettingsRepository {
        return SettingsRepositoryImpl(PlaylistMakerPreferences.getSharedPreferences(context))
    }

    fun providePlaylistRepository(context: Context): PlaylistRepository {
        return PlaylistRepositoryImpl(
            PlaylistStorage(PlaylistMakerPreferences.getSharedPreferences(context))
        )
    }

    fun providePlayerRepository(): PlayerRepository {
        return PlayerRepositoryImpl()
    }
}
