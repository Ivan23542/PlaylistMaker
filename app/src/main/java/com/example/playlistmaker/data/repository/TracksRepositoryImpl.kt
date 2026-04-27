package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.mapper.TrackMapper
import com.example.playlistmaker.data.network.ITunesApi
import com.example.playlistmaker.data.network.SearchResponse
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.TracksRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TracksRepositoryImpl(
    private val iTunesApi: ITunesApi
) : TracksRepository {

    override fun searchTracks(
        expression: String,
        consumer: TracksRepository.TracksConsumer
    ) {
        iTunesApi.search(expression).enqueue(object : Callback<SearchResponse> {
            override fun onResponse(
                call: Call<SearchResponse>,
                response: Response<SearchResponse>
            ) {
                if (!response.isSuccessful) {
                    consumer.consume(null, "Ошибка сервера")
                    return
                }

                val tracks: List<Track> = response.body()?.results
                    ?.map { TrackMapper.map(it) }
                    ?.filter { it.trackName.isNotBlank() || it.artistName.isNotBlank() }
                    ?: emptyList()

                consumer.consume(tracks, null)
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                consumer.consume(null, "Ошибка подключения")
            }
        })
    }
}