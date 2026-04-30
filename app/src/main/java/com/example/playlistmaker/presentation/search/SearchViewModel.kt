package com.example.playlistmaker.presentation.search

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.interactor.SearchHistoryInteractor
import com.example.playlistmaker.domain.interactor.TracksInteractor
import com.example.playlistmaker.domain.model.Track

class SearchViewModel(
    private val tracksInteractor: TracksInteractor,
    private val searchHistoryInteractor: SearchHistoryInteractor
) : ViewModel() {

    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable { executeSearch() }

    private val _uiState = MutableLiveData(SearchUiState())
    val uiState: LiveData<SearchUiState> = _uiState

    private var hasSearchFocus = false
    private var lastSearchQuery = ""

    fun onSearchTextChanged(text: String) {
        updateState {
            copy(
                searchText = text,
                isClearButtonVisible = text.isNotEmpty()
            )
        }

        handler.removeCallbacks(searchRunnable)

        if (text.isBlank()) {
            showHistoryOrIdle()
        } else {
            updateContentState(SearchContentState.Idle)
            handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
        }
    }

    fun onSearchSubmitted() {
        handler.removeCallbacks(searchRunnable)
        executeSearch()
    }

    fun onSearchFocusChanged(hasFocus: Boolean) {
        hasSearchFocus = hasFocus

        if (_uiState.value?.searchText.isNullOrBlank()) {
            showHistoryOrIdle()
        } else if (!hasFocus && _uiState.value?.contentState is SearchContentState.History) {
            updateContentState(SearchContentState.Idle)
        }
    }

    fun onClearClicked() {
        handler.removeCallbacks(searchRunnable)
        updateState {
            copy(
                searchText = "",
                isClearButtonVisible = false
            )
        }
        showHistoryOrIdle()
    }

    fun onRetryClicked() {
        val currentQuery = _uiState.value?.searchText?.trim().orEmpty()
        if (currentQuery.isNotEmpty()) {
            executeSearch(currentQuery)
        } else if (lastSearchQuery.isNotBlank()) {
            executeSearch(lastSearchQuery)
        }
    }

    fun onTrackClicked(track: Track) {
        searchHistoryInteractor.add(track)
        if (_uiState.value?.contentState is SearchContentState.History) {
            showHistoryOrIdle()
        }
    }

    fun onClearHistoryClicked() {
        searchHistoryInteractor.clear()
        showHistoryOrIdle()
    }

    fun refreshHistory() {
        if (_uiState.value?.searchText.isNullOrBlank()) {
            showHistoryOrIdle()
        }
    }

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacks(searchRunnable)
    }

    private fun executeSearch(query: String = _uiState.value?.searchText?.trim().orEmpty()) {
        if (query.isBlank()) {
            showHistoryOrIdle()
            return
        }

        lastSearchQuery = query
        updateContentState(SearchContentState.Loading)

        tracksInteractor.searchTracks(query, object : TracksInteractor.TracksConsumer {
            override fun consume(foundTracks: List<Track>?, errorMessage: String?) {
                if (query != _uiState.value?.searchText?.trim()) {
                    return
                }

                val contentState = when {
                    errorMessage != null -> SearchContentState.ConnectionError
                    foundTracks.isNullOrEmpty() -> SearchContentState.NothingFound
                    else -> SearchContentState.SearchResults(foundTracks)
                }

                updateContentState(contentState)
            }
        })
    }

    private fun showHistoryOrIdle() {
        val historyTracks = searchHistoryInteractor.read()
        val contentState = if (hasSearchFocus && historyTracks.isNotEmpty()) {
            SearchContentState.History(historyTracks)
        } else {
            SearchContentState.Idle
        }
        updateContentState(contentState)
    }

    private fun updateContentState(contentState: SearchContentState) {
        updateState { copy(contentState = contentState) }
    }

    private fun updateState(update: SearchUiState.() -> SearchUiState) {
        val currentState = _uiState.value ?: SearchUiState()
        _uiState.value = currentState.update()
    }

    private companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }
}
