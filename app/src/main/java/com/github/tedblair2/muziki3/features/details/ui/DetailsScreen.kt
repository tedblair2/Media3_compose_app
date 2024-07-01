package com.github.tedblair2.muziki3.features.details.ui

import android.app.Activity
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.core.view.WindowCompat
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.github.tedblair2.muziki3.R
import com.github.tedblair2.muziki3.core.local.model.Audio
import com.github.tedblair2.muziki3.core.local.model.CurrentTheme
import com.github.tedblair2.muziki3.features.createplaylist.ui.Age
import com.github.tedblair2.muziki3.features.createplaylist.ui.Parent
import com.github.tedblair2.muziki3.features.details.viewmodel.DetailsScreenEvents
import com.github.tedblair2.muziki3.features.home.ui.AlertDialogExample
import com.github.tedblair2.muziki3.features.home.ui.DeleteAudioWrapper
import com.github.tedblair2.muziki3.features.home.ui.tabs.LoadingSong
import com.github.tedblair2.muziki3.features.home.ui.tabs.MusicSong
import com.github.tedblair2.muziki3.features.miniplayer.ui.MiniPlayerScreen
import com.github.tedblair2.muziki3.features.playlistbottom.ui.PlaylistBottom
import com.github.tedblair2.muziki3.helpers.ScreenWidget
import com.github.tedblair2.muziki3.helpers.ShimmerEffect
import com.github.tedblair2.muziki3.helpers.isScrollingUp
import com.github.tedblair2.muziki3.helpers.loadBitmapFromByteArray

