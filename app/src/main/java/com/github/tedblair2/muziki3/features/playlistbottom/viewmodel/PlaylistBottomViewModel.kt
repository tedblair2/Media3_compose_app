package com.github.tedblair2.muziki3.features.playlistbottom.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.tedblair2.muziki3.core.local.AppCoroutineContext
import com.github.tedblair2.muziki3.core.local.RoomRepository
import com.github.tedblair2.muziki3.features.playlistbottom.ui.PlaylistBottomScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistBottomViewModel @Inject constructor(
    private val roomRepository: RoomRepository,
    private val appCoroutineContext: AppCoroutineContext
):ViewModel() {

    private val _screenState= MutableStateFlow(PlaylistBottomScreenState())
    val screenState = _screenState.asStateFlow()

    init {
        getPlaylists()
    }

    fun onEvent(event:PlaylistBottomScreenEvents){
        when(event){
            is PlaylistBottomScreenEvents.OnPlaylistClick -> {
                viewModelScope.launch(appCoroutineContext.io){
                    roomRepository.addAudioToPlaylist(event.audio,event.playlist)
                }
            }
        }
    }

    private fun getPlaylists(){
        viewModelScope.launch(appCoroutineContext.io){
            roomRepository.getPlaylists()
                .flowOn(appCoroutineContext.io)
                .collect{playlists->
                    _screenState.update {
                        it.copy(playlists = playlists,isLoading = false)
                    }
                }
        }
    }
}