package com.example.playlistmaker.presentation.media.playlists

import android.os.Bundle
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.playlist.NewPlaylistFragment
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment(R.layout.fragment_playlists) {

    private val viewModel: PlaylistsViewModel by viewModel()

    private lateinit var createPlaylistButton: View
    private lateinit var playlistsRecyclerView: RecyclerView
    private lateinit var emptyStateContainer: View
    private lateinit var playlistsAdapter: PlaylistGridAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createPlaylistButton = view.findViewById(R.id.createPlaylistButton)
        playlistsRecyclerView = view.findViewById(R.id.playlistsRecyclerView)
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer)

        playlistsAdapter = PlaylistGridAdapter(emptyList()) { playlist ->
            findNavController().navigate(
                R.id.action_mediatekaFragment_to_playlistDetailsFragment,
                Bundle().apply {
                    putLong(PlaylistDetailsFragment.ARG_PLAYLIST_ID, playlist.id)
                }
            )
        }
        playlistsRecyclerView.layoutManager = GridLayoutManager(requireContext(), SPAN_COUNT)
        playlistsRecyclerView.adapter = playlistsAdapter
        playlistsRecyclerView.addItemDecoration(
            PlaylistsGridSpacingDecoration(
                spanCount = SPAN_COUNT,
                spacing = resources.getDimensionPixelSize(R.dimen.playlists_grid_spacing)
            )
        )

        createPlaylistButton.setOnClickListener {
            findNavController().navigate(R.id.action_mediatekaFragment_to_newPlaylistFragment)
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                PlaylistsState.Empty -> showEmptyState()
                is PlaylistsState.Content -> showPlaylists(state.playlists)
            }
        }

        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<String>(NewPlaylistFragment.RESULT_PLAYLIST_CREATED)
            ?.observe(viewLifecycleOwner) { playlistName ->
                showPlaylistCreatedSnackbar(playlistName)
                findNavController().currentBackStackEntry
                    ?.savedStateHandle
                    ?.remove<String>(NewPlaylistFragment.RESULT_PLAYLIST_CREATED)
            }
    }

    override fun onDestroyView() {
        playlistsRecyclerView.adapter = null
        super.onDestroyView()
    }

    private fun showEmptyState() {
        emptyStateContainer.visibility = View.VISIBLE
        playlistsRecyclerView.visibility = View.GONE
        playlistsAdapter.updatePlaylists(emptyList())
    }

    private fun showPlaylists(playlists: List<com.example.playlistmaker.domain.model.Playlist>) {
        emptyStateContainer.visibility = View.GONE
        playlistsRecyclerView.visibility = View.VISIBLE
        playlistsAdapter.updatePlaylists(playlists)
    }

    private fun showPlaylistCreatedSnackbar(playlistName: String) {
        val snackbar = Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            getString(R.string.playlist_created_message, playlistName),
            Snackbar.LENGTH_SHORT
        )

        val snackbarView = snackbar.view
        val snackbarHeight = resources.getDimensionPixelSize(R.dimen.playlist_created_snackbar_height)
        val background = GradientDrawable().apply {
            setColor(ContextCompat.getColor(requireContext(), R.color.snackbar_bg))
            cornerRadius = 0f
        }

        snackbarView.background = background
        snackbarView.minimumHeight = snackbarHeight
        snackbarView.setPadding(0, 0, 0, 0)

        val textView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.gravity = Gravity.CENTER
        textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        textView.maxLines = 1
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.snackbar_text))
        textView.textSize = 14f
        textView.typeface = ResourcesCompat.getFont(requireContext(), R.font.ys_display_regular)

        (textView.parent as? ViewGroup)?.apply {
            minimumHeight = snackbarHeight
            setPadding(
                resources.getDimensionPixelSize(R.dimen.playlist_created_snackbar_padding_start),
                0,
                resources.getDimensionPixelSize(R.dimen.playlist_created_snackbar_padding_end),
                0
            )
        }

        snackbar.show()
        snackbarView.post {
            val parentWidth = (snackbarView.parent as? View)?.width ?: resources.displayMetrics.widthPixels
            val marginStart = resources.getDimensionPixelSize(R.dimen.playlist_created_snackbar_margin_start)
            val marginEnd = resources.getDimensionPixelSize(R.dimen.playlist_created_snackbar_margin_end)
            val marginBottom = resources.getDimensionPixelSize(R.dimen.playlist_created_snackbar_margin_bottom)

            (snackbarView.layoutParams as? ViewGroup.MarginLayoutParams)?.let { params ->
                params.width = parentWidth - marginStart - marginEnd
                params.height = snackbarHeight
                params.setMargins(marginStart, params.topMargin, marginEnd, marginBottom)
                snackbarView.layoutParams = params
            }
        }
    }

    private companion object {
        private const val SPAN_COUNT = 2
    }
}
