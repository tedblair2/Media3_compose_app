package com.github.tedblair2.muziki3.features.createplaylist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.tedblair2.muziki3.core.local.AppCoroutineContext
import com.github.tedblair2.muziki3.core.local.RoomRepository
import com.github.tedblair2.muziki3.core.local.model.Playlist
import com.github.tedblair2.muziki3.data.redux.Store
import com.github.tedblair2.muziki3.features.createplaylist.ui.Age
import com.github.tedblair2.muziki3.features.createplaylist.ui.CreatePlaylistScreenState
import com.github.tedblair2.muziki3.features.createplaylist.ui.Parent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreatePlaylistViewModel @Inject constructor(
    private val store: Store,
    private val roomRepository: RoomRepository,
    private val appCoroutineContext: AppCoroutineContext
):ViewModel() {

    private val _screenState= MutableStateFlow(CreatePlaylistScreenState())
    val screenState=_screenState.asStateFlow()

    init {
        viewModelScope.launch {
            store.getCurrentState {  }
                .collect{appState->
                    _screenState.update {state->
                        state.copy(
                            songs = appState.songs.sortedBy { it.name.lowercase() }
                        )
                    }
                }
        }
    }

    fun onEvent(event:CreatePlaylistEvents){
        when(event){
            is CreatePlaylistEvents.OnSelect -> {
                _screenState.update {
                    val selectedSongs=screenState.value.selectedSongs.toMutableList()
                    if (selectedSongs.contains(event.audio)){
                        selectedSongs.remove(event.audio)
                    }else{
                        selectedSongs.add(event.audio)
                    }
                    it.copy(selectedSongs = selectedSongs)
                }
            }

            is CreatePlaylistEvents.OnNavigateToScreen -> {
                _screenState.update {
                    it.copy(
                        title = event.title,
                        age = event.age,
                        parent = event.parent,
                        playlistId = event.playlistId
                    )
                }
            }

            CreatePlaylistEvents.OnAddToPlaylist -> {
                if (screenState.value.selectedSongs.isNotEmpty()){
                    if (screenState.value.age==Age.NEW){
                        viewModelScope.launch(appCoroutineContext.io) {
                            val playlist=Playlist(
                                name = screenState.value.title,
                                songs = screenState.value.selectedSongs
                            )
                            roomRepository.addPlaylist(playlist)
                        }
                    }else if (screenState.value.age==Age.EXISTS && screenState.value.parent==Parent.FAVORITE){
                        viewModelScope.launch(appCoroutineContext.io) {
                            roomRepository.addAudioListToFavorites(screenState.value.selectedSongs)
                        }
                    }else{
                        viewModelScope.launch(appCoroutineContext.io) {
                            roomRepository.addAudioListToPlaylist(
                                screenState.value.selectedSongs,
                                screenState.value.playlistId)
                        }
                    }
                }else{
                    _screenState.update {
                        it.copy(
                            isError = !it.isError
                        )
                    }
                }
            }

            CreatePlaylistEvents.OnError -> {
                _screenState.update {
                    it.copy(
                        isError = !it.isError
                    )
                }
            }
        }
    }
}