package com.example.playlistmaker.di

import com.example.playlistmaker.data.repository.FavoriteTracksRepositoryImpl
import com.example.playlistmaker.data.repository.PlaylistRepositoryImpl
import com.example.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.example.playlistmaker.data.repository.SettingsRepositoryImpl
import com.example.playlistmaker.data.repository.TracksRepositoryImpl
import com.example.playlistmaker.domain.repository.FavoriteTracksRepository
import com.example.playlistmaker.domain.repository.PlaylistRepository
import com.example.playlistmaker.domain.repository.SearchHistoryRepository
import com.example.playlistmaker.domain.repository.SettingsRepository
import com.example.playlistmaker.domain.repository.TracksRepository
import org.koin.dsl.module

val repositoryModule = module {

    single<TracksRepository> { TracksRepositoryImpl(get(), get()) }

    single<SearchHistoryRepository> { SearchHistoryRepositoryImpl(get(), get()) }

    single<FavoriteTracksRepository> { FavoriteTracksRepositoryImpl(get()) }

    single<SettingsRepository> { SettingsRepositoryImpl(get()) }

    single<PlaylistRepository> { PlaylistRepositoryImpl(get(), get(), get()) }
}
