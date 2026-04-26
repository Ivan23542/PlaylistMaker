package com.example.playlistmaker

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerActivity : AppCompatActivity() {

    private var isFavorite = false
    private var playerState = STATE_DEFAULT

    private lateinit var favoriteButton: ImageButton
    private lateinit var addToPlaylistButton: ImageButton
    private lateinit var playButton: ImageButton

    private lateinit var coverImageView: ImageView
    private lateinit var trackNameTextView: TextView
    private lateinit var artistNameTextView: TextView
    private lateinit var durationValueTextView: TextView
    private lateinit var progressTextView: TextView
    private lateinit var albumTitleTextView: TextView
    private lateinit var albumValueTextView: TextView
    private lateinit var yearTitleTextView: TextView
    private lateinit var yearValueTextView: TextView
    private lateinit var genreValueTextView: TextView
    private lateinit var countryValueTextView: TextView

    private var mediaPlayer = MediaPlayer()
    private val handler = Handler(Looper.getMainLooper())

    private val progressRunnable = object : Runnable {
        override fun run() {
            progressTextView.text = SimpleDateFormat("mm:ss", Locale.getDefault())
                .format(mediaPlayer.currentPosition)
            handler.postDelayed(this, PROGRESS_DELAY)
        }
    }

    private val newPlaylistLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val playlistName =
                    result.data?.getStringExtra(NewPlaylistActivity.PLAYLIST_NAME_EXTRA)

                if (!playlistName.isNullOrBlank()) {
                    showPlaylistCreatedSnackbar(playlistName)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)

        val rootView = findViewById<View>(R.id.rootView)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                top = systemBars.top,
                bottom = systemBars.bottom
            )
            insets
        }

        setupViews()
        setupFavoriteButton()
        setupAddToPlaylistButton()

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            stopPlayer()
            finish()
        }

        val track = intent.getSerializableExtra(PoiskActivity.TRACK_EXTRA) as? Track
        if (track == null) {
            finish()
            return
        }

        bindTrack(track)
        preparePlayer(track.previewUrl)

        playButton.setOnClickListener {
            playbackControl()
        }
    }

    private fun setupViews() {
        favoriteButton = findViewById(R.id.favoriteButton)
        addToPlaylistButton = findViewById(R.id.addToPlaylistButton)
        playButton = findViewById(R.id.playButton)

        coverImageView = findViewById(R.id.coverImageView)
        trackNameTextView = findViewById(R.id.trackNameTextView)
        artistNameTextView = findViewById(R.id.artistNameTextView)
        durationValueTextView = findViewById(R.id.durationValueTextView)
        progressTextView = findViewById(R.id.progressTextView)
        albumTitleTextView = findViewById(R.id.albumTitleTextView)
        albumValueTextView = findViewById(R.id.albumValueTextView)
        yearTitleTextView = findViewById(R.id.yearTitleTextView)
        yearValueTextView = findViewById(R.id.yearValueTextView)
        genreValueTextView = findViewById(R.id.genreValueTextView)
        countryValueTextView = findViewById(R.id.countryValueTextView)
    }

    private fun bindTrack(track: Track) {
        trackNameTextView.text = track.trackName
        artistNameTextView.text = track.artistName
        durationValueTextView.text = track.trackTime
        progressTextView.text = START_PROGRESS
        genreValueTextView.text = track.primaryGenreName ?: ""
        countryValueTextView.text = track.country ?: ""

        if (track.collectionName.isNullOrBlank()) {
            albumTitleTextView.visibility = View.GONE
            albumValueTextView.visibility = View.GONE
        } else {
            albumTitleTextView.visibility = View.VISIBLE
            albumValueTextView.visibility = View.VISIBLE
            albumValueTextView.text = track.collectionName
        }

        val year = track.releaseDate?.take(4)
        if (year.isNullOrBlank()) {
            yearTitleTextView.visibility = View.GONE
            yearValueTextView.visibility = View.GONE
        } else {
            yearTitleTextView.visibility = View.VISIBLE
            yearValueTextView.visibility = View.VISIBLE
            yearValueTextView.text = year
        }

        val artwork512 = track.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")

        Glide.with(this)
            .load(artwork512)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .centerCrop()
            .transform(
                RoundedCorners(
                    resources.getDimensionPixelSize(R.dimen.track_artwork_corner_radius)
                )
            )
            .into(coverImageView)
    }

    private fun preparePlayer(previewUrl: String?) {
        if (previewUrl.isNullOrBlank()) {
            playButton.isEnabled = false
            return
        }

        playButton.isEnabled = false

        try {
            mediaPlayer.setDataSource(previewUrl)
            mediaPlayer.prepareAsync()

            mediaPlayer.setOnPreparedListener {
                playerState = STATE_PREPARED
                playButton.isEnabled = true
            }

            mediaPlayer.setOnCompletionListener {
                playButton.setImageResource(R.drawable.ic_play)
                progressTextView.text = START_PROGRESS
                handler.removeCallbacks(progressRunnable)
                mediaPlayer.seekTo(0)
                playerState = STATE_PREPARED
            }

            mediaPlayer.setOnErrorListener { _, _, _ ->
                playButton.setImageResource(R.drawable.ic_play)
                progressTextView.text = START_PROGRESS
                handler.removeCallbacks(progressRunnable)
                playerState = STATE_DEFAULT
                true
            }
        } catch (e: Exception) {
            playButton.isEnabled = false
            playerState = STATE_DEFAULT
        }
    }

    private fun playbackControl() {
        when (playerState) {
            STATE_PLAYING -> pausePlayer()
            STATE_PREPARED, STATE_PAUSED -> startPlayer()
        }
    }

    private fun startPlayer() {
        mediaPlayer.start()
        playButton.setImageResource(R.drawable.ic_pause)
        playerState = STATE_PLAYING
        handler.post(progressRunnable)
    }

    private fun pausePlayer() {
        mediaPlayer.pause()
        playButton.setImageResource(R.drawable.ic_play)
        playerState = STATE_PAUSED
        handler.removeCallbacks(progressRunnable)
    }

    private fun stopPlayer() {
        if (playerState == STATE_PLAYING || playerState == STATE_PAUSED) {
            mediaPlayer.pause()
            mediaPlayer.seekTo(0)
        }

        playButton.setImageResource(R.drawable.ic_play)
        progressTextView.text = START_PROGRESS
        handler.removeCallbacks(progressRunnable)

        if (playerState != STATE_DEFAULT) {
            playerState = STATE_PREPARED
        }
    }

    private fun setupFavoriteButton() {
        favoriteButton.setOnClickListener {
            isFavorite = !isFavorite

            if (isFavorite) {
                favoriteButton.setImageResource(R.drawable.ic_favorite_filled)
                favoriteButton.clearColorFilter()
            } else {
                favoriteButton.setImageResource(R.drawable.ic_favorite)
                favoriteButton.setColorFilter(getColor(R.color.player_small_icon_color))
            }
        }
    }

    private fun setupAddToPlaylistButton() {
        addToPlaylistButton.setOnClickListener {
            val intent = Intent(this, NewPlaylistActivity::class.java)
            newPlaylistLauncher.launch(intent)
        }
    }

    private fun showPlaylistCreatedSnackbar(playlistName: String) {
        val snackbar = Snackbar.make(
            findViewById(R.id.rootView),
            "Плейлист «$playlistName» создан",
            Snackbar.LENGTH_LONG
        )

        snackbar.view.setBackgroundResource(R.drawable.snackbar_bg)

        snackbar.view.findViewById<TextView>(
            com.google.android.material.R.id.snackbar_text
        ).apply {
            gravity = Gravity.CENTER
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setTextColor(getColor(R.color.snackbar_text))
        }

        snackbar.show()
    }

    override fun onPause() {
        super.onPause()
        if (playerState == STATE_PLAYING) {
            pausePlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(progressRunnable)
        mediaPlayer.release()
    }

    companion object {
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3

        private const val PROGRESS_DELAY = 300L
        private const val START_PROGRESS = "00:00"
    }
}