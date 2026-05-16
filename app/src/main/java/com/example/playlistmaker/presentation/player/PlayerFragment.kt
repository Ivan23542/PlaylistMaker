package com.example.playlistmaker.presentation.player

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.media.playlists.PlaylistBottomSheetAdapter
import com.example.playlistmaker.presentation.playlist.NewPlaylistFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
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
    private var isPlayerServiceBound = false

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onNotificationPermissionChanged(isGranted)
    }

    private val playerServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val playerService = (service as AudioPlayerService.PlayerBinder).getService()
            viewModel.onPlayerServiceConnected(playerService)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            viewModel.onPlayerServiceDisconnected()
        }
    }

    private lateinit var favoriteButton: ImageButton
    private lateinit var addToPlaylistButton: ImageButton
    private lateinit var playButton: PlaybackButtonView

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
    private lateinit var overlayView: View
    private lateinit var playlistsBottomSheet: LinearLayout
    private lateinit var playlistsRecyclerView: RecyclerView
    private lateinit var newPlaylistButton: View
    private lateinit var playlistsAdapter: PlaylistBottomSheetAdapter

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { target, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            target.updatePadding(top = systemBars.top, bottom = systemBars.bottom)
            insets
        }

        setupViews(view)
        setupBottomSheet()
        setupFavoriteButton()
        setupAddToPlaylistButton()
        observeViewModel()
        observeResults()
        requestNotificationPermissionIfNeeded()
        bindPlayerService()

        view.findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            findNavController().navigateUp()
        }
        playButton.setOnClickListener { viewModel.onPlayButtonClicked() }
        newPlaylistButton.setOnClickListener {
            hideBottomSheet()
            findNavController().navigate(R.id.action_playerFragment_to_newPlaylistFragment)
        }
        overlayView.setOnClickListener { hideBottomSheet() }
    }

    override fun onStart() {
        super.onStart()
        viewModel.onPlayerScreenVisible()
    }

    override fun onStop() {
        viewModel.onPlayerScreenHidden(canShowPlaybackNotification())
        super.onStop()
    }

    override fun onDestroyView() {
        viewModel.onPlayerScreenClosed()
        unbindPlayerService()
        playlistsRecyclerView.adapter = null
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
        overlayView = root.findViewById(R.id.overlayView)
        playlistsBottomSheet = root.findViewById(R.id.playlistsBottomSheet)
        playlistsRecyclerView = root.findViewById(R.id.playlistsRecyclerView)
        newPlaylistButton = root.findViewById(R.id.newPlaylistButton)

        playlistsAdapter = PlaylistBottomSheetAdapter(emptyList()) { playlist ->
            viewModel.onPlaylistSelected(playlist)
        }
        playlistsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        playlistsRecyclerView.adapter = playlistsAdapter
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(playlistsBottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
            isHideable = true
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    overlayView.visibility = View.GONE
                    overlayView.alpha = 0f
                } else {
                    overlayView.visibility = View.VISIBLE
                    overlayView.alpha = 1f
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                overlayView.alpha = ((slideOffset + 1f) / 2f).coerceIn(0f, 1f)
            }
        })
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            if (boundTrackId != state.track.trackId) {
                bindTrack(state)
                boundTrackId = state.track.trackId
            }

            progressTextView.text = state.progress
            playButton.isEnabled = state.isPlayButtonEnabled
            playButton.setPlaying(state.isPlaying)

            if (state.isFavorite) {
                favoriteButton.setImageResource(R.drawable.ic_favorite_filled)
                favoriteButton.clearColorFilter()
            } else {
                favoriteButton.setImageResource(R.drawable.ic_favorite)
                favoriteButton.setColorFilter(requireContext().getColor(R.color.player_small_icon_color))
            }
        }

        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            playlistsAdapter.updatePlaylists(playlists)
        }

        viewModel.playlistEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                null -> Unit
                is PlayerPlaylistEvent.PlaylistCreated -> {
                    showToast(getString(R.string.playlist_created_message, event.playlistName))
                    viewModel.onPlaylistEventHandled()
                }
                is PlayerPlaylistEvent.TrackAdded -> {
                    hideBottomSheet()
                    showToast(getString(R.string.track_added_to_playlist_message, event.playlistName))
                    viewModel.onPlaylistEventHandled()
                }
                is PlayerPlaylistEvent.TrackAlreadyAdded -> {
                    showToast(
                        getString(
                            R.string.track_already_added_to_playlist_message,
                            event.playlistName
                        )
                    )
                    viewModel.onPlaylistEventHandled()
                }
            }
        }
    }

    private fun observeResults() {
        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<String>(NewPlaylistFragment.RESULT_PLAYLIST_CREATED)
            ?.observe(viewLifecycleOwner) { playlistName ->
                viewModel.onPlaylistCreated(playlistName)
                findNavController().currentBackStackEntry
                    ?.savedStateHandle
                    ?.remove<String>(NewPlaylistFragment.RESULT_PLAYLIST_CREATED)
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
        addToPlaylistButton.setOnClickListener { showBottomSheet() }
    }

    private fun bindPlayerService() {
        val intent = AudioPlayerService.createIntent(requireContext(), track)
        isPlayerServiceBound = requireContext().bindService(
            intent,
            playerServiceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    private fun unbindPlayerService() {
        if (isPlayerServiceBound) {
            requireContext().unbindService(playerServiceConnection)
            isPlayerServiceBound = false
        }
        viewModel.onPlayerServiceDisconnected()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun canShowPlaybackNotification(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun hideBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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
