package com.example.playlistmaker.presentation.media

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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.media.favorites.FavoriteTracksState
import com.example.playlistmaker.presentation.media.favorites.FavoriteTracksViewModel
import com.example.playlistmaker.presentation.media.playlists.PlaylistDetailsFragment
import com.example.playlistmaker.presentation.media.playlists.PlaylistsState
import com.example.playlistmaker.presentation.media.playlists.PlaylistsViewModel
import com.example.playlistmaker.presentation.player.PlayerFragment
import com.example.playlistmaker.presentation.playlist.NewPlaylistFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediatekaFragment : Fragment() {

    private val viewModel: MediatekaViewModel by viewModel()
    private val favoriteTracksViewModel: FavoriteTracksViewModel by viewModel()
    private val playlistsViewModel: PlaylistsViewModel by viewModel()
    private val playlistCreatedMessage = MutableLiveData<String?>(null)

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
                viewModel
                val favoriteTracksState by favoriteTracksViewModel.state.observeAsState(
                    FavoriteTracksState.Empty
                )
                val playlistsState by playlistsViewModel.state.observeAsState(PlaylistsState.Empty)
                val snackbarMessage by playlistCreatedMessage.observeAsState()

                MediatekaScreen(
                    favoriteTracksState = favoriteTracksState,
                    playlistsState = playlistsState,
                    playlistCreatedMessage = snackbarMessage,
                    onPlaylistCreatedMessageShown = { playlistCreatedMessage.value = null },
                    onTrackClicked = ::handleTrackClick,
                    onNewPlaylistClicked = {
                        findNavController().navigate(
                            R.id.action_mediatekaFragment_to_newPlaylistFragment
                        )
                    },
                    onPlaylistClicked = ::openPlaylistDetails
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<String>(NewPlaylistFragment.RESULT_PLAYLIST_CREATED)
            ?.observe(viewLifecycleOwner) { playlistName ->
                playlistCreatedMessage.value = getString(
                    R.string.playlist_created_message,
                    playlistName
                )
                findNavController().currentBackStackEntry
                    ?.savedStateHandle
                    ?.remove<String>(NewPlaylistFragment.RESULT_PLAYLIST_CREATED)
            }
    }

    override fun onDestroyView() {
        clickDebounceJob?.cancel()
        clickDebounceJob = null
        isClickAllowed = true
        super.onDestroyView()
    }

    private fun handleTrackClick(track: Track) {
        if (clickDebounce()) {
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
            R.id.action_mediatekaFragment_to_playerFragment,
            bundleOf(PlayerFragment.ARG_TRACK to track)
        )
    }

    private fun openPlaylistDetails(playlist: Playlist) {
        findNavController().navigate(
            R.id.action_mediatekaFragment_to_playlistDetailsFragment,
            Bundle().apply {
                putLong(PlaylistDetailsFragment.ARG_PLAYLIST_ID, playlist.id)
            }
        )
    }

    private companion object {
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}
