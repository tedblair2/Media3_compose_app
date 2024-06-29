package com.github.tedblair2.muziki3.features.playlists.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.tedblair2.muziki3.core.local.AppCoroutineContext
import com.github.tedblair2.muziki3.core.local.RoomRepository
import com.github.tedblair2.muziki3.core.local.model.Audio
import com.github.tedblair2.muziki3.data.redux.Store
import com.github.tedblair2.muziki3.features.playlists.ui.PlaylistScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val store:Store,
    private val roomRepository: RoomRepository,
    private val appCoroutineContext: AppCoroutineContext
):ViewModel() {

    private val _playlistScreenState= MutableStateFlow(PlaylistScreenState())
    val playlistScreenState= _playlistScreenState.asStateFlow()

    init {
        favoritesCount()
        recentsCount()
        getPlaylists()
    }

    fun onEvent(event: PlaylistScreenEvents){
        when(event){
            is PlaylistScreenEvents.OnDeletePlaylist -> {
                viewModelScope.launch(appCoroutineContext.io) {
                    roomRepository.deletePlaylist(event.playlist)
                }
            }
        }
    }

    private fun getPlaylists(){
        viewModelScope.launch(appCoroutineContext.io) {
            roomRepository.getPlaylists()
                .flowOn(appCoroutineContext.io)
                .collect{playlists->
                    _playlistScreenState.update {
                        it.copy(
                            playlists = playlists
                        )
                    }
                }
        }
    }

    private fun recentsCount(){
        viewModelScope.launch {
            store.getCurrentState {  }
                .collect{appState->
                    _playlistScreenState.update {
                        val audioList= getRecentSongs(appState.songs)
                        it.copy(recentCount = "${audioList.size} Songs")
                    }
                }
        }
    }

    private fun getRecentSongs(audioList:List<Audio>):List<Audio>{
        val list= arrayListOf<Audio>()
        if (audioList.size<10){
            return audioList
        }else{
            var j=0
            while (j<10){
                list.add(audioList[j])
                j++
            }
        }
        return list
    }

    private fun favoritesCount(){
        viewModelScope.launch {
            roomRepository.getFavorites()
                .flowOn(appCoroutineContext.io)
                .collect{audioList->
                    _playlistScreenState.update {
                        it.copy(favouriteCount = "${audioList.size} Songs")
                    }
                }
        }
    }
}