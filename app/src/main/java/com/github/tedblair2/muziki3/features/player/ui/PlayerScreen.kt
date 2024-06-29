package com.github.tedblair2.muziki3.features.player.ui

import android.app.Activity
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.github.tedblair2.muziki3.R
import com.github.tedblair2.muziki3.features.player.viewmodel.PlayerScreenEvents
import com.github.tedblair2.muziki3.helpers.formatTime
import com.github.tedblair2.muziki3.helpers.loadBitmapFromByteArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit = {},
    playerScreenState: PlayerScreenState,
    onEvent:(PlayerScreenEvents)->Unit={}
) {
    val view=LocalView.current
    val window=(view.context as Activity).window
    var songArt by remember { mutableStateOf<Bitmap?>(null) }
    var mainColor by remember { mutableStateOf(Color.DarkGray) }
    var onMainColor by remember { mutableStateOf(Color.White) }
    val configuration= LocalConfiguration.current
    val screenWidth=configuration.screenWidthDp
    val imgHeight=configuration.screenHeightDp.times(8/14)
    val screenWidthPx= with(LocalDensity.current){screenWidth.dp.toPx()}.toInt()
    val imgHeightPx= with(LocalDensity.current){imgHeight.dp.toPx()}.toInt()
    val context=LocalContext.current

    if (!view.isInEditMode){
        SideEffect {
            window.statusBarColor=Color.DarkGray.toArgb()
            window.navigationBarColor=Color.DarkGray.toArgb()
            WindowCompat.getInsetsController(window,view).isAppearanceLightStatusBars=false
            WindowCompat.getInsetsController(window,view).isAppearanceLightNavigationBars=false
        }
    }

    LaunchedEffect(key1=playerScreenState.songName) {
        songArt=loadBitmapFromByteArray(playerScreenState.songImage,screenWidthPx,imgHeightPx,context)
        songArt?.let {bitmap->
            val palette=Palette.from(bitmap).generate()
            mainColor=palette.dominantSwatch?.let { Color(it.rgb) } ?: Color.DarkGray
            onMainColor=palette.dominantSwatch?.let { Color(it.bodyTextColor) } ?: Color.White
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title= {
                    Text(
                        text = "Now Playing",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 28.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick={ onNavigateUp() }) {
                        Icon(imageVector=Icons.AutoMirrored.Default.ArrowBack ,
                            contentDescription=null
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.DarkGray,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = mainColor
    ) {paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        ) {
            PlayerCentre(modifier = Modifier.weight(8f),
                songTitle = playerScreenState.songName,
                songArtist = playerScreenState.songArtist,
                songArt = songArt,
                onMainColor = onMainColor,
                mainColor = mainColor
            )
            PlayerSlider(modifier = Modifier.weight(3f),
                currentPos = playerScreenState.currentPositionString,
                songDuration = playerScreenState.songDuration,
                currentPosition = playerScreenState.currentPosition,
                onMainColor = onMainColor,
                onPositionChange = {onEvent(PlayerScreenEvents.OnPositionChange(it.toLong()))}
            )
            PlayerControls(modifier = Modifier.weight(2f),
                isPlaying = playerScreenState.isPlaying,
                repeatOn = playerScreenState.repeatMode,
                shuffleOn = playerScreenState.shuffle,
                onMainColor = onMainColor,
                onPreviousClick={onEvent(PlayerScreenEvents.OnPrevious)},
                onPlayPauseClick={onEvent(PlayerScreenEvents.OnPlayPause)},
                onNextClick = {onEvent(PlayerScreenEvents.OnNext)},
                onRepeatClick = {onEvent(PlayerScreenEvents.OnRepeatClick)},
                onShuffleClick = {onEvent(PlayerScreenEvents.OnShuffleClick)}
            )
        }
    }
}

@Composable
fun PlayerCentre(modifier: Modifier=Modifier ,
                 songTitle:String="name" ,
                 songArtist:String="artist" ,
                 mainColor: Color= Color.DarkGray ,
                 onMainColor: Color= Color.White ,
                 songArt: Bitmap?=null) {
    val context= LocalContext.current
    Box(modifier = modifier
        .fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter) {
        AnimatedContent(targetState = songArt,
            modifier = Modifier.fillMaxSize(),
            transitionSpec ={ addAnimation()}, label = "musicImg"
        ){ img->
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
                    colors = listOf(Color.Transparent , mainColor)
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
                        iterations = Int.MAX_VALUE ,
                        velocity = 30.dp ,
                        initialDelayMillis = 0
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
fun PlayerSlider(
    modifier: Modifier=Modifier,
    currentPos:String,
    songDuration:Float,
    currentPosition:Float,
    onMainColor: Color= Color.White,
    onPositionChange:(Float)->Unit
) {

    var currentPosition2 by remember { mutableFloatStateOf(0f) }

    Column(modifier = modifier
        .fillMaxWidth(),
        verticalArrangement = Arrangement.Bottom) {
        Slider(value = currentPosition,
            onValueChange = { currentPosition2=it},
            valueRange = 0f..songDuration,
            onValueChangeFinished = {onPositionChange(currentPosition2)},
            modifier = Modifier.padding(start = 6.dp, end = 6.dp),
            colors = SliderDefaults.colors(
                thumbColor = onMainColor,
                activeTrackColor = onMainColor,
                inactiveTrackColor = onMainColor.copy(0.3f)
            ))

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 5.dp , end = 5.dp),
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
        RepeatMode.REPEAT_ALL-> R.drawable.baseline_repeat_24
        else-> R.drawable.baseline_repeat_one_24
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