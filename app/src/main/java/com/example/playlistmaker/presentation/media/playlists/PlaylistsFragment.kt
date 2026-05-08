package com.example.playlistmaker.presentation.media.playlists

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.playlist.NewPlaylistFragment
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

        playlistsAdapter = PlaylistGridAdapter(emptyList())
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
                Toast.makeText(
                    requireContext(),
                    getString(R.string.playlist_created_message, playlistName),
                    Toast.LENGTH_SHORT
                ).show()
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

    private companion object {
        private const val SPAN_COUNT = 2
    }
}
