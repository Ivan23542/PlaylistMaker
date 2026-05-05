package com.example.playlistmaker.presentation.player

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.playlist.NewPlaylistActivity
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PlayerFragment : Fragment(R.layout.activity_player) {

    private val track: Track by lazy(LazyThreadSafetyMode.NONE) {
        requireNotNull(readTrackFromArguments())
    }
    private val viewModel: PlayerViewModel by viewModel {
        parametersOf(track)
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
            if (result.resultCode == Activity.RESULT_OK) {
                val playlistName =
                    result.data?.getStringExtra(NewPlaylistActivity.PLAYLIST_NAME_EXTRA)

                if (!playlistName.isNullOrBlank()) {
                    viewModel.onPlaylistCreated(playlistName)
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { target, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            target.updatePadding(top = systemBars.top, bottom = systemBars.bottom)
            insets
        }

        setupViews(view)
        setupFavoriteButton()
        setupAddToPlaylistButton()
        observeViewModel()

        view.findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            findNavController().navigateUp()
        }
        playButton.setOnClickListener { viewModel.onPlayButtonClicked() }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    override fun onDestroyView() {
        boundTrackId = null
        super.onDestroyView()
    }

    private fun setupViews(root: View) {
        favoriteButton = root.findViewById(R.id.favoriteButton)
        addToPlaylistButton = root.findViewById(R.id.addToPlaylistButton)
        playButton = root.findViewById(R.id.playButton)

        coverImageView = root.findViewById(R.id.coverImageView)
        trackNameTextView = root.findViewById(R.id.trackNameTextView)
        artistNameTextView = root.findViewById(R.id.artistNameTextView)
        durationValueTextView = root.findViewById(R.id.durationValueTextView)
        progressTextView = root.findViewById(R.id.progressTextView)
        albumTitleTextView = root.findViewById(R.id.albumTitleTextView)
        albumValueTextView = root.findViewById(R.id.albumValueTextView)
        yearTitleTextView = root.findViewById(R.id.yearTitleTextView)
        yearValueTextView = root.findViewById(R.id.yearValueTextView)
        genreValueTextView = root.findViewById(R.id.genreValueTextView)
        countryValueTextView = root.findViewById(R.id.countryValueTextView)
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
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
                favoriteButton.setColorFilter(requireContext().getColor(R.color.player_small_icon_color))
            }

            state.createdPlaylistName?.let { playlistName ->
                showPlaylistCreatedSnackbar(playlistName)
                viewModel.onPlaylistCreatedHandled()
            }
        }
    }

    private fun bindTrack(state: PlayerUiState) {
        val currentTrack = state.track
        trackNameTextView.text = currentTrack.trackName
        artistNameTextView.text = currentTrack.artistName
        durationValueTextView.text = currentTrack.trackTime
        progressTextView.text = state.progress
        genreValueTextView.text = currentTrack.primaryGenreName ?: ""
        countryValueTextView.text = currentTrack.country ?: ""

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
            val intent = Intent(requireContext(), NewPlaylistActivity::class.java)
            newPlaylistLauncher.launch(intent)
        }
    }

    private fun showPlaylistCreatedSnackbar(playlistName: String) {
        val snackbar = Snackbar.make(
            requireView(),
            getString(R.string.playlist_created_message, playlistName),
            Snackbar.LENGTH_LONG
        )

        snackbar.view.setBackgroundResource(R.drawable.snackbar_bg)

        snackbar.view.findViewById<TextView>(
            com.google.android.material.R.id.snackbar_text
        ).apply {
            gravity = Gravity.CENTER
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setTextColor(requireContext().getColor(R.color.snackbar_text))
        }

        snackbar.show()
    }

    @Suppress("DEPRECATION")
    private fun readTrackFromArguments(): Track? {
        val args = arguments ?: return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            args.getSerializable(ARG_TRACK, Track::class.java)
        } else {
            args.getSerializable(ARG_TRACK) as? Track
        }
    }

    companion object {
        const val ARG_TRACK = "track"
    }
}
