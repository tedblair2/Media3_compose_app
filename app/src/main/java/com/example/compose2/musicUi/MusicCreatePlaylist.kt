package com.example.compose2.musicUi

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.compose2.viewmodel.PlaylistViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel

@Composable
fun CreatePlaylist(
    modifier: Modifier = Modifier,
    songList: List<Audio> = emptyList(),
    title: String = "title",
    currentPlaylist: Playlist?=null,
    navHostController: NavHostController,
    playlistViewModel: PlaylistViewModel = koinViewModel()
){
    val selectedList = playlistViewModel.selectedList
    val context= LocalContext.current
    val size= with(LocalDensity.current){50.dp.toPx()}.toInt()
    val playlist by remember { derivedStateOf { Playlist(title,selectedList) } }
    Column(modifier = modifier.fillMaxSize()) {
        Row(modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navHostController.popBackStack() }, modifier = Modifier.weight(1f)) {
                Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription =null,
                    modifier = Modifier.size(65.dp))
            }
            Text(text = "Add to: $title",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 30.sp, modifier = Modifier.weight(8f),
                textAlign = TextAlign.Center)
            Box(modifier = Modifier.weight(1f))
        }
        Text(text = "${selectedList.size} selected",
            style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(7.dp))
        LazyColumn(modifier = Modifier
            .padding(5.dp)
            .weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp)){
            items(items = songList, key = {it.id}){audio->
                var songImg by remember(audio.path){
                    mutableStateOf<Bitmap?>(null)
                }
                LaunchedEffect(key1 = audio.path){
                    val bitmap= withContext(Dispatchers.IO){
                        loadBitmap(audio.path,size,size)
                    }
                    songImg=bitmap
                }
                if (selectedList.contains(audio)){
                    CreatePlaylistItem(mainText = audio.name,
                        subText = formatTime(audio.duration), songImg = songImg, isSelected = true) {
                        playlistViewModel.addAudio(audio)
                    }
                }else{
                    CreatePlaylistItem(mainText = audio.name,
                        subText = formatTime(audio.duration), songImg = songImg) {
                        playlistViewModel.addAudio(audio)
                    }
                }
            }
        }
        Button(onClick = { if (selectedList.isNotEmpty()) {
            if (title=="Favorite"){
                for (audio in selectedList){
                    playlistViewModel.addFavorite(audio)
                }
            }else if (currentPlaylist != null){
                playlistViewModel.addAudioListToPlaylist(selectedList,currentPlaylist)
            }else{
                playlistViewModel.addPlaylist(playlist)
            }
            navHostController.popBackStack()
        }
        else Toast.makeText(context, "At least one song has to selected!", Toast.LENGTH_SHORT).show() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)) {
            Text(text = "Save to Playlist",
                style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun CreatePlaylistItem(isSelected:Boolean=false,
                       songImg:Bitmap?=null,
                       mainText:String="song name",
                       subText:String="3:20",
                       imageBackGround: Color = Color.Unspecified,
                       painter: Painter = painterResource(id = R.drawable.p32),
                       select:()->Unit) {
    val context= LocalContext.current
    val icon=if (isSelected) painterResource(id = R.drawable.baseline_check_box_24) else painterResource(
        id = R.drawable.baseline_check_box_outline_blank_24
    )
    Row(modifier = Modifier
        .fillMaxSize()
        .clickable { select() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End) {
        Box(modifier = Modifier
            .size(50.dp)
            .background(color = Color.White)){
            if (songImg != null){
                val request= ImageRequest.Builder(context)
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
                        .background(color = imageBackGround))
            }
        }
        Column(modifier = Modifier
            .weight(1f)
            .padding(start = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
            IconButton(onClick = {select()}) {
                Icon(painter = icon,
                    contentDescription =null,
                    tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayListBottom(showBottomSheet:Boolean=false,
                   dismissBottomSheet:()->Unit,
                   playlists:List<Playlist> = emptyList(),
                   onItemClick:(Playlist)->Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    if (showBottomSheet){
        ModalBottomSheet(onDismissRequest = { dismissBottomSheet() }, sheetState = sheetState) {
            LazyColumn(modifier = Modifier
                .fillMaxSize(0.9f)
                .padding(5.dp)){
                item {
                    Text(
                        text = "Select Playlist",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyLarge, fontSize = 24.sp,
                        textAlign = TextAlign.Center
                    )
                }
                items(items = playlists, key = {it.id}){
                    Row(modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            onItemClick(it)
                            scope
                                .launch {
                                    sheetState.hide()
                                }
                                .invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        dismissBottomSheet()
                                    }
                                }
                        },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End) {
                        Box(modifier = Modifier
                            .size(50.dp)){
                            Image(painter = painterResource(id = R.drawable.baseline_queue_music_24),
                                contentDescription =null,
                                modifier = Modifier
                                    .fillMaxSize(),
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground))
                        }
                        Column(modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(text = it.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(text = "${it.songs.size} Songs",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreatePreview() {

}