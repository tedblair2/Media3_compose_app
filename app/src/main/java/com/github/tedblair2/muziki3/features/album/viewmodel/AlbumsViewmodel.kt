package com.github.tedblair2.muziki3.features.album.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.tedblair2.muziki3.core.local.model.Audio
import com.github.tedblair2.muziki3.data.redux.Store
import com.github.tedblair2.muziki3.features.album.ui.AlbumScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumsViewmodel @Inject constructor(
    private val store: Store
):ViewModel() {

    private val _albumsScreenState= MutableStateFlow(AlbumScreenState())
    val albumsScreenState= _albumsScreenState.asStateFlow()

    init {
        viewModelScope.launch {
            store.getCurrentState(subscriber = {})
                .collect{appState->
                    _albumsScreenState.update {state->
                        state.copy(
                            albumList = getAlbums(appState.songs),
                            isLoading = appState.isLoading,
                            permissionGranted = appState.permissionGranted
                        )
                    }
                }
        }
    }

    fun onEvent(event: AlbumsScreenEvents){

    }

    private fun getAlbums(list:List<Audio>):List<Audio>{
        val albumList= arrayListOf<Audio>()
        val uniqueAlbums= mutableSetOf<String>()
        for(audio in list){
            if (uniqueAlbums.add(audio.album)){
                albumList.add(audio)
            }
        }
        return  albumList
    }
}