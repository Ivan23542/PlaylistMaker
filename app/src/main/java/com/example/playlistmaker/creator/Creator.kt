package com.example.playlistmaker.creator

import android.content.Context
import com.example.playlistmaker.data.network.ITunesApi
import com.example.playlistmaker.data.player.PlayerRepositoryImpl
import com.example.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.example.playlistmaker.data.repository.TracksRepositoryImpl
import com.example.playlistmaker.data.storage.PlaylistStorage
import com.example.playlistmaker.domain.interactor.PlayerInteractor
import com.example.playlistmaker.domain.interactor.SearchHistoryInteractor
import com.example.playlistmaker.domain.interactor.SearchHistoryInteractorImpl
import com.example.playlistmaker.domain.interactor.TracksInteractor
import com.example.playlistmaker.domain.interactor.TracksInteractorImpl
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.playlistmaker.App
import com.example.playlistmaker.data.repository.SettingsRepositoryImpl
import com.example.playlistmaker.domain.interactor.SettingsInteractor
import com.example.playlistmaker.domain.interactor.SettingsInteractorImpl



object Creator {
    private const val BASE_URL = "https://itunes.apple.com"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val iTunesApi: ITunesApi by lazy {
        retrofit.create(ITunesApi::class.java)
    }

    fun provideTracksInteractor(): TracksInteractor {
        return TracksInteractorImpl(TracksRepositoryImpl(iTunesApi))
    }

    fun provideSearchHistoryInteractor(context: Context): SearchHistoryInteractor {
        val sharedPreferences = context.getSharedPreferences(
            "playlist_maker_prefs",
            Context.MODE_PRIVATE
        )

        return SearchHistoryInteractorImpl(
            SearchHistoryRepositoryImpl(sharedPreferences)
        )
    }

    fun providePlayerInteractor(): PlayerInteractor {
        return PlayerRepositoryImpl()
    }


    fun providePlaylistStorage(context: Context): PlaylistStorage {
        return PlaylistStorage(context.getSharedPreferences("playlist_maker_prefs", Context.MODE_PRIVATE))
    }

    fun provideSettingsInteractor(context: Context): SettingsInteractor {
        val sharedPreferences = context.getSharedPreferences(App.PREFS_NAME, Context.MODE_PRIVATE)
        return SettingsInteractorImpl(
            SettingsRepositoryImpl(sharedPreferences)
        )
    }
}