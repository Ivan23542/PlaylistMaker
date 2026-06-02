package com.example.playlistmaker.presentation.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.compose.EmptyState
import com.example.playlistmaker.presentation.compose.PlaylistMakerDisplayFontFamily
import com.example.playlistmaker.presentation.compose.PlaylistMakerTextFontFamily
import com.example.playlistmaker.presentation.compose.RetryButton
import com.example.playlistmaker.presentation.compose.ScreenTitle
import com.example.playlistmaker.presentation.compose.TrackListItem

@Composable
fun SearchScreen(
    state: SearchUiState,
    onSearchTextChanged: (String) -> Unit,
    onSearchSubmitted: () -> Unit,
    onSearchFocusChanged: (Boolean) -> Unit,
    onClearClicked: () -> Unit,
    onRetryClicked: () -> Unit,
    onClearHistoryClicked: () -> Unit,
    onTrackClicked: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.screen_background))
            .statusBarsPadding()
    ) {
        Spacer(Modifier.height(14.dp))
        ScreenTitle(text = stringResource(R.string.search))
        SearchField(
            text = state.searchText,
            isClearButtonVisible = state.isClearButtonVisible,
            onTextChanged = onSearchTextChanged,
            onSubmitted = onSearchSubmitted,
            onFocusChanged = onSearchFocusChanged,
            onClearClicked = onClearClicked,
            modifier = Modifier
                .padding(start = 16.dp, top = 24.dp, end = 16.dp)
                .fillMaxWidth()
        )
        SearchContent(
            contentState = state.contentState,
            onRetryClicked = onRetryClicked,
            onClearHistoryClicked = onClearHistoryClicked,
            onTrackClicked = onTrackClicked,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SearchField(
    text: String,
    isClearButtonVisible: Boolean,
    onTextChanged: (String) -> Unit,
    onSubmitted: () -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    onClearClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = modifier
            .height(36.dp)
            .background(colorResource(R.color.YP_Light_Gray), RoundedCornerShape(8.dp))
            .clickable {
                focusRequester.requestFocus()
                keyboardController?.show()
            }
    ) {
        Image(
            painter = painterResource(R.drawable.lupa_poisk),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 13.dp)
                .size(16.dp)
        )
        BasicTextField(
            value = text,
            onValueChange = onTextChanged,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .padding(start = 36.dp, end = 40.dp)
                .focusRequester(focusRequester)
                .onFocusChanged { onFocusChanged(it.isFocused) },
            singleLine = true,
            textStyle = TextStyle(
                color = colorResource(R.color.black),
                fontFamily = PlaylistMakerTextFontFamily,
                fontSize = 16.sp
            ),
            cursorBrush = SolidColor(colorResource(R.color.black)),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSubmitted()
                    keyboardController?.hide()
                }
            ),
            decorationBox = { innerTextField ->
                if (text.isEmpty()) {
                    Text(
                        text = stringResource(R.string.search_poisk),
                        color = colorResource(R.color.YP_Text_Gray),
                        fontFamily = PlaylistMakerTextFontFamily,
                        fontSize = 16.sp
                    )
                }
                innerTextField()
            }
        )
        if (isClearButtonVisible) {
            Image(
                painter = painterResource(R.drawable.crest),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable {
                        onClearClicked()
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                    .padding(horizontal = 12.dp)
                    .size(24.dp)
            )
        }
    }
}

@Composable
private fun SearchContent(
    contentState: SearchContentState,
    onRetryClicked: () -> Unit,
    onClearHistoryClicked: () -> Unit,
    onTrackClicked: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        when (contentState) {
            SearchContentState.Idle -> Unit
            SearchContentState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(44.dp),
                    color = colorResource(R.color.blue)
                )
            }
            SearchContentState.NothingFound -> {
                EmptyState(
                    message = stringResource(R.string.nothing_found),
                    modifier = Modifier.fillMaxSize()
                )
            }
            SearchContentState.ConnectionError -> {
                EmptyState(
                    message = stringResource(R.string.connection_error),
                    subtext = stringResource(R.string.connection_error_message),
                    imageRes = R.drawable.connection_error,
                    modifier = Modifier.fillMaxSize()
                ) {
                    RetryButton(
                        text = stringResource(R.string.refresh),
                        onClick = onRetryClicked
                    )
                }
            }
            is SearchContentState.SearchResults -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp)
                ) {
                    items(
                        items = contentState.tracks,
                        key = { it.trackId }
                    ) { track ->
                        TrackListItem(track = track, onClick = onTrackClicked)
                    }
                }
            }
            is SearchContentState.History -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.history_title),
                        modifier = Modifier.padding(top = 24.dp),
                        color = colorResource(R.color.track_name_color),
                        fontFamily = PlaylistMakerDisplayFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        textAlign = TextAlign.Center
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 16.dp)
                    ) {
                        items(
                            items = contentState.tracks,
                            key = { it.trackId }
                        ) { track ->
                            TrackListItem(track = track, onClick = onTrackClicked)
                        }
                    }
                    RetryButton(
                        text = stringResource(R.string.clear_history),
                        onClick = onClearHistoryClicked,
                        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                    )
                }
            }
        }
    }
}
