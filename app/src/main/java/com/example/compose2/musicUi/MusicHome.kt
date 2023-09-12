package com.example.compose2.musicUi

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.util.DisplayMetrics
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
import com.example.compose2.routes.TabScreens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MusicHome(navHostController: NavHostController,
              modifier: Modifier=Modifier,
              songList:ArrayList<Audio>,
              albumList:ArrayList<Audio>,
              playLists:List<Playlist>,
              getRecentCount:String="0 Songs",
              favoritesCount:String="0 Songs",
              onFavoriteClick: (Audio) -> Unit,
              onRemoveFavorite: (Audio) -> Unit,
              onDeletePlaylist: (Playlist) -> Unit,
              onAddToPlaylist: (Audio,Playlist) -> Unit,
              onItemClick: (List<Audio>, Int) -> Unit,
              onDelete: (Audio) -> Unit,
              isRefreshing: Boolean=false,
              onRefresh: () -> Unit) {
    val tabScreens= listOf(
        TabScreens.Songs,
        TabScreens.Albums,
        TabScreens.Playlists
    )
    val pagerState= rememberPagerState { tabScreens.size }
    val scope= rememberCoroutineScope()
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TabRow(selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicator = {tabPositions ->
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    color = MaterialTheme.colorScheme.primary, width = 65.dp, shape = RoundedCornerShape(9.dp)
                )
            },
            divider = {} ) {
            tabScreens.forEachIndexed { index, tabScreen ->
                Tab(selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }, text = { Text(text = tabScreen.title) },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        HorizontalPager(state = pagerState) {page->
            when(page){
                0 -> {
                    MusicSongs(list = songList, playLists = playLists,
                        onItemClick = { list, pos ->
                            onItemClick(list, pos)
                            navHostController.navigate(Screens.Player.route)
                        },
                        onFavoriteClick ={onFavoriteClick(it)},
                        onRemoveFavorite = {onRemoveFavorite(it)},
                        onAddToPlaylist = {it,it1->onAddToPlaylist(it,it1)},
                        onDelete = {onDelete(it)}, isRefreshing = isRefreshing,
                        onRefresh = {onRefresh()})
                }
                1 -> {
                    MusicAlbums(albumList = albumList,
                        onAlbumClick ={
                            navHostController.navigate(Screens.Details.passParams("album",it))
                        })
                }
                2 -> {
                    MusicPlaylists(
                        onRowItemClicked = {navHostController.navigate(Screens.Details.passParams("playlist",it))},
                        recentsCount =getRecentCount, favoriteCount = favoritesCount, playLists = playLists,
                        onCreatePlaylist = {navHostController.navigate(Screens.CreatePlaylist.passParams("new",it))},
                        onDeletePlaylist ={onDeletePlaylist(it)},
                        onPlayListClick = {navHostController.navigate(Screens.Details.passParams("playlist",it.toString()))} )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MusicSongs(list:List<Audio> = emptyList(),
               playLists: List<Playlist> = emptyList(),
               onItemClick: (List<Audio>,Int) -> Unit,
               onFavoriteClick: (Audio) -> Unit,
               onRemoveFavorite: (Audio) -> Unit,
               onAddToPlaylist: (Audio,Playlist) -> Unit,
               onDelete: (Audio) -> Unit,
               isRefreshing:Boolean=false,
               onRefresh:()->Unit) {
    var showSheet by remember { mutableStateOf(false) }
    var currentAudio by remember { mutableStateOf<Audio?>(null) }
    val size= with(LocalDensity.current){50.dp.toPx()}.toInt()
    val refreshState= rememberPullRefreshState(refreshing = isRefreshing, onRefresh = { onRefresh() })
    Box(modifier = Modifier
        .fillMaxSize()
        .pullRefresh(state = refreshState)) {
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(top = 5.dp)){
            items(count = list.size, key = {list[it].id}){ position->
                val audio=list[position]
                var songImg by remember(audio.path){ mutableStateOf<Bitmap?>(null) }
                LaunchedEffect(key1 = audio.path){
                    val bitmap= withContext(Dispatchers.IO){
                        loadBitmap(audio.path,size,size)
                    }
                    songImg=bitmap
                }
                MusicSong(mainText = audio.name, subText = formatTime(audio.duration),
                    imageBackGround = Color.White, songImg = songImg,
                    onItemClick = {onItemClick(list,position)},
                    onFavoriteClick = {onFavoriteClick(audio)},
                    onRemoveFavorite = {onRemoveFavorite(audio)},
                    onDeletePlaylist = {},
                    onRemovePlaylistItem = {}, onAddToPlaylist = {
                        showSheet=true
                        currentAudio=audio
                    }, onDelete = {onDelete(audio)})
            }
        }
        PullRefreshIndicator(refreshing = isRefreshing, state =refreshState, modifier = Modifier.align(
            Alignment.TopCenter), contentColor = MaterialTheme.colorScheme.primary,
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant)
    }
    PlayListBottom(dismissBottomSheet = { showSheet=false },
        showBottomSheet = showSheet, playlists = playLists,
        onItemClick = { currentAudio?.let { it1 -> onAddToPlaylist(it1,it) } })
}

@Composable
fun MusicSong(painter: Painter=painterResource(id = R.drawable.p32),
              mainText:String,
              subText:String,
              songImg:Bitmap?=null,
              colorFilter: ColorFilter?=null,
              imageBackGround:Color=Color.Unspecified,
              screenWidget: ScreenWidget=ScreenWidget.MAIN,
              onItemClick:()->Unit,
              onFavoriteClick: () -> Unit,
              onRemoveFavorite: () -> Unit,
              onDeletePlaylist: () -> Unit,
              onRemovePlaylistItem: () -> Unit,
              onAddToPlaylist: () -> Unit,
              onDelete: () -> Unit) {
    val context= LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    Row(modifier = Modifier
        .fillMaxSize()
        .clickable { onItemClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement =Arrangement.End) {
        Box(modifier = Modifier
            .size(50.dp)){
            if (songImg != null){
                val request=ImageRequest.Builder(context)
                    .data(songImg)
                    .placeholder(R.drawable.p32)
                    .error(R.drawable.p32)
                    .fallback(R.drawable.p32)
                    .memoryCacheKey(mainText)
                    .diskCacheKey(mainText)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
                AsyncImage(model = request,
                    contentDescription =null,
                    imageLoader = context.imageLoader,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = imageBackGround))
            }else{
                Image(painter = painter,
                    contentDescription =null,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = imageBackGround),
                    colorFilter = colorFilter)
            }
        }
        Column(modifier = Modifier
            .weight(1f)
            .padding(start = 8.dp),
            verticalArrangement =Arrangement.spacedBy(2.dp)) {
            Text(text = mainText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(text = subText,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)){
            IconButton(onClick = { expanded=true }) {
                Icon(imageVector = Icons.Default.MoreVert,
                    contentDescription =null,
                    tint = MaterialTheme.colorScheme.onBackground )
            }
            when(screenWidget){
                ScreenWidget.MAIN->{
                    MainDropDown(isExpanded = expanded,
                        changeExpanded = {expanded=false},
                        onFavoriteClick = {
                            onFavoriteClick()
                            expanded=false},
                        onAddToPlaylist = {
                            onAddToPlaylist()
                            expanded=false
                        }, onDelete = {
                            onDelete()
                            expanded=false
                        })
                }
                ScreenWidget.PLAYLISTMAIN->{
                    PlaylistMainDropDown(isExpanded = expanded, changeExpanded = {expanded=false},
                        onDeletePlaylist ={onDeletePlaylist()} )
                }
                ScreenWidget.PLAYLISTDETAIL->{
                    PlaylistItemDropDown(isExpanded = expanded, changeExpanded = {expanded=false},
                        onRemovePlaylistItem = {
                            onRemovePlaylistItem()
                        expanded=false})
                }
                ScreenWidget.FAVORITE->{
                    FavoritesDropDown(isExpanded = expanded, changeExpanded = { expanded=false }) {
                        onRemoveFavorite()
                        expanded=false
                    }
                }
            }
        }
    }
}

@Composable
fun MainDropDown(isExpanded:Boolean,
                 changeExpanded:()->Unit,
                 onFavoriteClick:()->Unit,
                 onAddToPlaylist:()->Unit,
                 onDelete:()->Unit) {
    DropdownMenu(expanded = isExpanded,
        modifier = Modifier
            .fillMaxWidth(0.5f)
            .background(color = MaterialTheme.colorScheme.surface),
        onDismissRequest = { changeExpanded() }) {
        DropdownMenuItem(text = { Text(text = "Add to Favorites", color = MaterialTheme.colorScheme.onSurface) },
            onClick = { onFavoriteClick() })
        DropdownMenuItem(text = { Text(text = "Add to Playlist", color = MaterialTheme.colorScheme.onSurface) },
            onClick = { onAddToPlaylist() })
        DropdownMenuItem(text = { Text(text = "Delete",color = MaterialTheme.colorScheme.onSurface) },
            onClick = { onDelete() })
    }
}

@Composable
fun PlaylistMainDropDown(isExpanded:Boolean,changeExpanded:()->Unit,onDeletePlaylist:()->Unit) {
    DropdownMenu(expanded = isExpanded,
        modifier = Modifier
            .fillMaxWidth(0.5f)
            .background(color = MaterialTheme.colorScheme.surface),
        onDismissRequest = { changeExpanded() }) {
        DropdownMenuItem(text = { Text(text = "Delete Playlist", color = MaterialTheme.colorScheme.onSurface) },
            onClick = { onDeletePlaylist() })
    }
}

@Composable
fun PlaylistItemDropDown(isExpanded:Boolean,changeExpanded:()->Unit,onRemovePlaylistItem:()->Unit) {
    DropdownMenu(expanded = isExpanded,
        modifier = Modifier
            .fillMaxWidth(0.5f)
            .background(color = MaterialTheme.colorScheme.surface),
        onDismissRequest = { changeExpanded() }) {
        DropdownMenuItem(text = { Text(text = "Remove from Playlist", color = MaterialTheme.colorScheme.onSurface) },
            onClick = { onRemovePlaylistItem() })
    }
}

@Composable
fun FavoritesDropDown(isExpanded: Boolean,changeExpanded: () -> Unit,onRemoveFavorite:()->Unit) {
    DropdownMenu(expanded = isExpanded,
        modifier = Modifier
            .fillMaxWidth(0.5f)
            .background(color = MaterialTheme.colorScheme.surface),
        onDismissRequest = { changeExpanded() }) {
        DropdownMenuItem(text = { Text(text = "Remove from Favorites",
            color = MaterialTheme.colorScheme.onSurface) }, onClick = { onRemoveFavorite() })
    }
}

@Composable
fun AlertCreatePlaylist(dismissAlert: () -> Unit,onCreate:(String)->Unit) {
    var text by remember { mutableStateOf("") }
    val focusRequester= remember { FocusRequester() }
    Dialog(onDismissRequest = { dismissAlert() }) {
        Card(modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(20.dp),
            shape = RoundedCornerShape(15.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "New Playlist",
                    fontSize = 22.sp,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    textAlign = TextAlign.Start
                )
                TextField(
                    value = text,
                    onValueChange ={text=it},
                    modifier = Modifier
                        .padding(12.dp)
                        .focusRequester(focusRequester)
                        .onGloballyPositioned { focusRequester.requestFocus() },
                    singleLine = true,
                    placeholder = { Text(text = "Enter playlist name",
                        modifier = Modifier.alpha(0.5f))})
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { dismissAlert() }, modifier = Modifier.padding(8.dp)) {
                        Text(text = "Cancel")
                    }
                    Button(onClick = { if (text != "") onCreate(text) },modifier = Modifier.padding(8.dp)) {
                        Text(text = "Create")
                    }
                }
            }
        }
    }
}

fun formatTime(duration:Long):String{
    val time=TimeUnit.MILLISECONDS.toSeconds(duration)
    val hours= time/3600
    val minutes = time % 3600 / 60
    val seconds = time % 60
    if (hours<1){
        return "${minutes.pad()}:${seconds.pad()}"
    }
    return "${hours.pad()}:${minutes.pad()}:${seconds.pad()}"
}

fun Long.pad(): String {
    return this.toString().padStart(2, '0')
}

fun loadBitmap(path:String,maxWidth:Int,maxHeight: Int):Bitmap?{
    val art= getAlbumArt(path)
    if (art != null) {
        return decodeSampledBitmapFromByteArray(art,maxWidth, maxHeight)
    }
    return null
}
fun decodeSampledBitmapFromByteArray(data: ByteArray,maxWidth:Int,maxHeight: Int): Bitmap? {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeByteArray(data, 0, data.size, options)

    // Calculate the inSampleSize to reduce memory consumption
    options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)

    // Decode the bitmap with the calculated sample size
    options.inJustDecodeBounds = false
    return BitmapFactory.decodeByteArray(data, 0, data.size, options)
}
fun calculateInSampleSize(options: BitmapFactory.Options, maxWidth: Int, maxHeight: Int): Int {
    var inSampleSize = 1
    val width = options.outWidth
    val height = options.outHeight

    if (width > maxWidth || height > maxHeight) {
        val halfWidth = width / 2
        val halfHeight = height / 2

        while ((halfWidth / inSampleSize) >= maxWidth && (halfHeight / inSampleSize) >= maxHeight) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}

fun getAlbumArt(uri:String):ByteArray?{
    val retriever = MediaMetadataRetriever()
    try{
        retriever.setDataSource(uri)
    }catch (e:IllegalArgumentException){
        e.printStackTrace()
    }
    return retriever.embeddedPicture
}
@Preview(showBackground = true)
@Composable
fun MusicPreview() {
}