@Composable
fun DetailsScreen(
    modifier: Modifier = Modifier,
    detailsScreenState: DetailsScreenState,
    onNavigateUp:()->Unit,
    onEvent:(DetailsScreenEvents)->Unit={},
    onNavigateToPlayer:()->Unit,
    onNavigateToCreatePlaylist:(age:Age,parent:Parent,playlist:Int,title:String)->Unit
) {
    val context= LocalContext.current
    val configuration= LocalConfiguration.current
    val screenWidth=configuration.screenWidthDp
    val screenWidthPx= with(LocalDensity.current){screenWidth.dp.toPx()}.toInt()
    val imgTopHeight= with(LocalDensity.current){190.dp.toPx()}.toInt()
    var showButton by remember {
        mutableStateOf(false)
    }
    var screenWidget by remember {
        mutableStateOf(ScreenWidget.MAIN)
    }
    var showSheet by remember { mutableStateOf(false) }
    var currentAudio by remember { mutableStateOf<Audio?>(null) }
    val lazyListState=rememberLazyListState()

    val view=LocalView.current
    val window=(view.context as Activity).window
    val mainColor=MaterialTheme.colorScheme.surfaceVariant.toArgb()

    val statusBarState=when(detailsScreenState.currentTheme){
        CurrentTheme.LIGHT_THEME->true
        CurrentTheme.DARK_THEME->false
        else->!isSystemInDarkTheme()
    }

    if (!view.isInEditMode){
        SideEffect {
            window.statusBarColor=mainColor
            window.navigationBarColor=mainColor
            WindowCompat.getInsetsController(window,view).isAppearanceLightStatusBars=statusBarState
            WindowCompat.getInsetsController(window,view).isAppearanceLightNavigationBars=statusBarState
        }
    }

    LaunchedEffect(key1 = detailsScreenState.currentScreenType) {
        when(detailsScreenState.currentScreenType){
            DetailScreenType.ALBUM -> {
                showButton=false
                screenWidget=ScreenWidget.MAIN
            }
            DetailScreenType.PLAYLIST -> {
                showButton=true
                screenWidget=ScreenWidget.PLAYLISTDETAIL
            }
            DetailScreenType.FAVORITE -> {
                showButton=true
                screenWidget=ScreenWidget.FAVORITE
            }
            DetailScreenType.RECENT -> {
                showButton=false
                screenWidget=ScreenWidget.MAIN
            }
        }
    }

    Scaffold(
        modifier=modifier.fillMaxSize(),
        floatingActionButton = {
            AnimatedVisibility(
                visible = showButton && lazyListState.isScrollingUp()
            ) {
                val customModifier=if (detailsScreenState.isMiniPlayerVisible) Modifier.padding(bottom = 75.dp)
                else Modifier

                FloatingActionButton(
                    onClick ={
                        when (detailsScreenState.currentScreenType) {
                            DetailScreenType.FAVORITE -> {
                                onNavigateToCreatePlaylist(Age.EXISTS,Parent.FAVORITE,0,"Favorites")
                            }
                            DetailScreenType.PLAYLIST -> {
                                onNavigateToCreatePlaylist(
                                    Age.EXISTS,
                                    Parent.PLAYLIST,
                                    detailsScreenState.playlist?.id ?: 0,
                                    detailsScreenState.playlist?.name ?: ""
                                )
                            }
                            else -> {}
                        }
                    },
                    modifier = customModifier
                ) {
                    Icon(
                        imageVector = Icons.Default.Add ,
                        contentDescription = "add"
                    )
                }
            }
        }
    ) {paddingValues ->
        Box(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
        ){
            Column(modifier = Modifier.fillMaxSize()) {
                ConstraintLayout(
                    constraintSet = initialConstraints(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .layoutId("header")
                    )


                    LazyColumn(modifier = Modifier
                        .fillMaxWidth()
                        .layoutId("body"),
                        state = lazyListState
                    ){

                        if (detailsScreenState.isLoading){
                            items(count = 15){
                                ShimmerEffect {brush->
                                    LoadingSong(shimmerBrush = brush)
                                }
                            }
                        }else{
                            items(
                                count = detailsScreenState.songs.size,
                                key = {detailsScreenState.songs[it].id}
                            ){position->
                                val audio=detailsScreenState.songs[position]
                                var isDelete by remember {
                                    mutableStateOf(false)
                                }
                                var showDeleteDialog by remember {
                                    mutableStateOf(false)
                                }

                                DeleteAudioWrapper(
                                    audioToDelete= audio,
                                    isDelete = isDelete,
                                    isVersionQ={ isDelete=true },
                                    onSuccessfulDelete = {
                                        isDelete=false
                                        onEvent(DetailsScreenEvents.RemoveAudioFromPlayer(audio))
                                    },
                                    unSuccessfulDelete = {isDelete=false}
                                ){
                                    MusicSong(
                                        audio = audio,
                                        screenWidget = screenWidget,
                                        modifier = Modifier
                                            .clickable {
                                                onEvent(DetailsScreenEvents.OnAudioClick(detailsScreenState.songs,position))
                                            },
                                        onRemoveFavorite = {
                                            onEvent(DetailsScreenEvents.OnRemoveFavorite(audio))
                                        },
                                        onAddToFavorites = {
                                            onEvent(DetailsScreenEvents.OnAddToFavorite(audio))
                                        },
                                        onRemovePlaylistItem = {
                                            onEvent(DetailsScreenEvents.OnRemoveFromPlaylist(audio))
                                        },
                                        onAddToPlaylist = {
                                            currentAudio=audio
                                            showSheet=true
                                        },
                                        onDelete = {
                                            showDeleteDialog=true
                                        }
                                    )
                                }

                                if (showDeleteDialog){
                                    AlertDialogExample(
                                        onDismissRequest= {
                                            isDelete = false
                                            showDeleteDialog = false
                                        } ,
                                        onConfirmation={
                                            isDelete=true
                                            showDeleteDialog=false
                                        } ,
                                        dialogTitle= "Delete Audio File",
                                        dialogText= "Are you sure you want to delete ${audio.name}?",
                                        icon= Icons.Default.Delete
                                    )
                                }
                            }
                        }
                    }
                    DetailsHeader(
                        modifier = Modifier.layoutId("image"),
                        topImg = if (detailsScreenState.songs.isNotEmpty()) loadBitmapFromByteArray(detailsScreenState.songs[0].artWork,screenWidthPx,imgTopHeight,context) else null,
                        key = if(detailsScreenState.songs.isNotEmpty()) detailsScreenState.songs[0].id.toString() else ""
                    )

                    IconButton(onClick = { onNavigateUp() },
                        modifier=Modifier.layoutId("backBtn")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription =null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Text(text = detailsScreenState.title, color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 24.sp,
                        modifier = Modifier
                            .layoutId("title"))

                }
                MiniPlayerScreen(
                    modifier = Modifier,
                    onNavigateToPlayer = onNavigateToPlayer
                )
            }

            currentAudio?.let {
                PlaylistBottom(
                    onDismissRequest ={
                        currentAudio=null
                        showSheet=false
                    } ,
                    selectedAudio = it,
                    showBottomSheet = showSheet,
                )
            }
        }
    }
}

@Composable
fun DetailsHeader(modifier: Modifier=Modifier ,
                  topImg: Bitmap?=null , key:String="") {
    val context= LocalContext.current
    Box(modifier = modifier
        .fillMaxWidth(),
        contentAlignment = Alignment.BottomStart) {
        if (topImg != null){
            val request= ImageRequest.Builder(context)
                .data(topImg)
                .placeholder(R.drawable.p32)
                .error(R.drawable.p32)
                .fallback(R.drawable.p32)
                .memoryCacheKey(key)
                .diskCacheKey(key)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()
            AsyncImage(model = request,
                contentDescription =null,
                imageLoader = context.imageLoader,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.White) )
        }else{
            Image(painter = painterResource(id = R.drawable.p32),
                contentDescription = null, contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.White))
        }
        Box(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.7f)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent , Color.DarkGray)
                )
            ))
    }
}


