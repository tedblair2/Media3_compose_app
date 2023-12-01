package com.example.compose2.musicUi

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.ExperimentalMotionApi
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.compose2.R
import com.example.compose2.model.Audio
import com.example.compose2.model.Playlist
import com.example.compose2.model.ScreenWidget
import com.example.compose2.routes.Screens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMotionApi::class)
@Composable
fun MusicDetails(navHostController: NavHostController,
                 modifier: Modifier=Modifier,
                 detailsList:List<Audio> = emptyList(),
                 playLists:List<Playlist> = emptyList(),
                 title: String="",
                 screenWidget: ScreenWidget=ScreenWidget.MAIN,
                 onFavoriteClick:(Audio)->Unit,
                 onRemoveFavorite: (Audio) -> Unit,
                 onRemovePlaylistItem:(Audio)->Unit,
                 checkScreen:(ScreenWidget)->Unit,
                 onAddToPlaylist:(Audio,Playlist)->Unit,
                 onItemClick: (List<Audio>, Int) -> Unit,
                 onDelete: (Audio) -> Unit
) {
    checkScreen(screenWidget)
    var showSheet by remember { mutableStateOf(false) }
    var currentAudio by remember { mutableStateOf<Audio?>(null) }
    val configuration= LocalConfiguration.current
    val screenWidth=configuration.screenWidthDp
    val screenWidthPx= with(LocalDensity.current){screenWidth.dp.toPx()}.toInt()
    val imgTopHeight= with(LocalDensity.current){190.dp.toPx()}.toInt()
    val size= with(LocalDensity.current){50.dp.toPx()}.toInt()

    Box(modifier = modifier.fillMaxSize()){
        ConstraintLayout(
            constraintSet = initialConstraints(),
            modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .layoutId("header")
            )

            LazyColumn(modifier = Modifier
                .fillMaxWidth()
                .layoutId("body")){
                items(count = detailsList.size, key = {detailsList[it].id}){position->
                    val audio=detailsList[position]
                    var songImg by remember(audio.path){ mutableStateOf<Bitmap?>(null) }
                    LaunchedEffect(key1 = audio.path){
                        val bitmap= withContext(Dispatchers.Default){
                            loadBitmap(audio.path,size,size)
                        }
                        songImg=bitmap
                    }
                    MusicSong(mainText =audio.name,
                        subText = formatTime(audio.duration),
                        imageBackGround = Color.White,
                        songImg = songImg,
                        screenWidget = screenWidget,
                        onItemClick = {
                            onItemClick(detailsList, position)
                            navHostController.navigate(Screens.Player.route)
                        },
                        onFavoriteClick = {onFavoriteClick(audio)},
                        onRemoveFavorite = {onRemoveFavorite(audio)},
                        onDeletePlaylist = {},
                        onRemovePlaylistItem = {onRemovePlaylistItem(audio)},
                        onAddToPlaylist = {
                            showSheet=true
                            currentAudio=audio
                        },
                        onDelete = {onDelete(audio)})
                }
            }

            DetailsHeader(modifier = Modifier.layoutId("image"),
                topImg = if (detailsList.isNotEmpty()) loadBitmap(detailsList[0].path,screenWidthPx,imgTopHeight) else null,
                key = if(detailsList.isNotEmpty()) detailsList[0].album else "")

            IconButton(onClick = { navHostController.popBackStack() },
                modifier=Modifier.layoutId("backBtn")) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription =null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Text(text = title, color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 24.sp,
                modifier = Modifier
                    .layoutId("title"))
        }
    }

    PlayListBottom(dismissBottomSheet = { showSheet=false },
        showBottomSheet = showSheet, playlists = playLists,
        onItemClick = { currentAudio?.let { it1 -> onAddToPlaylist(it1,it) } })
}

@Composable
fun DetailsHeader(modifier: Modifier=Modifier,
                  topImg:Bitmap?=null,key:String="") {
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
                    colors = listOf(Color.Transparent, Color.DarkGray)
                )
            ))
    }
}

fun getSongsForAlbum(audioList:ArrayList<Audio>, name:String):ArrayList<Audio>{
    val list= arrayListOf<Audio>()
    var j=0
    for (song in audioList){
        if (song.album == name){
            list.add(j,song)
            j++
        }
    }
    return getUniqueSongs(list)
}
fun getUniqueSongs(list:ArrayList<Audio>):ArrayList<Audio>{
    val set= list.toSet()
    return ArrayList(set)
}

fun initialConstraints():ConstraintSet{
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

fun firstTest():ConstraintSet{
    return ConstraintSet{
        val header=createRefFor("header")
        val body=createRefFor("body")

        constrain(header){
            width= Dimension.matchParent
            height=Dimension.value(200.dp)
            top.linkTo(parent.top)
        }
        constrain(body){
            width=Dimension.matchParent
            height= Dimension.fillToConstraints
            top.linkTo(header.bottom,0.dp)
            bottom.linkTo(parent.bottom,0.dp)
        }
    }
}

fun lastTest():ConstraintSet{
    return ConstraintSet{
        val header=createRefFor("header")
        val body=createRefFor("body")

        constrain(header){
            width= Dimension.matchParent
            height=Dimension.value(56.dp)
            top.linkTo(parent.top)
        }
        constrain(body){
            width=Dimension.matchParent
            height= Dimension.fillToConstraints
            top.linkTo(header.bottom,0.dp)
            bottom.linkTo(parent.bottom,0.dp)
        }
    }
}


fun Float.toFontSize():Int{
    return (25 - (this*(25-20))).toInt()
}

fun Float.toOffset()= Offset(0f,this)

@Preview(showBackground = true)
@Composable
fun MusicDetailsPreview() {

}