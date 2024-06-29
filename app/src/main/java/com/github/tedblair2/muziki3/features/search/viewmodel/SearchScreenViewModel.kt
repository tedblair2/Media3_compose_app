package com.github.tedblair2.muziki3.features.search.viewmodel

import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import com.github.tedblair2.muziki3.core.local.AppCoroutineContext
import com.github.tedblair2.muziki3.core.local.RoomRepository
import com.github.tedblair2.muziki3.core.local.datastore.DataPrefService
import com.github.tedblair2.muziki3.core.local.model.Audio
import com.github.tedblair2.muziki3.data.redux.Store
import com.github.tedblair2.muziki3.features.search.ui.SearchScreenState
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchScreenViewModel @Inject constructor(
    private val store: Store ,
    private val controllerFuture: ListenableFuture<MediaController> ,
    @ApplicationContext private val context: Context ,
    private val roomRepository: RoomRepository ,
    private val appCoroutineContext: AppCoroutineContext ,
    private val dataPrefService: DataPrefService
):ViewModel() {

    private val _screenState=MutableStateFlow(SearchScreenState())
    val screenState=_screenState.asStateFlow()
    private var mediaController: MediaController?=null

    init {
        initMediaController()
    }

    fun onEvent(event: SearchScreenEvents){
        when(event){
            is SearchScreenEvents.OnSearch -> {
                searchAudio(event.query)
            }

            is SearchScreenEvents.OnAddToFavorites -> {
                addToFavorite(event.audio)
            }
            is SearchScreenEvents.OnAudioClick -> {
                kotlin.run {
                    val player = this.mediaController ?: return@run
                    player.setMediaItems(setMediaItems(event.songs), event.position, 0)
                    player.prepare()
                    player.play()
                    updateRecentPlaylist(event.songs)
                    updateLastPosition(event.position)
                }
            }
            is SearchScreenEvents.RemoveAudioFromPlayer -> {
                removeAudioFromPlayer(event.audio)
                removeAudioFromFavoriteAndPlaylist(event.audio)
            }
        }
    }

    private fun searchAudio(query:String){
        viewModelScope.launch {
            store.getCurrentState {  }
                .collect { appState ->
                    val filteredSongs = appState.songs
                        .sortedBy { it.name.lowercase() }
                        .filter { it.name.lowercase().contains(query) || it.artist.lowercase().contains(query) }
                    if (query.isNotEmpty()){
                        _screenState.update {
                            it.copy(
                                songs = filteredSongs,
                                isLoading = appState.isLoading,
                                isPermissionGranted = appState.permissionGranted
                            )
                        }
                    }else{
                        _screenState.update {state->
                            state.copy(
                                songs = emptyList() ,
                                isLoading = appState.isLoading,
                                isPermissionGranted = appState.permissionGranted
                            )
                        }
                    }
                }
        }
    }

    private fun initMediaController(){
        controllerFuture.addListener({
            mediaController=try {
                controllerFuture.get()
            }catch (e:Exception){
                null
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun removeAudioFromPlayer(audio: Audio){
        viewModelScope.launch {
            roomRepository.getAudioRecentList().collect{recents->
                recents.forEachIndexed { index, audioRecent ->
                    if (audio.id==audioRecent.id){
                        removeMediaItem(index)
                    }
                }
            }
        }
    }

    private fun removeAudioFromFavoriteAndPlaylist(audio: Audio){
        viewModelScope.launch(appCoroutineContext.io) {
            roomRepository.deleteAudioFromFavoritesAndPlaylist(audio)
        }
    }

    private fun removeMediaItem(position: Int){
        kotlin.run {
            val player = this.mediaController ?: return@run
            player.removeMediaItem(position)
        }
    }

    private fun updateLastPosition(position:Int){
        viewModelScope.launch(appCoroutineContext.io) {
            dataPrefService.setLastPosition(position.toLong())
        }
    }

    private fun updateRecentPlaylist(songs: List<Audio>){
        viewModelScope.launch(appCoroutineContext.io) {
            roomRepository.addAudioRecentListToDb(songs)
        }
    }

    private fun addToFavorite(audio: Audio){
        viewModelScope.launch(appCoroutineContext.io) {
            roomRepository.addAudioToFavorites(audio)
        }
    }

    private fun setMediaItems(songs:List<Audio>):List<MediaItem>{
        return songs.map {
            MediaItem.Builder()
                .setMediaMetadata(getMediaMetadata(it))
                .setUri(Uri.parse(it.path))
                .build()
        }
    }

    private fun getMediaMetadata(song: Audio): MediaMetadata {
        return MediaMetadata.Builder()
            .setTitle(song.name)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .setArtworkUri(Uri.parse(song.path))
            .setArtworkData(song.artWork, MediaMetadata.PICTURE_TYPE_FILE_ICON)
            .build()
    }
}