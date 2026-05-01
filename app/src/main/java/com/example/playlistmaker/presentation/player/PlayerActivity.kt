package com.example.playlistmaker.presentation.player

import android.content.Intent
import android.os.Build
import android.os.Bundle
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
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.playlist.NewPlaylistActivity
import com.example.playlistmaker.presentation.search.PoiskActivity
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PlayerActivity : AppCompatActivity() {

    private val track: Track? by lazy { readTrackFromIntent() }
    private val viewModel: PlayerViewModel by viewModel {
        parametersOf(requireNotNull(track))
    }

    private var boundTrackId: Long? = null

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

    private val newPlaylistLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val playlistName =
                    result.data?.getStringExtra(NewPlaylistActivity.PLAYLIST_NAME_EXTRA)

                if (!playlistName.isNullOrBlank()) {
                    viewModel.onPlaylistCreated(playlistName)
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

        if (track == null) {
            finish()
            return
        }

        setupViews()
        setupFavoriteButton()
        setupAddToPlaylistButton()
        observeViewModel()

        findViewById<ImageButton>(R.id.backButton).setOnClickListener { finish() }
        playButton.setOnClickListener { viewModel.onPlayButtonClicked() }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
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

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            if (boundTrackId != state.track.trackId) {
                bindTrack(state)
                boundTrackId = state.track.trackId
            }

            progressTextView.text = state.progress
            playButton.isEnabled = state.isPlayButtonEnabled
            playButton.setImageResource(
                if (state.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            )

            if (state.isFavorite) {
                favoriteButton.setImageResource(R.drawable.ic_favorite_filled)
                favoriteButton.clearColorFilter()
            } else {
                favoriteButton.setImageResource(R.drawable.ic_favorite)
                favoriteButton.setColorFilter(getColor(R.color.player_small_icon_color))
            }

            state.createdPlaylistName?.let { playlistName ->
                showPlaylistCreatedSnackbar(playlistName)
                viewModel.onPlaylistCreatedHandled()
            }
        }
    }

    private fun bindTrack(state: PlayerUiState) {
        val track = state.track
        trackNameTextView.text = track.trackName
        artistNameTextView.text = track.artistName
        durationValueTextView.text = track.trackTime
        progressTextView.text = state.progress
        genreValueTextView.text = track.primaryGenreName ?: ""
        countryValueTextView.text = track.country ?: ""

        if (state.isAlbumVisible) {
            albumTitleTextView.visibility = View.VISIBLE
            albumValueTextView.visibility = View.VISIBLE
            albumValueTextView.text = state.album
        } else {
            albumTitleTextView.visibility = View.GONE
            albumValueTextView.visibility = View.GONE
        }

        if (state.isYearVisible) {
            yearTitleTextView.visibility = View.VISIBLE
            yearValueTextView.visibility = View.VISIBLE
            yearValueTextView.text = state.year
        } else {
            yearTitleTextView.visibility = View.GONE
            yearValueTextView.visibility = View.GONE
        }

        Glide.with(this)
            .load(state.artworkUrl)
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

    private fun setupFavoriteButton() {
        favoriteButton.setOnClickListener { viewModel.onFavoriteClicked() }
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
            getString(R.string.playlist_created_message, playlistName),
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

    @Suppress("DEPRECATION")
    private fun readTrackFromIntent(): Track? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(PoiskActivity.TRACK_EXTRA, Track::class.java)
        } else {
            intent.getSerializableExtra(PoiskActivity.TRACK_EXTRA) as? Track
        }
    }
}
