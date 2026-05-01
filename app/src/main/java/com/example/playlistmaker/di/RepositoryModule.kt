package com.example.playlistmaker.di

import com.example.playlistmaker.data.player.PlayerRepositoryImpl
import com.example.playlistmaker.data.repository.PlaylistRepositoryImpl
import com.example.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.example.playlistmaker.data.repository.SettingsRepositoryImpl
import com.example.playlistmaker.data.repository.TracksRepositoryImpl
import com.example.playlistmaker.domain.repository.PlayerRepository
import com.example.playlistmaker.domain.repository.PlaylistRepository
import com.example.playlistmaker.domain.repository.SearchHistoryRepository
import com.example.playlistmaker.domain.repository.SettingsRepository
import com.example.playlistmaker.domain.repository.TracksRepository
import org.koin.dsl.module

val repositoryModule = module {

    single<TracksRepository> { TracksRepositoryImpl(get()) }

    single<SearchHistoryRepository> { SearchHistoryRepositoryImpl(get(), get()) }

    single<SettingsRepository> { SettingsRepositoryImpl(get()) }

    single<PlaylistRepository> { PlaylistRepositoryImpl(get()) }

    factory<PlayerRepository> { PlayerRepositoryImpl() }
}
