package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.db.AppDatabase
import com.example.playlistmaker.data.mapper.TrackMapper
import com.example.playlistmaker.data.network.ITunesApi
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.TracksRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class TracksRepositoryImpl(
    private val iTunesApi: ITunesApi,
    private val appDatabase: AppDatabase
) : TracksRepository {

    override fun searchTracks(expression: String): Flow<List<Track>> = flow {
        val response = iTunesApi.search(expression)
        val favoriteTrackIds = appDatabase.favoriteTrackDao().getFavoriteTrackIds().toHashSet()
        val tracks: List<Track> = response.results
            .map {
                TrackMapper.map(it).apply {
                    isFavorite = trackId in favoriteTrackIds
                }
            }
            .filter { it.trackName.isNotBlank() || it.artistName.isNotBlank() }

        emit(tracks)
    }.flowOn(Dispatchers.IO)
}
