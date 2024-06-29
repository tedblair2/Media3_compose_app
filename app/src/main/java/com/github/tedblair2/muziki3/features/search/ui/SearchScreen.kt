package com.github.tedblair2.muziki3.features.search.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.tedblair2.muziki3.core.local.model.Audio
import com.github.tedblair2.muziki3.features.home.ui.AlertDialogExample
import com.github.tedblair2.muziki3.features.home.ui.DeleteAudioWrapper
import com.github.tedblair2.muziki3.features.home.ui.NoMediaScreen
import com.github.tedblair2.muziki3.features.home.ui.tabs.LoadingSong
import com.github.tedblair2.muziki3.features.home.ui.tabs.MusicSong
import com.github.tedblair2.muziki3.features.playlistbottom.ui.PlaylistBottom
import com.github.tedblair2.muziki3.features.search.viewmodel.SearchScreenEvents
import com.github.tedblair2.muziki3.features.search.viewmodel.SearchScreenViewModel
import com.github.tedblair2.muziki3.helpers.ShimmerEffect

@Composable
fun SearchScreen(
    modifier: Modifier=Modifier,
    onNavigateUp: () -> Unit
) {
    val viewmodel= hiltViewModel<SearchScreenViewModel>()
    val screenState by viewmodel.screenState.collectAsStateWithLifecycle()

    SearchScreenContent(
        modifier = modifier,
        searchScreenState = screenState ,
        onNavigateUp = onNavigateUp,
        onEvent = viewmodel::onEvent
    )
}

@Composable
fun SearchScreenContent(
    modifier: Modifier=Modifier,
    searchScreenState: SearchScreenState,
    onNavigateUp:()->Unit,
    onEvent:(SearchScreenEvents)->Unit
) {
    var showSheet by remember { mutableStateOf(false) }
    var currentAudio by remember { mutableStateOf<Audio?>(null) }
    var searchText by remember {
        mutableStateOf("")
    }

    LaunchedEffect(key1 = searchText) {
        onEvent(SearchScreenEvents.OnSearch(searchText))
    }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) {paddingValues->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Top
        ) {
            SearchTopBar(
                searchText= searchText,
                onSearchText= {
                    searchText = it
                } ,
                onCloseClick = {
                    onNavigateUp()
                }
            )

            Box(
                modifier= Modifier
                    .fillMaxSize()
            ){
                LazyColumn(modifier = Modifier
                    .fillMaxSize()
                ) {
                    if (searchScreenState.isLoading){
                        items(count = 25){
                            ShimmerEffect {brush->
                                LoadingSong(shimmerBrush = brush)
                            }
                        }
                    }else if (searchText.isEmpty()){
                        item {
                            Text(
                                text = "Try searching for the music name or artist",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 15.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    } else if (!searchScreenState.isPermissionGranted || (searchText.isNotEmpty() && searchScreenState.songs.isEmpty())){
                        item {
                            NoMediaScreen()
                        }
                    }else{
                        items(
                            count = searchScreenState.songs.size,
                            key = {searchScreenState.songs[it].id}
                        ){position->
                            val audio=searchScreenState.songs[position]
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
                                    onEvent(SearchScreenEvents.RemoveAudioFromPlayer(audio))
                                },
                                unSuccessfulDelete = {isDelete=false}
                            ) {
                                MusicSong(
                                    audio=audio,
                                    modifier = Modifier
                                        .clickable {
                                            onEvent(
                                                SearchScreenEvents.OnAudioClick(
                                                    searchScreenState.songs,
                                                    position
                                                )
                                            )
                                        },
                                    onDelete = {
                                        showDeleteDialog=true
                                    } ,
                                    onAddToFavorites = {
                                        onEvent(SearchScreenEvents.OnAddToFavorites(audio))
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    modifier: Modifier=Modifier,
    searchText:String,
    onSearchText:(query:String)->Unit,
    onCloseClick:()->Unit
) {
    val focusRequester=remember {
        FocusRequester()
    }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = SearchBarDefaults.TonalElevation
    ) {
        TextField(
            value= searchText,
            onValueChange=onSearchText,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onGloballyPositioned { focusRequester.requestFocus() },
            colors = TextFieldDefaults.colors(
                cursorColor = MaterialTheme.colorScheme.onSurface.copy(0.5f)
            ),
            placeholder = {
                Text(text="Search...", modifier = Modifier.alpha(0.5f))
            },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector= Icons.Default.Search,
                    contentDescription=null,
                    modifier = Modifier.alpha(0.5f),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                IconButton(onClick={
                    if (searchText.isNotEmpty()){
                        onSearchText("")
                    }else{
                        focusRequester.freeFocus()
                        onCloseClick()
                    }
                }) {
                    Icon(
                        imageVector= Icons.Default.Close,
                        contentDescription=null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            )
        )
    }
}