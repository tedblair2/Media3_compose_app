package com.github.tedblair2.muziki3.features.miniplayer.ui

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.github.tedblair2.muziki3.R
import com.github.tedblair2.muziki3.features.miniplayer.viewmodel.MiniPlayerEvents
import com.github.tedblair2.muziki3.features.miniplayer.viewmodel.MiniPlayerViewModel
import com.github.tedblair2.muziki3.features.player.ui.PlayerScreenState
import com.github.tedblair2.muziki3.helpers.loadBitmapFromByteArray

@Composable
fun MiniPlayerScreen(
    modifier: Modifier=Modifier,
    onNavigateToPlayer:()->Unit={}
) {
    val viewModel=hiltViewModel<MiniPlayerViewModel>()
    val playerState by viewModel.playerScreenState.collectAsStateWithLifecycle()

    AnimatedVisibility(visible=playerState.isMiniPlayerVisible) {
        MiniPlayerScreenContent(
            modifier=modifier
                .clickable { onNavigateToPlayer() },
            playerScreenState=playerState,
            onEvent=viewModel::onEvent
        )
    }
}

@Composable
fun MiniPlayerScreenContent(
    modifier: Modifier=Modifier,
    playerScreenState: PlayerScreenState,
    onEvent: (MiniPlayerEvents)->Unit
) {
    var songImage by remember(playerScreenState.songName) { mutableStateOf<Bitmap?>(null) }
    val context= LocalContext.current
    val imgHeightPx= with(LocalDensity.current){60.dp.toPx()}.toInt()
    val configuration= LocalConfiguration.current
    val imgWidth=configuration.screenWidthDp.times(3/19)
    val imgWidthPx= with(LocalDensity.current){imgWidth.dp.toPx()}.toInt()
    val playPauseIcon=if (playerScreenState.isPlaying) R.drawable.baseline_pause_circle_filled_24 else R.drawable.baseline_play_circle_filled_24
    
    LaunchedEffect(key1=playerScreenState.songName) {
        songImage=loadBitmapFromByteArray(playerScreenState.songImage,imgWidthPx,imgHeightPx,context)
        println("Visibility is ${playerScreenState.isMiniPlayerVisible}")
    }
    
    Column(
        modifier =modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier =Modifier
                .fillMaxWidth()
                .height(70.dp),
            verticalAlignment = Alignment.CenterVertically) {

            Box(modifier =Modifier
                .size(60.dp)
                .padding(5.dp)
                .clip(RoundedCornerShape(8.dp))
            ) {
                if (songImage != null){
                    val request= ImageRequest.Builder(context)
                        .data(songImage)
                        .placeholder(R.drawable.p32)
                        .error(R.drawable.p32)
                        .fallback(R.drawable.p32)
                        .memoryCacheKey(playerScreenState.songName)
                        .diskCacheKey(playerScreenState.songName)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .build()
                    AsyncImage(model = request,
                        contentDescription =null,
                        imageLoader = context.imageLoader,
                        modifier =Modifier
                            .fillMaxSize()
                            .background(color=Color.White),
                        contentScale = ContentScale.Crop)
                }else{
                    Image(painter = painterResource(id =R.drawable.p32) ,
                        contentDescription =null,
                        modifier =Modifier
                            .fillMaxSize()
                            .background(color=Color.White),
                        contentScale = ContentScale.Crop)
                }
            }

            Column(modifier =Modifier
                .fillMaxHeight()
                .weight(10f)
                .padding(start=7.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = playerScreenState.songName,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
                Text(text = playerScreenState.songArtist,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            IconButton(onClick = { onEvent(MiniPlayerEvents.OnPrevious) },
                modifier =Modifier
                    .weight(2f)
                    .fillMaxHeight()
                    .padding(2.dp)) {
                Icon(painter = painterResource(id = R.drawable.baseline_skip_previous_24) ,
                    contentDescription ="previous",
                    modifier = Modifier.size(55.dp),
                    tint =MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = { onEvent(MiniPlayerEvents.OnPlayPause) },
                modifier =Modifier
                    .weight(2f)
                    .fillMaxHeight()
                    .padding(2.dp)) {
                Icon(painter = painterResource(id = playPauseIcon) ,
                    contentDescription =if (playerScreenState.isPlaying) "pause" else "play",
                    modifier = Modifier.size(55.dp),
                    tint =MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = { onEvent(MiniPlayerEvents.OnNext) },
                modifier =Modifier
                    .weight(2f)
                    .fillMaxHeight()
                    .padding(2.dp)) {
                Icon(painter = painterResource(id = R.drawable.baseline_skip_next_24) ,
                    contentDescription ="next",
                    modifier = Modifier.size(55.dp),
                    tint =MaterialTheme.colorScheme.primary)
            }
        }

        HorizontalDivider(
            modifier =Modifier
                .fillMaxWidth(),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
    }
}