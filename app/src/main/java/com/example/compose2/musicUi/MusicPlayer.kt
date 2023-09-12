package com.example.compose2.musicUi

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.example.compose2.R
import com.example.compose2.model.RepeatMode
import com.example.compose2.viewmodel.PlayerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MusicPlayer(modifier: Modifier=Modifier,
                playerViewModel: PlayerViewModel,
                onMinimize: () -> Unit,
                onPreviousClick: () -> Unit,
                onPlayPauseClick: () -> Unit,
                onNextClick: () -> Unit,
                onShuffleClick:()->Unit,
                onRepeatClick:()->Unit,
                onPositionChange:(Float)->Unit) {
    val context= LocalContext.current
    val isPlaying=playerViewModel.isPlaying
    val songTitle=playerViewModel.songName
    val songArtist=playerViewModel.songArtist
    val songImg by playerViewModel.artWorkLive.observeAsState()
    val songDuration=playerViewModel.songDuration
    val currentPos=playerViewModel.currentPosition
    val currentPosString=playerViewModel.currentDurationString
    val repeat=playerViewModel.repeat
    val shuffle=playerViewModel.shuffle
    var songArt by remember(songTitle) { mutableStateOf<Bitmap?>(null) }
    var mainColor by remember { mutableStateOf(Color.DarkGray) }
    var onMainColor by remember { mutableStateOf(Color.White) }
    val configuration= LocalConfiguration.current
    val screenWidth=configuration.screenWidthDp
    val imgHeight=configuration.screenHeightDp.times(8/14)
    val screenWidthPx= with(LocalDensity.current){screenWidth.dp.toPx()}.toInt()
    val imgHeightPx= with(LocalDensity.current){imgHeight.dp.toPx()}.toInt()
    LaunchedEffect(key1 = songTitle){
        withContext(Dispatchers.IO){
            if (songImg != null){
                songArt= decodeSampledBitmapFromByteArray(songImg!!,screenWidthPx,imgHeightPx)
                val palette= Palette.from(songArt!!).generate()
                mainColor=palette.dominantSwatch?.let { Color(it.rgb) } ?: Color.DarkGray
                onMainColor=palette.dominantSwatch?.let { Color(it.bodyTextColor) } ?: Color.White
            }else{
                songArt=BitmapFactory.decodeResource(context.resources,R.drawable.p32)
                mainColor=Color.DarkGray
                onMainColor=Color.White
            }
        }
    }

    Column(modifier = modifier
        .fillMaxSize()
        .background(color = mainColor)) {
        PlayerHeader(modifier =Modifier.weight(1f),
            onMinimize = onMinimize)
        PlayerCentre(modifier = Modifier.weight(8f),
            songTitle = songTitle,
            songArtist = songArtist,
            songArt = songArt,
            onMainColor = onMainColor,
            mainColor = mainColor)
        PlayerSlider(modifier = Modifier.weight(3f),
            currentPos = currentPosString,
            songDuration = songDuration,
            currentPosition = currentPos,
            onMainColor = onMainColor,
            onPositionChange = {onPositionChange(it)})
        PlayerControls(modifier = Modifier.weight(2f),
            isPlaying = isPlaying,
            repeatOn = repeat,
            shuffleOn = shuffle,
            onMainColor = onMainColor,
            onPreviousClick=onPreviousClick,
            onPlayPauseClick=onPlayPauseClick,
            onNextClick = onNextClick,
            onRepeatClick = onRepeatClick,
            onShuffleClick = onShuffleClick)
    }
}

