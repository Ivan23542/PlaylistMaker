package com.example.playlistmaker.data.db

import com.example.playlistmaker.domain.model.Playlist
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PlaylistMapper {

    private val trackIdsType = object : TypeToken<List<Long>>() {}.type

    fun map(playlist: Playlist, gson: Gson): PlaylistEntity {
        return PlaylistEntity(
            id = playlist.id,
            name = playlist.name,
            description = playlist.description,
            coverPath = playlist.coverPath,
            trackIdsJson = gson.toJson(playlist.trackIds),
            tracksCount = playlist.tracksCount
        )
    }

    fun map(entity: PlaylistEntity, gson: Gson): Playlist {
        return Playlist(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            coverPath = entity.coverPath,
            trackIds = gson.fromJson<List<Long>>(entity.trackIdsJson, trackIdsType).orEmpty(),
            tracksCount = entity.tracksCount
        )
    }
}