fun initialConstraints(): ConstraintSet {
    return ConstraintSet{
        val header=createRefFor("header")
        val backBtn=createRefFor("backBtn")
        val title=createRefFor("title")
        val image=createRefFor("image")
        val body=createRefFor("body")
        val play=createRefFor("play")

        constrain(header){
            width= Dimension.matchParent
            height= Dimension.value(190.dp)
            top.linkTo(parent.top,0.dp)
        }
        constrain(body){
            width= Dimension.matchParent
            height= Dimension.fillToConstraints
            top.linkTo(header.bottom,0.dp)
            bottom.linkTo(parent.bottom,0.dp)
        }
        constrain(image){
            width= Dimension.matchParent
            height= Dimension.fillToConstraints
            top.linkTo(parent.top,0.dp)
            bottom.linkTo(header.bottom,0.dp)
        }
        constrain(backBtn){
            top.linkTo(parent.top,10.dp)
            start.linkTo(parent.start,8.dp)
            alpha=0f
        }
        constrain(title){
            start.linkTo(parent.start,10.dp)
            bottom.linkTo(header.bottom,12.dp)
        }
//        constrain(play){
//            width= Dimension.value(60.dp)
//            height= Dimension.value(60.dp)
//            end.linkTo(parent.end,20.dp)
//            top.linkTo(header.bottom, (-30).dp)
//        }
    }
}

fun finalConstraints():ConstraintSet{
    return ConstraintSet{
        val header=createRefFor("header")
        val backBtn=createRefFor("backBtn")
        val title=createRefFor("title")
        val image=createRefFor("image")
        val body=createRefFor("body")
        val play=createRefFor("play")

        constrain(header){
            width= Dimension.matchParent
            height= Dimension.value(56.dp)
            top.linkTo(parent.top,0.dp)
        }
        constrain(body){
            width=Dimension.matchParent
            height= Dimension.fillToConstraints
            top.linkTo(header.bottom,0.dp)
            bottom.linkTo(parent.bottom,0.dp)
        }
        constrain(image){
            width= Dimension.matchParent
            height= Dimension.fillToConstraints
            top.linkTo(parent.top,0.dp)
            bottom.linkTo(header.bottom,0.dp)
            alpha=0f
        }
        constrain(backBtn){
            top.linkTo(parent.top)
            bottom.linkTo(header.bottom)
            start.linkTo(parent.start,8.dp)
        }
        constrain(title){
            start.linkTo(backBtn.end,10.dp)
            top.linkTo(parent.top)
            bottom.linkTo(header.bottom)
        }
//        constrain(play){
//            width= Dimension.value(40.dp)
//            height=Dimension.value(40.dp)
//            start.linkTo(parent.start)
//            end.linkTo(parent.end)
//            top.linkTo(header.bottom, (-20).dp)
//            alpha=0f
//        }
    }
}
