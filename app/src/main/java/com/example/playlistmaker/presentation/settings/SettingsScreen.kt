package com.example.playlistmaker.presentation.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.compose.PlaylistMakerDisplayFontFamily
import com.example.playlistmaker.presentation.compose.ScreenTitle

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onThemeCheckedChanged: (Boolean) -> Unit,
    onShareClicked: () -> Unit,
    onSupportClicked: () -> Unit,
    onAgreementClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.screen_background))
            .statusBarsPadding()
    ) {
        Spacer(Modifier.height(14.dp))
        ScreenTitle(text = stringResource(R.string.button_3))
        SettingsThemeRow(
            isDarkTheme = state.isDarkTheme,
            onThemeCheckedChanged = onThemeCheckedChanged,
            modifier = Modifier.padding(top = 32.dp)
        )
        SettingsActionRow(
            text = stringResource(R.string.Share_the_app),
            iconRes = R.drawable.share,
            onClick = onShareClicked,
            modifier = Modifier.padding(top = 12.dp)
        )
        SettingsActionRow(
            text = stringResource(R.string.support),
            iconRes = R.drawable.support,
            onClick = onSupportClicked
        )
        SettingsActionRow(
            text = stringResource(R.string.Agreement),
            iconRes = R.drawable.strelka_2,
            onClick = onAgreementClicked
        )
    }
}

@Composable
private fun SettingsThemeRow(
    isDarkTheme: Boolean,
    onThemeCheckedChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(62.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.tema),
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            color = colorResource(R.color.track_name_color),
            fontFamily = PlaylistMakerDisplayFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp
        )
        Switch(
            checked = isDarkTheme,
            onCheckedChange = onThemeCheckedChanged,
            modifier = Modifier.padding(end = 15.dp)
        )
    }
}

@Composable
private fun SettingsActionRow(
    text: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(62.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            color = colorResource(R.color.track_name_color),
            fontFamily = PlaylistMakerDisplayFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp
        )
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            colorFilter = ColorFilter.tint(colorResource(R.color.player_small_icon_color)),
            modifier = Modifier
                .padding(end = 15.dp)
                .size(24.dp)
        )
    }
}
