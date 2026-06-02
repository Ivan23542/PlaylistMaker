package com.example.playlistmaker.presentation.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.player.PlayerFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private val viewModel: SearchViewModel by viewModel()

    private var clickDebounceJob: Job? = null
    private var isClickAllowed = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val state by viewModel.uiState.observeAsState(SearchUiState())

                SearchScreen(
                    state = state,
                    onSearchTextChanged = viewModel::onSearchTextChanged,
                    onSearchSubmitted = viewModel::onSearchSubmitted,
                    onSearchFocusChanged = viewModel::onSearchFocusChanged,
                    onClearClicked = viewModel::onClearClicked,
                    onRetryClicked = viewModel::onRetryClicked,
                    onClearHistoryClicked = viewModel::onClearHistoryClicked,
                    onTrackClicked = ::handleTrackClick
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshHistory()
    }

    override fun onDestroyView() {
        clickDebounceJob?.cancel()
        clickDebounceJob = null
        isClickAllowed = true
        super.onDestroyView()
    }

    private fun handleTrackClick(track: Track) {
        if (clickDebounce()) {
            viewModel.onTrackClicked(track)
            openPlayer(track)
        }
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

    private fun openPlayer(track: Track) {
        findNavController().navigate(
            R.id.action_searchFragment_to_playerFragment,
            bundleOf(PlayerFragment.ARG_TRACK to track)
        )
    }

    private companion object {
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}
