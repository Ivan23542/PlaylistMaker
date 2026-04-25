package com.example.playlistmaker

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import android.content.Intent
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import androidx.activity.result.contract.ActivityResultContracts


class PlayerActivity : AppCompatActivity() {
    private var isFavorite = false
    private lateinit var favoriteButton: ImageButton
    private lateinit var addToPlaylistButton: ImageButton
    private var isPlaying = false
    private lateinit var coverImageView: ImageView
    private lateinit var trackNameTextView: TextView
    private lateinit var artistNameTextView: TextView
    private lateinit var durationValueTextView: TextView
    private lateinit var albumTitleTextView: TextView
    private lateinit var albumValueTextView: TextView
    private lateinit var yearTitleTextView: TextView
    private lateinit var yearValueTextView: TextView
    private lateinit var genreValueTextView: TextView
    private lateinit var countryValueTextView: TextView

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
            finish()
        }

        val track = intent.getSerializableExtra(PoiskActivity.TRACK_EXTRA) as? Track
        if (track != null) {
            bindTrack(track)
        }

        val playButton = findViewById<ImageButton>(R.id.playButton)

        playButton.setOnClickListener {
            isPlaying = !isPlaying

            if (isPlaying) {
                playButton.setImageResource(R.drawable.ic_pause)
            } else {
                playButton.setImageResource(R.drawable.ic_play)
            }
        }
    }

    private fun setupViews() {
        favoriteButton = findViewById(R.id.favoriteButton)
        addToPlaylistButton = findViewById(R.id.addToPlaylistButton)
        coverImageView = findViewById(R.id.coverImageView)
        trackNameTextView = findViewById(R.id.trackNameTextView)
        artistNameTextView = findViewById(R.id.artistNameTextView)
        durationValueTextView = findViewById(R.id.durationValueTextView)
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
            .transform(RoundedCorners(resources.getDimensionPixelSize(R.dimen.track_artwork_corner_radius)))
            .into(coverImageView)
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


    private val newPlaylistLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val playlistName =
                    result.data?.getStringExtra(NewPlaylistActivity.PLAYLIST_NAME_EXTRA)

                if (!playlistName.isNullOrBlank()) {
                    val snackbar = Snackbar.make(
                        findViewById(R.id.rootView),
                        "Плейлист «$playlistName» создан",
                        Snackbar.LENGTH_LONG
                    )

                    snackbar.view.setBackgroundResource(R.drawable.snackbar_bg)

                    snackbar.view.findViewById<TextView>(
                        com.google.android.material.R.id.snackbar_text
                    ).apply {
                        gravity = android.view.Gravity.CENTER
                        textAlignment = View.TEXT_ALIGNMENT_CENTER
                        setTextColor(getColor(R.color.snackbar_text))
                    }

                    snackbar.show()
                }
            }
        }
}