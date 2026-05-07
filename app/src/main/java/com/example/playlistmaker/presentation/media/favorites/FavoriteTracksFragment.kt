package com.example.playlistmaker.presentation.media.favorites

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.player.PlayerFragment
import com.example.playlistmaker.presentation.search.TrackAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoriteTracksFragment : Fragment(R.layout.fragment_favorite_tracks) {

    private val viewModel: FavoriteTracksViewModel by viewModel()

    private lateinit var favoriteTracksRecyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var emptyStateContainer: View

    private var clickDebounceJob: Job? = null
    private var isClickAllowed = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoriteTracksRecyclerView = view.findViewById(R.id.favoriteTracksRecyclerView)
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer)

        trackAdapter = TrackAdapter(emptyList()) { track ->
            if (clickDebounce()) {
                openPlayer(track)
            }
        }

        favoriteTracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        favoriteTracksRecyclerView.adapter = trackAdapter

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                FavoriteTracksState.Empty -> showEmptyState()
                is FavoriteTracksState.Content -> showTracks(state.tracks)
            }
        }
    }

    override fun onDestroyView() {
        clickDebounceJob?.cancel()
        clickDebounceJob = null
        isClickAllowed = true
        favoriteTracksRecyclerView.adapter = null
        super.onDestroyView()
    }

    private fun clickDebounce(): Boolean {
        if (!isClickAllowed) return false

        isClickAllowed = false
        clickDebounceJob?.cancel()
        clickDebounceJob = lifecycleScope.launch {
            delay(CLICK_DEBOUNCE_DELAY)
            isClickAllowed = true
        }
        return true
    }

    private fun showEmptyState() {
        emptyStateContainer.visibility = View.VISIBLE
        favoriteTracksRecyclerView.visibility = View.GONE
        trackAdapter.updateTracks(emptyList())
    }

    private fun showTracks(tracks: List<Track>) {
        emptyStateContainer.visibility = View.GONE
        favoriteTracksRecyclerView.visibility = View.VISIBLE
        trackAdapter.updateTracks(tracks)
    }

    private fun openPlayer(track: Track) {
        findNavController().navigate(
            R.id.action_mediatekaFragment_to_playerFragment,
            bundleOf(PlayerFragment.ARG_TRACK to track)
        )
    }

    private companion object {
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}
