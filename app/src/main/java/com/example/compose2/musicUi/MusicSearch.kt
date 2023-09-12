package com.example.compose2.musicUi

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.compose2.model.Audio
import com.example.compose2.model.Playlist
import com.example.compose2.routes.Screens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MusicSearch(navHostController: NavHostController,
                list: List<Audio> = emptyList(),
                playLists: List<Playlist> = emptyList(),
                onItemClick: (List<Audio>, Int) -> Unit,
                onFavoriteClick: (Audio) -> Unit,
                onAddToPlaylist: (Audio, Playlist) -> Unit,
                onDelete: (Audio) -> Unit
) {
    var searchTxt by remember { mutableStateOf("") }
    var filteredList by remember { mutableStateOf(list) }
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally) {
        SearchTopBar(text = searchTxt,
            onTextChange = {
                searchTxt = it
                filteredList=list.filter { audio ->
                    audio.name.contains(it,ignoreCase = true)
                }
            },
            onCloseClicked = {navHostController.popBackStack()})
        if (searchTxt.isNotEmpty()){
            SearchItems(
                onItemClick = { it, it1 ->
                    onItemClick(it, it1)
                    navHostController.navigate(Screens.Player.route){
                        popUpTo(Screens.SearchSong.route){
                            inclusive=true
                        }
                    }
                },
                onFavoriteClick = {onFavoriteClick(it)},
                onAddToPlaylist = {it,it1->onAddToPlaylist(it,it1)},
                playLists = playLists, list = filteredList,
                onDelete = {onDelete(it)})
        }
    }
}

@Composable
fun SearchItems(list:List<Audio> = emptyList(),
                playLists: List<Playlist> = emptyList(),
                onItemClick: (List<Audio>, Int) -> Unit,
                onFavoriteClick: (Audio) -> Unit,
                onAddToPlaylist: (Audio, Playlist) -> Unit,
                onDelete: (Audio) -> Unit) {

    var showSheet by remember { mutableStateOf(false) }
    var currentAudio by remember { mutableStateOf<Audio?>(null) }
    val size= with(LocalDensity.current){50.dp.toPx()}.toInt()
    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(top = 5.dp)){
        items(count = list.size, key = {list[it].id}){ position->
            val audio=list[position]
            var songImg by remember(audio.path){
                mutableStateOf<Bitmap?>(null)
            }
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
                onRemoveFavorite = {},
                onDeletePlaylist = {},
                onRemovePlaylistItem = {}, onAddToPlaylist = {
                    showSheet=true
                    currentAudio=audio
                }, onDelete = {onDelete(audio)})
        }
    }
    PlayListBottom(dismissBottomSheet = { showSheet=false },
        showBottomSheet = showSheet, playlists = playLists,
        onItemClick = { currentAudio?.let { it1 -> onAddToPlaylist(it1,it) } })

}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(modifier: Modifier=Modifier,
                 text:String,
                 onTextChange:(String)->Unit,
                 onCloseClicked:()->Unit) {

    val focusRequester = remember { FocusRequester() }
    Surface(modifier = modifier
        .fillMaxWidth()
        .height(56.dp),
        color = MaterialTheme.colorScheme.background, tonalElevation = SearchBarDefaults.TonalElevation) {

        TextField(value = text,
            onValueChange ={onTextChange(it)},
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onGloballyPositioned { focusRequester.requestFocus() },
            colors = TextFieldDefaults.colors(
                cursorColor = MaterialTheme.colorScheme.onSurface.copy(0.5f)),
            placeholder ={ Text(text = "Search...", modifier = Modifier.alpha(0.5f))},
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription ="search",
                    modifier = Modifier.alpha(0.5f),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }, trailingIcon = {
                IconButton(onClick = {
                    if (text.isNotEmpty()) onTextChange("") else onCloseClicked()
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription ="close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ))
    }
}