package com.example.compose2.musicUi

import android.graphics.Bitmap
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.compose2.R
import com.example.compose2.viewmodel.PlayerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Composable
fun MiniPlayer(onMiniPlayerClick:()->Unit,
               playerViewModel: PlayerViewModel,
               onPreviousClick:()->Unit,
               onPlayPauseClick:()->Unit,
               onNextClick:()->Unit) {
    var songImage by remember(playerViewModel.songName) { mutableStateOf<Bitmap?>(null) }
    val context= LocalContext.current
    val isPlaying=playerViewModel.isPlaying
    val art by playerViewModel.artWorkLive.observeAsState()
    val imgHeightPx= with(LocalDensity.current){60.dp.toPx()}.toInt()
    val configuration= LocalConfiguration.current
    val imgWidth=configuration.screenWidthDp.times(3/19)
    val imgWidthPx= with(LocalDensity.current){imgWidth.dp.toPx()}.toInt()
    val playPauseIcon=if (isPlaying) R.drawable.baseline_pause_circle_filled_24 else R.drawable.baseline_play_circle_filled_24
    LaunchedEffect(key1 = playerViewModel.songName){
        withContext(Dispatchers.Default){
            if (art != null){
                songImage= decodeSampledBitmapFromByteArray(art!!,imgWidthPx,imgHeightPx)
            }
        }
    }
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(60.dp)
        .clip(RoundedCornerShape(6.dp))
        .background(color = MaterialTheme.colorScheme.surfaceVariant)
        .clickable { onMiniPlayerClick() },
        verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier
            .fillMaxHeight()
            .weight(3f)) {
            if (songImage != null){
                val request= ImageRequest.Builder(context)
                    .data(songImage)
                    .placeholder(R.drawable.p32)
                    .error(R.drawable.p32)
                    .fallback(R.drawable.p32)
                    .memoryCacheKey(playerViewModel.songName)
                    .diskCacheKey(playerViewModel.songName)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
                AsyncImage(model = request,
                    contentDescription =null,
                    imageLoader = context.imageLoader,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.White),
                    contentScale = ContentScale.FillWidth)
            }else{
                Image(painter = painterResource(id =R.drawable.p32),
                    contentDescription =null,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.White),
                    contentScale = ContentScale.FillWidth)
            }
        }
        Column(modifier = Modifier
            .fillMaxHeight()
            .weight(10f)
            .padding(top = 6.dp, start = 4.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(text = playerViewModel.songName,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis)
            Text(text = playerViewModel.songArtist,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        IconButton(onClick = { onPreviousClick() },
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .padding(2.dp)) {
            Icon(painter = painterResource(id = R.drawable.baseline_skip_previous_24),
                contentDescription ="previous",
                modifier = Modifier.size(55.dp),
                tint =MaterialTheme.colorScheme.primary)
        }
        IconButton(onClick = { onPlayPauseClick() },
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .padding(2.dp)) {
            Icon(painter = painterResource(id = playPauseIcon),
                contentDescription =if (isPlaying) "pause" else "play",
                modifier = Modifier.size(55.dp),
                tint =MaterialTheme.colorScheme.primary)
        }
        IconButton(onClick = { onNextClick() },
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .padding(2.dp)) {
            Icon(painter = painterResource(id = R.drawable.baseline_skip_next_24),
                contentDescription ="next",
                modifier = Modifier.size(55.dp),
                tint =MaterialTheme.colorScheme.primary)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MiniPlayerPreview() {

}