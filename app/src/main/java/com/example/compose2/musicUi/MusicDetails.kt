package com.example.compose2.musicUi

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val imgTopHeight= with(LocalDensity.current){180.dp.toPx()}.toInt()
    val size= with(LocalDensity.current){50.dp.toPx()}.toInt()
    LazyColumn(modifier = modifier.fillMaxSize()){
        item {
            DetailsHeader(title = title,
                topImg = if (detailsList.isNotEmpty()) loadBitmap(detailsList[0].path,screenWidthPx,imgTopHeight) else null,
                key = if(detailsList.isNotEmpty()) detailsList[0].album else "")
        }
        items(count = detailsList.size, key = {detailsList[it].id}){position->
            val audio=detailsList[position]
            var songImg by remember(audio.path){ mutableStateOf<Bitmap?>(null) }
            LaunchedEffect(key1 = audio.path){
                val bitmap= withContext(Dispatchers.IO){
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
    PlayListBottom(dismissBottomSheet = { showSheet=false },
        showBottomSheet = showSheet, playlists = playLists,
        onItemClick = { currentAudio?.let { it1 -> onAddToPlaylist(it1,it) } })
}

@Composable
fun DetailsHeader(title:String="",topImg:Bitmap?=null,key:String="") {
    val context= LocalContext.current
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(180.dp), contentAlignment = Alignment.BottomStart) {
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
        Text(text = title, color = Color.White,
            style = MaterialTheme.typography.bodyLarge, fontSize = 24.sp,
            modifier = Modifier.padding(6.dp))
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

@Preview(showBackground = true)
@Composable
fun MusicDetailsPreview() {

}