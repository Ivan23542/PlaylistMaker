package com.example.playlistmaker.data.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.playlistmaker.data.db.AppDatabase
import com.example.playlistmaker.data.db.PlaylistMapper
import com.example.playlistmaker.data.db.PlaylistTrackMapper
import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.PlaylistRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.io.FileOutputStream

class PlaylistRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val gson: Gson,
    private val context: Context
) : PlaylistRepository {

    override suspend fun savePlaylist(playlist: Playlist) {
        val savedCoverPath = copyCoverToPrivateStorage(playlist.coverPath)
        val playlistToSave = playlist.copy(coverPath = savedCoverPath)
        appDatabase.playlistDao().insertPlaylist(PlaylistMapper.map(playlistToSave, gson))
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        val savedCoverPath = copyCoverToPrivateStorage(playlist.coverPath)
        val playlistToSave = playlist.copy(coverPath = savedCoverPath)
        appDatabase.playlistDao().updatePlaylist(PlaylistMapper.map(playlistToSave, gson))
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return appDatabase.playlistDao().getPlaylists().map { playlists ->
            playlists.map { PlaylistMapper.map(it, gson) }
        }.flowOn(Dispatchers.IO)
    }

    override fun getPlaylist(playlistId: Long): Flow<Playlist?> {
        return appDatabase.playlistDao().observePlaylistById(playlistId).map { playlist ->
            playlist?.let { PlaylistMapper.map(it, gson) }
        }.flowOn(Dispatchers.IO)
    }

    override fun getPlaylistTracks(trackIds: List<Long>): Flow<List<Track>> {
        return appDatabase.playlistTrackDao().getTracks().map { tracks ->
            val tracksById = tracks.associateBy { it.trackId }
            trackIds.asReversed()
                .mapNotNull { trackId -> tracksById[trackId] }
                .map(PlaylistTrackMapper::map)
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist): Boolean {
        val actualPlaylist = appDatabase.playlistDao().getPlaylistById(playlist.id) ?: return false
        val actualPlaylistDomain = PlaylistMapper.map(actualPlaylist, gson)

        if (track.trackId in actualPlaylistDomain.trackIds) {
            return false
        }

        val updatedPlaylist = actualPlaylistDomain.copy(
            trackIds = actualPlaylistDomain.trackIds + track.trackId,
            tracksCount = actualPlaylistDomain.tracksCount + 1
        )

        appDatabase.playlistTrackDao().insertTrack(PlaylistTrackMapper.map(track))
        appDatabase.playlistDao().updatePlaylist(PlaylistMapper.map(updatedPlaylist, gson))

        return true
    }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        val playlistEntity = appDatabase.playlistDao().getPlaylistById(playlistId) ?: return
        val playlist = PlaylistMapper.map(playlistEntity, gson)
        val updatedTrackIds = playlist.trackIds.filterNot { it == trackId }

        appDatabase.playlistDao().updatePlaylist(
            PlaylistMapper.map(
                playlist.copy(
                    trackIds = updatedTrackIds,
                    tracksCount = updatedTrackIds.size
                ),
                gson
            )
        )

        deleteTrackIfOrphan(trackId)
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        val playlistEntity = appDatabase.playlistDao().getPlaylistById(playlistId) ?: return
        val playlist = PlaylistMapper.map(playlistEntity, gson)

        appDatabase.playlistDao().deletePlaylistById(playlistId)
        playlist.trackIds.forEach { trackId ->
            deleteTrackIfOrphan(trackId)
        }
    }

    private suspend fun deleteTrackIfOrphan(trackId: Long) {
        val isTrackStillUsed = appDatabase.playlistDao()
            .getPlaylistsOnce()
            .map { PlaylistMapper.map(it, gson) }
            .any { playlist -> trackId in playlist.trackIds }

        if (!isTrackStillUsed) {
            appDatabase.playlistTrackDao().deleteTrackById(trackId)
        }
    }

    private fun copyCoverToPrivateStorage(coverPath: String?): String? {
        if (coverPath.isNullOrBlank()) return null
        if (!coverPath.startsWith(CONTENT_SCHEME_PREFIX)) return coverPath

        return runCatching {
            val uri = Uri.parse(coverPath)
            val extension = resolveFileExtension(uri)
            val coversDirectory = File(context.filesDir, PLAYLIST_COVERS_DIRECTORY).apply {
                mkdirs()
            }
            val outputFile = File(
                coversDirectory,
                "${PLAYLIST_COVER_FILE_PREFIX}${System.currentTimeMillis()}.$extension"
            )

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return null

            outputFile.absolutePath
        }
            .getOrNull()
    }

    private fun resolveFileExtension(uri: Uri): String {
        val mimeType = context.contentResolver.getType(uri)
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(mimeType)
            ?.takeIf { it.isNotBlank() }
            ?: DEFAULT_COVER_EXTENSION
    }

    private companion object {
        private const val PLAYLIST_COVERS_DIRECTORY = "playlist_covers"
        private const val PLAYLIST_COVER_FILE_PREFIX = "playlist_cover_"
        private const val CONTENT_SCHEME_PREFIX = "content://"
        private const val DEFAULT_COVER_EXTENSION = "jpg"
    }
}
