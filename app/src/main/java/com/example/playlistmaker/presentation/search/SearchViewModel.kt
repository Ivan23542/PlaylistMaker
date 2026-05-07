package com.example.playlistmaker.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactor.FavoriteTracksInteractor
import com.example.playlistmaker.domain.interactor.SearchHistoryInteractor
import com.example.playlistmaker.domain.interactor.TracksInteractor
import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SearchViewModel(
    private val tracksInteractor: TracksInteractor,
    private val searchHistoryInteractor: SearchHistoryInteractor,
    private val favoriteTracksInteractor: FavoriteTracksInteractor
) : ViewModel() {

    private val _uiState = MutableLiveData(SearchUiState())
    val uiState: LiveData<SearchUiState> = _uiState

    private var hasSearchFocus = false
    private var lastSearchQuery = ""
    private var searchDebounceJob: Job? = null
    private var searchJob: Job? = null
    private var historyJob: Job? = null

    fun onSearchTextChanged(text: String) {
        updateState {
            copy(
                searchText = text,
                isClearButtonVisible = text.isNotEmpty()
            )
        }

        searchDebounceJob?.cancel()

        if (text.isBlank()) {
            searchJob?.cancel()
            showHistoryOrIdle()
        } else {
            updateContentState(SearchContentState.Idle)
            searchDebounceJob = viewModelScope.launch {
                delay(SEARCH_DEBOUNCE_DELAY)
                executeSearch()
            }
        }
    }

    fun onSearchSubmitted() {
        searchDebounceJob?.cancel()
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
        searchDebounceJob?.cancel()
        searchJob?.cancel()
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
        when {
            currentQuery.isNotEmpty() -> executeSearch(currentQuery)
            lastSearchQuery.isNotBlank() -> executeSearch(lastSearchQuery)
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

    private fun executeSearch(query: String = _uiState.value?.searchText?.trim().orEmpty()) {
        if (query.isBlank()) {
            showHistoryOrIdle()
            return
        }

        lastSearchQuery = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            updateContentState(SearchContentState.Loading)

            tracksInteractor.searchTracks(query)
                .catch {
                    if (query == _uiState.value?.searchText?.trim()) {
                        updateContentState(SearchContentState.ConnectionError)
                    }
                }
                .collect { foundTracks ->
                    if (query != _uiState.value?.searchText?.trim()) return@collect

                    val contentState = if (foundTracks.isEmpty()) {
                        SearchContentState.NothingFound
                    } else {
                        SearchContentState.SearchResults(foundTracks)
                    }

                    updateContentState(contentState)
                }
        }
    }

    private fun showHistoryOrIdle() {
        historyJob?.cancel()
        historyJob = viewModelScope.launch {
            val historyTracks = searchHistoryInteractor.read()
            val favoriteTrackIds = favoriteTracksInteractor.getFavoriteTracks()
                .first()
                .map { it.trackId }
                .toHashSet()

            historyTracks.forEach { track ->
                track.isFavorite = track.trackId in favoriteTrackIds
            }

            val shouldShowHistory =
                hasSearchFocus && _uiState.value?.searchText.isNullOrBlank() == true &&
                    historyTracks.isNotEmpty()

            val contentState = if (shouldShowHistory) {
                SearchContentState.History(historyTracks)
            } else {
                SearchContentState.Idle
            }
            updateContentState(contentState)
        }
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
