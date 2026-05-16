package com.example.playlistmaker.di

import com.example.playlistmaker.domain.interactor.FavoriteTracksInteractor
import com.example.playlistmaker.domain.interactor.FavoriteTracksInteractorImpl
import com.example.playlistmaker.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.domain.interactor.PlaylistInteractorImpl
import com.example.playlistmaker.domain.interactor.SearchHistoryInteractor
import com.example.playlistmaker.domain.interactor.SearchHistoryInteractorImpl
import com.example.playlistmaker.domain.interactor.SettingsInteractor
import com.example.playlistmaker.domain.interactor.SettingsInteractorImpl
import com.example.playlistmaker.domain.interactor.TracksInteractor
import com.example.playlistmaker.domain.interactor.TracksInteractorImpl
import org.koin.dsl.module

val interactorModule = module {

    single<TracksInteractor> { TracksInteractorImpl(get()) }

    single<FavoriteTracksInteractor> { FavoriteTracksInteractorImpl(get()) }

    single<SearchHistoryInteractor> { SearchHistoryInteractorImpl(get()) }

    single<SettingsInteractor> { SettingsInteractorImpl(get()) }

    single<PlaylistInteractor> { PlaylistInteractorImpl(get()) }
}
