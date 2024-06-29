package com.github.tedblair2.muziki3.features.playlistbottom.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.tedblair2.muziki3.R
import com.github.tedblair2.muziki3.core.local.model.Audio
import com.github.tedblair2.muziki3.features.home.ui.NoMediaScreen
import com.github.tedblair2.muziki3.features.home.ui.tabs.LoadingSong
import com.github.tedblair2.muziki3.features.playlistbottom.viewmodel.PlaylistBottomScreenEvents
import com.github.tedblair2.muziki3.features.playlistbottom.viewmodel.PlaylistBottomViewModel
import com.github.tedblair2.muziki3.helpers.ShimmerEffect
import kotlinx.coroutines.launch

@Composable
fun PlaylistBottom(
    modifier: Modifier = Modifier,
    showBottomSheet:Boolean=false,
    onDismissRequest: () -> Unit,
    selectedAudio:Audio
) {
    val viewModel= hiltViewModel<PlaylistBottomViewModel>()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    PlaylistBottomContent(
        screenState = screenState,
        onEvent = viewModel::onEvent,
        modifier = modifier,
        showBottomSheet = showBottomSheet,
        onDismissRequest = onDismissRequest,
        selectedAudio = selectedAudio
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistBottomContent(
    modifier: Modifier=Modifier,
    screenState: PlaylistBottomScreenState,
    onEvent:(PlaylistBottomScreenEvents)->Unit,
    showBottomSheet:Boolean=false,
    onDismissRequest:()->Unit,
    selectedAudio:Audio
){
    val sheetState= rememberModalBottomSheetState()
    val scope= rememberCoroutineScope()

    if (showBottomSheet){
        ModalBottomSheet(onDismissRequest = { onDismissRequest() },sheetState = sheetState) {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(5.dp)
            ) {
                if (screenState.isLoading){
                    items(count = 15){
                        ShimmerEffect {brush->
                            LoadingSong(shimmerBrush = brush)
                        }
                    }
                }else if (screenState.playlists.isEmpty()){
                    item {
                        NoMediaScreen()
                    }
                }else{
                    item {
                        Text(
                            text = "Select Playlist",
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    items(items = screenState.playlists,key = {it.id}){playlist->
                        Row(modifier = modifier
                            .padding(vertical = 5.dp)
                            .clickable {
                                onEvent(PlaylistBottomScreenEvents.OnPlaylistClick(playlist,selectedAudio))
                                scope
                                    .launch {
                                        sheetState.hide()
                                    }
                                    .invokeOnCompletion {
                                        if (!sheetState.isVisible) {
                                            onDismissRequest()
                                        }
                                    }
                            } ,
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End) {

                            Box(modifier = Modifier
                                .size(50.dp)){
                                Image(painter = painterResource(id = R.drawable.baseline_library_music_24) ,
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
                        }
                    }
                }
            }
        }
    }
}