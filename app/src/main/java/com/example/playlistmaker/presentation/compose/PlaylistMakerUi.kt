package com.example.playlistmaker.presentation.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.Track

val PlaylistMakerDisplayFontFamily = FontFamily(
    Font(R.font.ys_display_regular, FontWeight.Normal),
    Font(R.font.ys_display_medium, FontWeight.Medium),
    Font(R.font.ys_display_bold, FontWeight.Bold)
)

val PlaylistMakerTextFontFamily = FontFamily(
    Font(R.font.ys_text_regular, FontWeight.Normal),
    Font(R.font.ys_text_medium, FontWeight.Medium),
    Font(R.font.ys_text_bold, FontWeight.Bold)
)

@Composable
fun ScreenTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = colorResource(R.color.track_name_color),
        fontFamily = PlaylistMakerDisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    )
}

@Composable
fun PrimaryPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(R.color.media_primary_button_bg),
            contentColor = colorResource(R.color.media_primary_button_text)
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = null,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontFamily = PlaylistMakerDisplayFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun RetryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(R.color.player_play_button_color),
            contentColor = colorResource(R.color.retry_button_text_color)
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = null
    ) {
        Text(
            text = text,
            fontFamily = PlaylistMakerDisplayFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 19.sp
        )
    }
}

@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    subtext: String? = null,
    imageRes: Int = R.drawable.nothing_found,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            color = colorResource(R.color.track_name_color),
            fontFamily = PlaylistMakerDisplayFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 19.sp,
            textAlign = TextAlign.Center
        )
        if (subtext != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = subtext,
                color = colorResource(R.color.track_name_color),
                fontFamily = PlaylistMakerDisplayFontFamily,
                fontSize = 19.sp,
                textAlign = TextAlign.Center
            )
        }
        if (action != null) {
            Spacer(Modifier.height(16.dp))
            action()
        }
    }
}

@Composable
fun TrackListItem(
    track: Track,
    onClick: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(61.dp)
            .clickable { onClick(track) }
            .padding(horizontal = 13.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = track.artworkUrl100,
            contentDescription = stringResource(R.string.track_cover),
            placeholder = painterResource(R.drawable.placeholder),
            error = painterResource(R.drawable.placeholder),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(45.dp)
                .clip(RoundedCornerShape(dimensionResource(R.dimen.track_artwork_corner_radius)))
        )
        Spacer(Modifier.width(8.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = track.trackName,
                color = colorResource(R.color.track_name_color),
                fontFamily = PlaylistMakerDisplayFontFamily,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = context.getString(
                    R.string.track_subtitle_format,
                    track.artistName,
                    track.trackTime
                ),
                color = colorResource(R.color.track_secondary_color),
                fontFamily = PlaylistMakerDisplayFontFamily,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        Image(
            painter = painterResource(R.drawable.strelka_2),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}
