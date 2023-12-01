package com.example.compose2.musicUi

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose2.R
import com.example.compose2.model.Playlist
import com.example.compose2.model.ScreenWidget

@Composable
fun MusicPlaylists(onRowItemClicked:(String)->Unit,
                   recentsCount:String="0 Songs",
                   favoriteCount:String="0 Songs",
                   playLists:List<Playlist> = emptyList(),
                   onCreatePlaylist:(String)->Unit,
                   onDeletePlaylist:(Playlist)->Unit,
                   onPlayListClick:(Int)->Unit) {
    var showDialog by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        LazyColumn(modifier = Modifier.fillMaxSize()){
            item {
                val painter= painterResource(id = R.drawable.recents5)
                val painter2= painterResource(id = R.drawable.fave_music2)
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 5.dp)) {
                    PlaylistItem(modifier = Modifier.weight(1f),
                        painter =painter2 , mainText = "Favorites",
                        subText =favoriteCount, contentScale = ContentScale.Crop,
                        onItemClick = {onRowItemClicked(it)})
                    PlaylistItem(modifier = Modifier.weight(1f),
                        painter =painter , mainText = "Recently Added",
                        subText =recentsCount, onItemClick = {onRowItemClicked(it)})
                }
            }
            item {
                Text(text = "Playlists(${playLists.size})",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 10.dp), fontSize = 20.sp)
            }
            items(items = playLists, key = {it.id}){playList->
                MusicSong(painter = painterResource(id = R.drawable.baseline_queue_music_24),
                    mainText =playList.name,
                    subText = "${playList.songs.size} Songs",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                    onItemClick = {onPlayListClick(playList.id)},
                    screenWidget = ScreenWidget.PLAYLISTMAIN,
                    onFavoriteClick = {},
                    onRemoveFavorite = {},
                    onDeletePlaylist = {onDeletePlaylist(playList)},
                    onRemovePlaylistItem = {}, onAddToPlaylist = {}, onDelete = {})
            }
        }
        FloatingActionButton(
            onClick = { showDialog=true },
            modifier = Modifier
                .padding(end = 20.dp, bottom = 18.dp)) {
            Icon(imageVector = Icons.Default.Add, contentDescription =null)
        }
        if (showDialog){
            AlertCreatePlaylist(dismissAlert = {showDialog=false},
                onCreate = {
                    showDialog=false
                    onCreatePlaylist(it)})
        }
    }
}

@Composable
fun PlaylistItem(modifier: Modifier,
                 painter: Painter,
                 mainText:String,
                 subText:String,
                 contentScale: ContentScale= ContentScale.Fit,
                 onItemClick:(String)->Unit) {
    Box(modifier = modifier
        .height(150.dp)
        .clickable { onItemClick(mainText) }, contentAlignment = Alignment.BottomCenter) {
        Box(modifier = Modifier.fillMaxSize()){
            Image(painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.White), contentScale = contentScale)
        }
        Box(modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.DarkGray)
                )
            ))
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {

            Column(modifier = Modifier
                .weight(1f)){
                Text(
                    text = mainText,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 17.sp, fontWeight = FontWeight.Bold
                )
                Text(
                    text = subText,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Icon(painter = painterResource(id = R.drawable.baseline_play_circle_filled_24),
                contentDescription =null, tint = Color.White, modifier = Modifier.size(30.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlaylistPreview() {

}
