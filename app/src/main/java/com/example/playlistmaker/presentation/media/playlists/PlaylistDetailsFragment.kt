package com.example.playlistmaker.presentation.media.playlists

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.player.PlayerFragment
import com.example.playlistmaker.presentation.playlist.NewPlaylistFragment
import com.example.playlistmaker.presentation.search.TrackAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.io.File

class PlaylistDetailsFragment : Fragment(R.layout.fragment_playlist_details) {

    private val playlistId: Long by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getLong(ARG_PLAYLIST_ID)
    }
    private val viewModel: PlaylistDetailsViewModel by viewModel {
        parametersOf(playlistId)
    }

    private lateinit var coverImageView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var statsTextView: TextView
    private lateinit var shareButton: ImageButton
    private lateinit var menuButton: ImageButton
    private lateinit var overlayView: View
    private lateinit var tracksRecyclerView: RecyclerView
    private lateinit var emptyTracksTextView: TextView
    private lateinit var menuBottomSheet: LinearLayout
    private lateinit var menuCoverImageView: ImageView
    private lateinit var menuTitleTextView: TextView
    private lateinit var menuTracksCountTextView: TextView
    private lateinit var tracksAdapter: TrackAdapter
    private lateinit var menuBottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private var currentPlaylist: Playlist? = null
    private val coverPlaceholderPadding by lazy(LazyThreadSafetyMode.NONE) {
        resources.getDimensionPixelSize(R.dimen.playlist_detail_cover_placeholder_padding)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { target, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            target.updatePadding(top = systemBars.top)
            insets
        }

        setupViews(view)
        setupTrackBottomSheet(view)
        setupMenuBottomSheet()
        setupListeners(view)
        observeViewModel()
    }

    override fun onDestroyView() {
        tracksRecyclerView.adapter = null
        super.onDestroyView()
    }

    private fun setupViews(root: View) {
        coverImageView = root.findViewById(R.id.playlistCoverImageView)
        titleTextView = root.findViewById(R.id.playlistTitleTextView)
        descriptionTextView = root.findViewById(R.id.playlistDescriptionTextView)
        statsTextView = root.findViewById(R.id.playlistStatsTextView)
        shareButton = root.findViewById(R.id.shareButton)
        menuButton = root.findViewById(R.id.menuButton)
        overlayView = root.findViewById(R.id.overlayView)
        tracksRecyclerView = root.findViewById(R.id.tracksRecyclerView)
        emptyTracksTextView = root.findViewById(R.id.emptyTracksTextView)
        menuBottomSheet = root.findViewById(R.id.menuBottomSheet)
        menuCoverImageView = root.findViewById(R.id.menuPlaylistCoverImageView)
        menuTitleTextView = root.findViewById(R.id.menuPlaylistTitleTextView)
        menuTracksCountTextView = root.findViewById(R.id.menuPlaylistTracksCountTextView)

        tracksAdapter = TrackAdapter(
            tracks = emptyList(),
            onTrackClick = ::openPlayer,
            onTrackLongClick = ::showTrackDeleteDialog
        )
        tracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        tracksRecyclerView.adapter = tracksAdapter
    }

    private fun setupTrackBottomSheet(root: View) {
        BottomSheetBehavior.from<LinearLayout>(root.findViewById(R.id.tracksBottomSheet)).apply {
            state = BottomSheetBehavior.STATE_COLLAPSED
            isHideable = false
        }
    }

    private fun setupMenuBottomSheet() {
        menuBottomSheetBehavior = BottomSheetBehavior.from(menuBottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
            isHideable = true
        }

        menuBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
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

    private fun setupListeners(root: View) {
        root.findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            findNavController().navigateUp()
        }

        shareButton.setOnClickListener {
            viewModel.onShareClicked()
        }

        menuButton.setOnClickListener {
            showMenuBottomSheet()
        }

        overlayView.setOnClickListener {
            hideMenuBottomSheet()
        }

        root.findViewById<TextView>(R.id.menuShareTextView).setOnClickListener {
            hideMenuBottomSheet()
            viewModel.onShareClicked()
        }

        root.findViewById<TextView>(R.id.menuEditTextView).setOnClickListener {
            hideMenuBottomSheet()
            openPlaylistEditor()
        }

        root.findViewById<TextView>(R.id.menuDeleteTextView).setOnClickListener {
            hideMenuBottomSheet()
            showPlaylistDeleteDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            val playlist = state.playlist ?: return@observe
            currentPlaylist = playlist

            titleTextView.text = playlist.name
            descriptionTextView.text = playlist.description
            descriptionTextView.visibility = if (playlist.description.isBlank()) View.GONE else View.VISIBLE
            statsTextView.text = getString(
                R.string.playlist_details_stats_format,
                formatMinutes(state.totalMinutes),
                formatTrackCount(requireContext(), state.tracks.size)
            )

            renderCover(playlist.coverPath)
            bindMenuHeader(playlist)
            tracksAdapter.updateTracks(state.tracks)
            tracksRecyclerView.visibility = if (state.tracks.isEmpty()) View.GONE else View.VISIBLE
            emptyTracksTextView.visibility = if (state.tracks.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                null -> Unit
                PlaylistDetailsEvent.EmptyShare -> {
                    Toast.makeText(requireContext(), R.string.playlist_share_empty, Toast.LENGTH_SHORT).show()
                    viewModel.onEventHandled()
                }
                PlaylistDetailsEvent.PlaylistDeleted -> {
                    viewModel.onEventHandled()
                    findNavController().navigateUp()
                }
                is PlaylistDetailsEvent.SharePlaylist -> {
                    sharePlaylist(event.text)
                    viewModel.onEventHandled()
                }
            }
        }
    }

    private fun bindMenuHeader(playlist: Playlist) {
        menuTitleTextView.text = playlist.name
        menuTracksCountTextView.text = formatTrackCount(requireContext(), playlist.tracksCount)

        Glide.with(this)
            .load(playlist.coverPath?.takeIf { it.isNotBlank() }?.let(::File))
            .placeholder(R.drawable.vector)
            .error(R.drawable.vector)
            .centerCrop()
            .transform(
                RoundedCorners(
                    resources.getDimensionPixelSize(R.dimen.track_artwork_corner_radius)
                )
            )
            .into(menuCoverImageView)
    }

    private fun renderCover(coverPath: String?) {
        if (coverPath.isNullOrBlank()) {
            coverImageView.setPadding(
                coverPlaceholderPadding,
                coverPlaceholderPadding,
                coverPlaceholderPadding,
                coverPlaceholderPadding
            )
            coverImageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
            coverImageView.setImageResource(R.drawable.vector)
            return
        }

        coverImageView.setPadding(0, 0, 0, 0)
        coverImageView.scaleType = ImageView.ScaleType.CENTER_CROP
        Glide.with(this)
            .load(File(coverPath))
            .placeholder(R.drawable.vector)
            .error(R.drawable.vector)
            .centerCrop()
            .into(coverImageView)
    }

    private fun openPlayer(track: Track) {
        findNavController().navigate(
            R.id.action_playlistDetailsFragment_to_playerFragment,
            Bundle().apply {
                putSerializable(PlayerFragment.ARG_TRACK, track)
            }
        )
    }

    private fun openPlaylistEditor() {
        val playlist = currentPlaylist ?: return
        findNavController().navigate(
            R.id.action_playlistDetailsFragment_to_newPlaylistFragment,
            Bundle().apply {
                putLong(NewPlaylistFragment.ARG_PLAYLIST_ID, playlist.id)
                putBoolean(NewPlaylistFragment.ARG_EDIT_MODE, true)
            }
        )
    }

    private fun showTrackDeleteDialog(track: Track) {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.delete_track_message)
            .setNegativeButton(R.string.dialog_no, null)
            .setPositiveButton(R.string.dialog_yes) { _, _ ->
                viewModel.onTrackRemoveConfirmed(track)
            }
            .show()
    }

    private fun showPlaylistDeleteDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_playlist_title)
            .setMessage(R.string.delete_playlist_message)
            .setNegativeButton(R.string.dialog_no, null)
            .setPositiveButton(R.string.dialog_yes) { _, _ ->
                viewModel.onPlaylistDeleteConfirmed()
            }
            .show()
    }

    private fun sharePlaylist(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, null))
    }

    private fun showMenuBottomSheet() {
        menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun hideMenuBottomSheet() {
        menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun formatMinutes(minutes: Int): String {
        val mod100 = minutes % 100
        val mod10 = minutes % 10
        val suffix = when {
            mod100 in 11..14 -> "\u043c\u0438\u043d\u0443\u0442"
            mod10 == 1 -> "\u043c\u0438\u043d\u0443\u0442\u0430"
            mod10 in 2..4 -> "\u043c\u0438\u043d\u0443\u0442\u044b"
            else -> "\u043c\u0438\u043d\u0443\u0442"
        }
        return "$minutes $suffix"
    }

    companion object {
        const val ARG_PLAYLIST_ID = "playlistId"
    }
}
