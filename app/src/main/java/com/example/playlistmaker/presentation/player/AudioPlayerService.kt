package com.example.playlistmaker.presentation.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AudioPlayerService : Service(), PlaybackServiceController {

    private val binder = PlayerBinder()
    private val mediaPlayer = MediaPlayer()
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(serviceJob + Dispatchers.Main.immediate)
    private val _playerState = MutableStateFlow(PlaybackServiceState())

    private var progressJob: Job? = null
    private var previewUrl: String? = null
    private var trackName: String = ""
    private var artistName: String = ""
    private var currentPlayerState = PlaybackPlayerState.DEFAULT
    private var isForeground = false

    inner class PlayerBinder : Binder() {
        fun getService(): AudioPlayerService = this@AudioPlayerService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        configureAudioAttributes()
    }

    override fun onBind(intent: Intent): IBinder {
        previewUrl = intent.getStringExtra(EXTRA_PREVIEW_URL)
        trackName = intent.getStringExtra(EXTRA_TRACK_NAME).orEmpty()
        artistName = intent.getStringExtra(EXTRA_ARTIST_NAME).orEmpty()
        preparePlayer(previewUrl)

        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        hideForegroundNotification()
        return super.onUnbind(intent)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopPlayer()
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        stopProgressUpdates()
        hideForegroundNotification()
        serviceJob.cancel()
        mediaPlayer.release()
        super.onDestroy()
    }

    override fun startPlayer() {
        if (currentPlayerState != PlaybackPlayerState.PREPARED &&
            currentPlayerState != PlaybackPlayerState.PAUSED
        ) {
            return
        }

        mediaPlayer.start()
        currentPlayerState = PlaybackPlayerState.PLAYING
        updatePlayerState(
            playerState = PlaybackPlayerState.PLAYING,
            progressMillis = mediaPlayer.currentPosition,
            isPlayButtonEnabled = true
        )
        startProgressUpdates()
    }

    override fun pausePlayer() {
        if (currentPlayerState != PlaybackPlayerState.PLAYING) {
            return
        }

        mediaPlayer.pause()
        currentPlayerState = PlaybackPlayerState.PAUSED
        stopProgressUpdates()
        updatePlayerState(
            playerState = PlaybackPlayerState.PAUSED,
            progressMillis = mediaPlayer.currentPosition,
            isPlayButtonEnabled = true
        )
        hideForegroundNotification()
    }

    override fun stopPlayer() {
        stopProgressUpdates()

        if (currentPlayerState != PlaybackPlayerState.DEFAULT) {
            runCatching {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                }
                mediaPlayer.seekTo(START_POSITION)
            }
        }

        currentPlayerState = if (_playerState.value.isPlayButtonEnabled) {
            PlaybackPlayerState.PREPARED
        } else {
            PlaybackPlayerState.DEFAULT
        }
        updatePlayerState(
            playerState = currentPlayerState,
            progressMillis = START_POSITION,
            isPlayButtonEnabled = _playerState.value.isPlayButtonEnabled
        )
        hideForegroundNotification()
    }

    override fun getPlayerState(): PlaybackServiceState {
        return _playerState.value
    }

    override fun observePlayerState(): StateFlow<PlaybackServiceState> {
        return _playerState
    }

    override fun showForegroundNotification() {
        if (!_playerState.value.isPlaying || isForeground) {
            return
        }

        val foregroundServiceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        } else {
            0
        }

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            createPlaybackNotification(),
            foregroundServiceType
        )
        isForeground = true
    }

    override fun hideForegroundNotification() {
        if (isForeground) {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            isForeground = false
        }
    }

    private fun preparePlayer(url: String?) {
        stopProgressUpdates()
        hideForegroundNotification()
        mediaPlayer.reset()
        configureAudioAttributes()

        if (url.isNullOrBlank()) {
            currentPlayerState = PlaybackPlayerState.DEFAULT
            updatePlayerState(
                playerState = PlaybackPlayerState.DEFAULT,
                progressMillis = START_POSITION,
                isPlayButtonEnabled = false
            )
            return
        }

        currentPlayerState = PlaybackPlayerState.DEFAULT
        updatePlayerState(
            playerState = PlaybackPlayerState.DEFAULT,
            progressMillis = START_POSITION,
            isPlayButtonEnabled = false
        )

        mediaPlayer.setOnPreparedListener {
            currentPlayerState = PlaybackPlayerState.PREPARED
            updatePlayerState(
                playerState = PlaybackPlayerState.PREPARED,
                progressMillis = START_POSITION,
                isPlayButtonEnabled = true
            )
        }
        mediaPlayer.setOnCompletionListener {
            it.seekTo(START_POSITION)
            stopProgressUpdates()
            currentPlayerState = PlaybackPlayerState.PREPARED
            updatePlayerState(
                playerState = PlaybackPlayerState.PREPARED,
                progressMillis = START_POSITION,
                isPlayButtonEnabled = true
            )
            hideForegroundNotification()
        }
        mediaPlayer.setOnErrorListener { _, _, _ ->
            handlePlayerError()
            true
        }

        runCatching {
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepareAsync()
        }.onFailure {
            handlePlayerError()
        }
    }

    private fun handlePlayerError() {
        stopProgressUpdates()
        hideForegroundNotification()
        currentPlayerState = PlaybackPlayerState.DEFAULT
        updatePlayerState(
            playerState = PlaybackPlayerState.DEFAULT,
            progressMillis = START_POSITION,
            isPlayButtonEnabled = false
        )
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressJob = serviceScope.launch {
            while (isActive && currentPlayerState == PlaybackPlayerState.PLAYING) {
                updatePlayerState(
                    playerState = PlaybackPlayerState.PLAYING,
                    progressMillis = mediaPlayer.currentPosition,
                    isPlayButtonEnabled = true
                )
                delay(PROGRESS_UPDATE_DELAY)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun updatePlayerState(
        playerState: PlaybackPlayerState,
        progressMillis: Int,
        isPlayButtonEnabled: Boolean
    ) {
        _playerState.value = PlaybackServiceState(
            playerState = playerState,
            progressMillis = progressMillis,
            isPlayButtonEnabled = isPlayButtonEnabled
        )
    }

    private fun configureAudioAttributes() {
        mediaPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(R.string.playback_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun createPlaybackNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.playback_notification_text, artistName, trackName))
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        private const val EXTRA_PREVIEW_URL = "previewUrl"
        private const val EXTRA_TRACK_NAME = "trackName"
        private const val EXTRA_ARTIST_NAME = "artistName"
        private const val NOTIFICATION_CHANNEL_ID = "playback_channel"
        private const val NOTIFICATION_ID = 1001
        private const val PROGRESS_UPDATE_DELAY = 300L
        private const val START_POSITION = 0

        fun createIntent(context: Context, track: Track): Intent {
            return Intent(context, AudioPlayerService::class.java).apply {
                putExtra(EXTRA_PREVIEW_URL, track.previewUrl)
                putExtra(EXTRA_TRACK_NAME, track.trackName)
                putExtra(EXTRA_ARTIST_NAME, track.artistName)
            }
        }
    }
}
