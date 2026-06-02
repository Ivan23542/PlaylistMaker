package com.example.playlistmaker.presentation.media

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.compose.EmptyState
import com.example.playlistmaker.presentation.compose.PlaylistMakerDisplayFontFamily
import com.example.playlistmaker.presentation.compose.PrimaryPillButton
import com.example.playlistmaker.presentation.compose.ScreenTitle
import com.example.playlistmaker.presentation.compose.TrackListItem
import com.example.playlistmaker.presentation.media.favorites.FavoriteTracksState
import com.example.playlistmaker.presentation.media.playlists.PlaylistsState
import com.example.playlistmaker.presentation.media.playlists.formatTrackCount
import java.io.File

@Composable
fun MediatekaScreen(
    favoriteTracksState: FavoriteTracksState,
    playlistsState: PlaylistsState,
    playlistCreatedMessage: String?,
    onPlaylistCreatedMessageShown: () -> Unit,
    onTrackClicked: (Track) -> Unit,
    onNewPlaylistClicked: () -> Unit,
    onPlaylistClicked: (Playlist) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(playlistCreatedMessage) {
        if (playlistCreatedMessage != null) {
            snackbarHostState.showSnackbar(playlistCreatedMessage)
            onPlaylistCreatedMessageShown()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.screen_background)),
        containerColor = colorResource(R.color.screen_background),
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = colorResource(R.color.snackbar_bg),
                    contentColor = colorResource(R.color.snackbar_text)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
                .background(colorResource(R.color.screen_background))
        ) {
            Spacer(Modifier.height(14.dp))
            ScreenTitle(text = stringResource(R.string.mediateka))
            Spacer(Modifier.height(24.dp))
            MediaTabs(
                favoriteTracksState = favoriteTracksState,
                playlistsState = playlistsState,
                onTrackClicked = onTrackClicked,
                onNewPlaylistClicked = onNewPlaylistClicked,
                onPlaylistClicked = onPlaylistClicked,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MediaTabs(
    favoriteTracksState: FavoriteTracksState,
    playlistsState: PlaylistsState,
    onTrackClicked: (Track) -> Unit,
    onNewPlaylistClicked: () -> Unit,
    onPlaylistClicked: (Playlist) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(FAVORITES_TAB_INDEX) }
    val tabs = listOf(
        stringResource(R.string.favorite_tracks_tab),
        stringResource(R.string.playlists_tab)
    )

    Column(modifier = modifier.fillMaxWidth()) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = colorResource(R.color.screen_background),
            contentColor = colorResource(R.color.media_tab_text_color),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    height = 2.dp,
                    color = colorResource(R.color.media_tab_indicator_color)
                )
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontFamily = PlaylistMakerDisplayFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                    }
                )
            }
        }

        when (selectedTabIndex) {
            FAVORITES_TAB_INDEX -> FavoriteTracksContent(
                state = favoriteTracksState,
                onTrackClicked = onTrackClicked,
                modifier = Modifier.weight(1f)
            )
            PLAYLISTS_TAB_INDEX -> PlaylistsContent(
                state = playlistsState,
                onNewPlaylistClicked = onNewPlaylistClicked,
                onPlaylistClicked = onPlaylistClicked,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FavoriteTracksContent(
    state: FavoriteTracksState,
    onTrackClicked: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        when (state) {
            FavoriteTracksState.Empty -> EmptyState(
                message = stringResource(R.string.empty_favorite_tracks),
                modifier = Modifier.fillMaxSize()
            )
            is FavoriteTracksState.Content -> LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = state.tracks,
                    key = { it.trackId }
                ) { track ->
                    TrackListItem(track = track, onClick = onTrackClicked)
                }
            }
        }
    }
}

@Composable
private fun PlaylistsContent(
    state: PlaylistsState,
    onNewPlaylistClicked: () -> Unit,
    onPlaylistClicked: (Playlist) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 24.dp, end = 16.dp),
    ) {
        PrimaryPillButton(
            text = stringResource(R.string.new_playlist),
            onClick = onNewPlaylistClicked,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 24.dp)
        ) {
            when (state) {
                PlaylistsState.Empty -> EmptyState(
                    message = stringResource(R.string.empty_playlists),
                    modifier = Modifier.fillMaxSize()
                )
                is PlaylistsState.Content -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = state.playlists,
                        key = { it.id }
                    ) { playlist ->
                        PlaylistGridItem(
                            playlist = playlist,
                            onClick = onPlaylistClicked
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistGridItem(
    playlist: Playlist,
    onClick: (Playlist) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(playlist) }
    ) {
        AsyncImage(
            model = playlist.coverPath?.takeIf { it.isNotBlank() }?.let(::File),
            contentDescription = stringResource(R.string.playlist_cover),
            placeholder = painterResource(R.drawable.vector),
            error = painterResource(R.drawable.vector),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(dimensionResource(R.dimen.track_artwork_corner_radius)))
        )
        Text(
            text = playlist.name,
            modifier = Modifier.padding(top = 4.dp),
            color = colorResource(R.color.track_name_color),
            fontFamily = PlaylistMakerDisplayFontFamily,
            fontSize = 14.sp,
            lineHeight = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = formatTrackCount(context, playlist.tracksCount),
            color = colorResource(R.color.track_name_color),
            fontFamily = PlaylistMakerDisplayFontFamily,
            fontSize = 14.sp,
            lineHeight = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private const val FAVORITES_TAB_INDEX = 0
private const val PLAYLISTS_TAB_INDEX = 1
