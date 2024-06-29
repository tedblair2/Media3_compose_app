package com.github.tedblair2.muziki3.features.playlists.ui

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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.tedblair2.muziki3.R
import com.github.tedblair2.muziki3.core.local.model.Playlist
import com.github.tedblair2.muziki3.features.details.ui.DetailScreenType
import com.github.tedblair2.muziki3.features.playlists.viewmodel.PlaylistScreenEvents
import com.github.tedblair2.muziki3.features.playlists.viewmodel.PlaylistViewModel

@Composable
fun PlaylistsScreen(
    modifier: Modifier = Modifier,
    navigateToDetailsScreen:(screenType:DetailScreenType,albumName:String,playlistId:Int)->Unit
) {
    val viewModel= hiltViewModel<PlaylistViewModel>()
    val playlistScreenState by viewModel.playlistScreenState.collectAsStateWithLifecycle()

    PlaylistsScreenContent(
        modifier = modifier,
        playlistScreenState = playlistScreenState,
        navigateToDetails = navigateToDetailsScreen,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun PlaylistsScreenContent(
    modifier: Modifier = Modifier,
    playlistScreenState: PlaylistScreenState,
    onEvent:(PlaylistScreenEvents)->Unit,
    navigateToDetails:(screenType:DetailScreenType,albumName:String,playlistId:Int)->Unit
) {

    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        item {
            val painter= painterResource(id = R.drawable.recents5)
            val painter2= painterResource(id = R.drawable.fave_music2)

            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 5.dp)) {
                PlaylistItem(
                    modifier = Modifier.weight(1f)
                        .clickable {
                                   navigateToDetails(DetailScreenType.FAVORITE,"",0)
                        },
                    painter =painter2 , mainText = "Favorites",
                    subText =playlistScreenState.favouriteCount,
                    contentScale = ContentScale.Crop
                )
                PlaylistItem(modifier = Modifier.weight(1f)
                    .clickable {
                               navigateToDetails(DetailScreenType.RECENT,"",0)
                    },
                    painter =painter , mainText = "Recently Added",
                    subText =playlistScreenState.recentCount,
                )
            }
        }
        item {
            Text(text = "Playlists(${playlistScreenState.playlists.size})",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 10.dp), fontSize = 20.sp)
        }
        items(
            items = playlistScreenState.playlists,
            key = {it.id}
        ){
            PlaylistRow(
                playlist = it,
                modifier = Modifier
                    .clickable {
                        navigateToDetails(DetailScreenType.PLAYLIST,"",it.id)
                    },
                onDeletePlaylist = {
                    onEvent(PlaylistScreenEvents.OnDeletePlaylist(it))
                })
        }
    }
}

@Composable
fun PlaylistRow(
    modifier: Modifier = Modifier,
    playlist: Playlist,
    onDeletePlaylist: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Row(modifier = modifier
        .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End) {

        Box(modifier = Modifier
            .size(50.dp)){

            Image(painter = painterResource(id = R.drawable.baseline_library_music_24),
                contentDescription =null,
                modifier = Modifier
                    .fillMaxSize(),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground))
        }
        Column(modifier = Modifier
            .weight(1f)
            .padding(start = 8.dp),
            verticalArrangement =Arrangement.spacedBy(2.dp)) {
            Text(text = playlist.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(text = "${playlist.songs.size} Songs" ,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
            IconButton(onClick={ expanded=true }) {
                Icon(
                    imageVector=Icons.Default.MoreVert ,
                    contentDescription=null
                )
            }
            PlaylistMainDropDown(
                isExpanded = expanded,
                changeExpanded = {expanded=false},
                onDeletePlaylist ={
                    expanded=false
                    onDeletePlaylist()
                }
            )
        }
    }
}

@Composable
fun PlaylistItem(
    modifier: Modifier,
    painter: Painter,
    mainText:String,
    subText:String,
    contentScale: ContentScale= ContentScale.Fit
) {
    Box(modifier = modifier
        .height(150.dp) ,
        contentAlignment = Alignment.BottomCenter) {
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
                    colors = listOf(Color.Transparent , Color.DarkGray)
                )
            ))
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp , end = 10.dp),
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
                contentDescription =null,
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
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