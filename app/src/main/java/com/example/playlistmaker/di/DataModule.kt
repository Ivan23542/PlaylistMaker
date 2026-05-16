package com.example.playlistmaker.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.playlistmaker.data.db.AppDatabase
import com.example.playlistmaker.data.network.ITunesApi
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val ITUNES_BASE_URL = "https://itunes.apple.com/"
private const val PREFS_NAME = "playlist_maker_prefs"
private const val DATABASE_NAME = "playlist_maker_database"

private val migrationFrom1To2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `playlists` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `coverPath` TEXT,
                `trackIdsJson` TEXT NOT NULL,
                `tracksCount` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `playlist_tracks` (
                `trackId` INTEGER NOT NULL,
                `trackName` TEXT NOT NULL,
                `artistName` TEXT NOT NULL,
                `trackTime` TEXT NOT NULL,
                `artworkUrl100` TEXT NOT NULL,
                `collectionName` TEXT,
                `releaseDate` TEXT,
                `primaryGenreName` TEXT,
                `country` TEXT,
                `previewUrl` TEXT,
                PRIMARY KEY(`trackId`)
            )
            """.trimIndent()
        )
    }
}

val dataModule = module {

    single<ITunesApi> {
        Retrofit.Builder()
            .baseUrl(ITUNES_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ITunesApi::class.java)
    }

    single<SharedPreferences> {
        androidContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    single { Gson() }

    single<AppDatabase> {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            DATABASE_NAME
        )
            .addMigrations(migrationFrom1To2)
            .build()
    }
}
