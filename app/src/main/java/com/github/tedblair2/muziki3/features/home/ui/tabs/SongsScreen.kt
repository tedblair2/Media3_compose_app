package com.github.tedblair2.muziki3.features.home.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.github.tedblair2.muziki3.R
import com.github.tedblair2.muziki3.core.local.model.Audio
import com.github.tedblair2.muziki3.features.home.ui.AlertDialogExample
import com.github.tedblair2.muziki3.features.home.ui.DeleteAudioWrapper
import com.github.tedblair2.muziki3.features.home.ui.NoMediaScreen
import com.github.tedblair2.muziki3.features.home.viewmodel.SongsScreenEvents
import com.github.tedblair2.muziki3.features.home.viewmodel.SongsViewmodel
import com.github.tedblair2.muziki3.features.playlistbottom.ui.PlaylistBottom
import com.github.tedblair2.muziki3.helpers.ScreenWidget
import com.github.tedblair2.muziki3.helpers.ShimmerEffect
import com.github.tedblair2.muziki3.helpers.formatTime

@Composable
fun SongsScreen(
    modifier: Modifier = Modifier
) {

    val songsViewmodel= hiltViewModel<SongsViewmodel>()
    val songsScreenState by songsViewmodel.songScreenState.collectAsStateWithLifecycle()

    SongsScreenContent(
        modifier = modifier,
        songsScreenState = songsScreenState,
        onEvent = songsViewmodel::onEvent
    )
}

@Composable
fun SongsScreenContent(
    modifier: Modifier = Modifier ,
    songsScreenState: SongsScreenState,
    onEvent:(SongsScreenEvents)->Unit={}
) {
    var showSheet by remember { mutableStateOf(false) }
    var currentAudio by remember { mutableStateOf<Audio?>(null) }

    Box(modifier=modifier.fillMaxSize()){
        LazyColumn(modifier = Modifier
            .fillMaxSize()
        ) {
            if (songsScreenState.isLoading){
                items(count = 25){
                    ShimmerEffect {brush->
                        LoadingSong(shimmerBrush = brush)
                    }
                }
            }else if (!songsScreenState.permissionGranted || songsScreenState.songs.isEmpty()){
                item {
                    NoMediaScreen()
                }
            }else{
                items(
                    count = songsScreenState.songs.size,
                    key = {songsScreenState.songs[it].id}
                ){position->
                    val audio=songsScreenState.songs[position]
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
                            onEvent(SongsScreenEvents.RemoveAudioFromPlayer(audio))
                        },
                        unSuccessfulDelete = {isDelete=false}
                    ) {
                        MusicSong(
                            audio=audio,
                            modifier = Modifier
                                .clickable {
                                    onEvent(
                                        SongsScreenEvents.OnAudioClick(
                                            songsScreenState.songs,
                                            position
                                        )
                                    )
                                },
                            onDelete = {
                                showDeleteDialog=true
                            } ,
                            onAddToFavorites = {
                                onEvent(SongsScreenEvents.OnAddToFavorites(audio))
                            },
                            onAddToPlaylist = {
                                currentAudio=audio
                                showSheet=true
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

@Composable
fun LoadingSong(
    modifier: Modifier = Modifier,
    shimmerBrush: Brush
) {

    Row(modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End){
        Box(modifier =Modifier
            .size(60.dp)
            .padding(5.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(shimmerBrush))
        Column(modifier =Modifier
            .weight(1f)
            .padding(start=8.dp),
            verticalArrangement =Arrangement.spacedBy(5.dp)) {
            Spacer(modifier =Modifier
                .fillMaxWidth(0.9f)
                .height(15.dp)
                .background(shimmerBrush))
            Spacer(modifier =Modifier
                .fillMaxWidth(0.15f)
                .height(12.dp)
                .background(shimmerBrush))
        }
    }
}

@Composable
fun MusicSong(
    modifier: Modifier = Modifier,
    audio: Audio,
    screenWidget: ScreenWidget=ScreenWidget.MAIN,
    onDelete:()->Unit={},
    onRemoveFavorite: () -> Unit={},
    onRemovePlaylistItem: () -> Unit={},
    onAddToPlaylist: () -> Unit={},
    onAddToFavorites: () -> Unit={}
) {
    val context= LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Row(modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End) {

        Box(modifier =Modifier
            .size(60.dp)
            .padding(5.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
        ){
            val request= ImageRequest.Builder(context)
                .data(audio.picture)
                .placeholder(R.drawable.p33)
                .error(R.drawable.p33)
                .fallback(R.drawable.p33)
                .memoryCacheKey(audio.id.toString())
                .diskCacheKey(audio.id.toString())
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()
            AsyncImage(model = request ,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Column(modifier =Modifier
            .weight(1f)
            .padding(start=8.dp)) {
            Text(text = audio.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(text = formatTime(audio.duration) ,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(0.6f)
            )
        }
        Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)){
            IconButton(onClick = { expanded=true }) {
                Icon(imageVector = Icons.Default.MoreVert,
                    contentDescription = null
                )
            }
            when(screenWidget){
                ScreenWidget.MAIN -> {
                    MainDropDown(
                        isExpanded = expanded,
                        changeExpanded = {expanded=false},
                        onFavoriteClick ={
                            expanded=false
                            onAddToFavorites()
                        },
                        onAddToPlaylist = {
                            expanded=false
                            onAddToPlaylist()
                        },
                        onDelete = {
                            expanded=false
                            onDelete()
                        }
                    )
                }
                ScreenWidget.PLAYLISTDETAIL -> {
                    PlaylistItemDropDown(
                        isExpanded = expanded,
                        changeExpanded = {expanded=false},
                        onRemovePlaylistItem = {
                            expanded=false
                            onRemovePlaylistItem()
                        }
                    )
                }
                ScreenWidget.FAVORITE -> {
                    FavoritesDropDown(
                        isExpanded = expanded,
                        changeExpanded = { expanded=false },
                        onRemoveFavorite = {
                            expanded=false
                            onRemoveFavorite()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MainDropDown(
    isExpanded:Boolean,
    changeExpanded:()->Unit,
    onFavoriteClick:()->Unit,
    onAddToPlaylist:()->Unit,
    onDelete:()->Unit) {

    DropdownMenu(expanded = isExpanded,
        modifier =Modifier
            .fillMaxWidth(0.5f)
            .background(color=MaterialTheme.colorScheme.surface),
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
fun PlaylistItemDropDown(isExpanded:Boolean,changeExpanded:()->Unit,onRemovePlaylistItem:()->Unit) {
    DropdownMenu(expanded = isExpanded,
        modifier =Modifier
            .fillMaxWidth(0.5f)
            .background(color=MaterialTheme.colorScheme.surface),
        onDismissRequest = { changeExpanded() }) {
        DropdownMenuItem(text = { Text(text = "Remove from Playlist", color = MaterialTheme.colorScheme.onSurface) },
            onClick = { onRemovePlaylistItem() })
    }
}

@Composable
fun FavoritesDropDown(isExpanded: Boolean,changeExpanded: () -> Unit,onRemoveFavorite:()->Unit) {
    DropdownMenu(expanded = isExpanded,
        modifier =Modifier
            .fillMaxWidth(0.5f)
            .background(color=MaterialTheme.colorScheme.surface),
        onDismissRequest = { changeExpanded() }) {
        DropdownMenuItem(text = { Text(text = "Remove from Favorites",
            color = MaterialTheme.colorScheme.onSurface) }, onClick = { onRemoveFavorite() })
    }
}