@Composable
fun PlayerHeader(modifier: Modifier=Modifier,
                 onMinimize:()->Unit,
                 textColor:Color= Color.White) {
    Row(modifier = modifier
        .fillMaxWidth()
        .background(Color.DarkGray),
        verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onMinimize() }, modifier = Modifier.weight(1f)) {
            Icon(imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription =null,
                modifier = Modifier.size(65.dp),
                tint = textColor)
        }
        Text(text = "Now Playing",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 30.sp, modifier = Modifier.weight(6f),
            textAlign = TextAlign.Center, color = textColor)
        Box(modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerCentre(modifier: Modifier=Modifier,
                 songTitle:String="name",
                 songArtist:String="artist",
                 mainColor:Color= Color.DarkGray,
                 onMainColor: Color= Color.White,
                 songArt:Bitmap?=null) {
    val context= LocalContext.current
    Box(modifier = modifier
        .fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter) {
        AnimatedContent(targetState = songArt,
            modifier = Modifier.fillMaxSize(),
            transitionSpec ={ addAnimation()}){img->
            val request= ImageRequest.Builder(context)
                .data(img)
                .placeholder(R.drawable.p32)
                .error(R.drawable.p32)
                .fallback(R.drawable.p32)
                .build()
            AsyncImage(model = request,
                contentDescription =null,
                imageLoader = context.imageLoader,
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.White),
                contentScale = ContentScale.Crop)
        }
        Box(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.7f)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, mainColor)
                )
            ))
        Column(modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom) {
            Text(text = songTitle,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 22.sp,
                color = onMainColor,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 3.dp)
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        velocity = 30.dp,
                        initialDelayMillis = 0, delayMillis = 0
                    ))
            Text(text = songArtist, fontSize = 17.sp,
                style = MaterialTheme.typography.bodyMedium,
                color = onMainColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun PlayerSlider(modifier: Modifier=Modifier, currentPos:String,
                 songDuration:Float, currentPosition:Float,
                 onMainColor: Color= Color.White,
                 onPositionChange:(Float)->Unit) {
    var currentPosition2 by remember { mutableStateOf(0f) }
    Column(modifier = modifier
        .fillMaxWidth(),
        verticalArrangement = Arrangement.Bottom) {
        Slider(value = currentPosition,
            onValueChange = { currentPosition2=it},
            valueRange = 0f..songDuration, onValueChangeFinished = {onPositionChange(currentPosition2)},
            modifier = Modifier.padding(start = 6.dp, end = 6.dp),
            colors = SliderDefaults.colors(
                thumbColor = onMainColor,
                activeTrackColor = onMainColor,
                inactiveTrackColor = onMainColor.copy(0.3f)
            ))
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 5.dp, end = 5.dp),
            verticalAlignment = Alignment.CenterVertically, 
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = currentPos,
                style = MaterialTheme.typography.bodyMedium,
                color = onMainColor)
            Text(text = formatTime(songDuration.toLong()), style = MaterialTheme.typography.bodyMedium,
                color = onMainColor)
        }
    }
}

@SuppressLint("PrivateResource")
@Composable
fun PlayerControls(modifier: Modifier=Modifier,
                   isPlaying:Boolean=false,
                   repeatOn:RepeatMode=RepeatMode.REPEAT_OFF,
                   shuffleOn:Boolean=false,
                   onMainColor: Color= Color.White,
                   onPreviousClick: () -> Unit,
                   onPlayPauseClick: () -> Unit,
                   onNextClick: () -> Unit,
                   onShuffleClick:()->Unit,
                   onRepeatClick:()->Unit) {
    val playPauseIcon=if (isPlaying) R.drawable.baseline_pause_circle_filled_24 else R.drawable.baseline_play_circle_filled_24
    val isRepeatIcon=when(repeatOn){
        RepeatMode.REPEAT_OFF-> R.drawable.repeat_off_1
        RepeatMode.REPEAT_ALL-> androidx.media3.ui.R.drawable.exo_icon_repeat_all
        else-> androidx.media3.ui.R.drawable.exo_icon_repeat_one
    }
    val shuffleIcon=if (shuffleOn) R.drawable.baseline_shuffle_24 else R.drawable.noun_shuffle_off_82403
    Row(modifier = modifier
        .fillMaxWidth(),
        verticalAlignment = Alignment.Bottom) {
        IconButton(onClick = { onShuffleClick() },modifier = Modifier
            .fillMaxHeight()
            .weight(1f)) {
            Icon(painter = painterResource(id = shuffleIcon),
                contentDescription =null,
                modifier = Modifier.fillMaxSize(0.5f),
                tint = onMainColor)
        }
        IconButton(onClick = {onPreviousClick() },modifier = Modifier
            .fillMaxHeight()
            .weight(1f)) {
            Icon(painter = painterResource(id = R.drawable.baseline_skip_previous_24),
                contentDescription =null,
                modifier = Modifier.fillMaxSize(0.6f),
                tint = onMainColor)
        }
        IconButton(onClick = { onPlayPauseClick() }, modifier = Modifier
            .fillMaxHeight()
            .weight(1f)) {
            Icon(painter = painterResource(id = playPauseIcon),
                contentDescription =null,
                modifier = Modifier.fillMaxSize(),
                tint = onMainColor)
        }
        IconButton(onClick = { onNextClick() },modifier = Modifier
            .fillMaxHeight()
            .weight(1f)) {
            Icon(painter = painterResource(id = R.drawable.baseline_skip_next_24),
                contentDescription =null,
                modifier = Modifier.fillMaxSize(0.6f),
                tint = onMainColor)
        }
        IconButton(onClick = { onRepeatClick() },modifier = Modifier
            .fillMaxHeight()
            .weight(1f)) {
            Icon(painter = painterResource(id = isRepeatIcon),
                contentDescription =null,
                modifier = Modifier.fillMaxSize(0.5f),
                tint = onMainColor)
        }
    }
}

fun addAnimation(duration:Int=300): ContentTransform {
    return slideInHorizontally(animationSpec = tween(durationMillis = duration)){ fullHeight -> fullHeight  } + fadeIn(
        animationSpec = tween(durationMillis = duration)
    ) togetherWith  slideOutHorizontally(animationSpec = tween(durationMillis = duration)){ fullHeight -> -fullHeight  } + fadeOut(
        animationSpec = tween(durationMillis = duration)
    )
}
@Preview(showBackground = true)
@Composable
fun PlayerPreview() {

}