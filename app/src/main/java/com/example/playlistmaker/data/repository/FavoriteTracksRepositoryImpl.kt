package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.db.AppDatabase
import com.example.playlistmaker.data.db.FavoriteTrackMapper
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.FavoriteTracksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteTracksRepositoryImpl(
    private val appDatabase: AppDatabase
) : FavoriteTracksRepository {

    override suspend fun addTrack(track: Track) {
        appDatabase.favoriteTrackDao().insertTrack(FavoriteTrackMapper.map(track))
    }

    override suspend fun removeTrack(track: Track) {
        appDatabase.favoriteTrackDao().deleteTrack(FavoriteTrackMapper.map(track, 0L))
    }

    override fun getFavoriteTracks(): Flow<List<Track>> {
        return appDatabase.favoriteTrackDao().getFavoriteTracks().map { entities ->
            entities.map(FavoriteTrackMapper::map)
        }
    }
}
