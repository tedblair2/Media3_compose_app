package com.github.tedblair2.muziki3.features.createplaylist.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.github.tedblair2.muziki3.R
import com.github.tedblair2.muziki3.core.local.model.Audio
import com.github.tedblair2.muziki3.features.createplaylist.viewmodel.CreatePlaylistEvents
import com.github.tedblair2.muziki3.helpers.formatTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlaylistScreen(
    modifier: Modifier = Modifier,
    createPlaylistScreenState: CreatePlaylistScreenState,
    onEvent:(CreatePlaylistEvents)->Unit,
    onNavigateUp:()->Unit
) {
    val snackBarHostState= remember {
        SnackbarHostState()
    }

    LaunchedEffect(key1 = createPlaylistScreenState.isError) {
        if (createPlaylistScreenState.isError){
            snackBarHostState.showSnackbar("At least one song has to selected!")
            onEvent(CreatePlaylistEvents.OnError)
        }
    }

    Scaffold(
        modifier=modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Add to: ${createPlaylistScreenState.title}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 30.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState)}
    ) {paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {

            Text(text = "${createPlaylistScreenState.selectedSongs.size} selected",
                style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(7.dp))

            LazyColumn(
                modifier = Modifier
                    .padding(5.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                items(
                    count = createPlaylistScreenState.songs.size,
                    key = {createPlaylistScreenState.songs[it].id}
                ){position->
                    val audio=createPlaylistScreenState.songs[position]
                    val isSelected=createPlaylistScreenState.selectedSongs.contains(audio)

                    CreatePlaylistItem(
                        audio = audio,
                        isSelected = isSelected,
                        onSelect = {
                            onEvent(CreatePlaylistEvents.OnSelect(audio))
                        }
                    )
                }
            }
            
            Button(
                onClick = {
                    onEvent(CreatePlaylistEvents.OnAddToPlaylist)
                    if (createPlaylistScreenState.selectedSongs.isNotEmpty()){
                        onNavigateUp()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp)
            ) {
                Text(text = "Save to Playlist",
                    style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun CreatePlaylistItem(
    modifier: Modifier = Modifier,
    audio: Audio,
    isSelected:Boolean=false,
    onSelect:()->Unit
) {
    val isSelectedIcon=if(isSelected) painterResource(id = R.drawable.baseline_check_box_24) else painterResource(
        id = R.drawable.baseline_check_box_outline_blank_24
    )
    val context= LocalContext.current

    Row(modifier = modifier
        .clickable { onSelect() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End){

        Box(modifier = Modifier
            .size(50.dp)
            .background(Color.White)){
            val request= ImageRequest.Builder(context)
                .data(audio.picture)
                .placeholder(R.drawable.p32)
                .error(R.drawable.p32)
                .fallback(R.drawable.p32)
                .memoryCacheKey(audio.id.toString())
                .diskCacheKey(audio.id.toString())
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()
            AsyncImage(model = request ,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop)
        }

        Column(modifier = Modifier
            .weight(1f)
            .padding(start = 8.dp),
            verticalArrangement =Arrangement.spacedBy(2.dp)) {
            Text(text = audio.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(text = formatTime(audio.duration) ,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
            IconButton(onClick = { onSelect() }) {
                Icon(
                    painter = isSelectedIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}