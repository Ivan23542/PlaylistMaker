package com.example.playlistmaker.presentation.search

import com.example.playlistmaker.domain.model.Track

data class SearchUiState(
    val searchText: String = "",
    val isClearButtonVisible: Boolean = false,
    val contentState: SearchContentState = SearchContentState.Idle
)

sealed interface SearchContentState {
    data object Idle : SearchContentState
    data object Loading : SearchContentState
    data object NothingFound : SearchContentState
    data object ConnectionError : SearchContentState
    data class SearchResults(val tracks: List<Track>) : SearchContentState
    data class History(val tracks: List<Track>) : SearchContentState
}
