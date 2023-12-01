package com.example.compose2.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.example.compose2.model.Audio
import com.example.compose2.model.AudioRecent
import com.example.compose2.model.Playlist
import com.example.compose2.model.ThemeSelection
import com.example.compose2.pref.DataPrefService
import com.example.compose2.repository.MuzikiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(SavedStateHandleSaveableApi::class)
class MuzikiViewModel(private val repository: MuzikiRepository,
                      private val dataPrefService: DataPrefService,
                      savedStateHandle: SavedStateHandle):ViewModel(){

    private val _allSongs= MutableLiveData<ArrayList<Audio>>()
    val allSongs:LiveData<ArrayList<Audio>> = _allSongs

    private val _recentSongs= MutableLiveData<ArrayList<Audio>>()
    val recentSongs:LiveData<ArrayList<Audio>> = _recentSongs

    private val _albums=MutableLiveData<ArrayList<Audio>>()
    val albums:LiveData<ArrayList<Audio>> = _albums

    val favorites:LiveData<List<Audio>> =repository.getFavorites()

    val playlists:LiveData<List<Playlist>> = repository.getPlaylists()

    val recentPlaylist:Flow<List<AudioRecent>> = repository.getRecentList()


    val currentThemeFlow=dataPrefService.getCurrentTheme()
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),ThemeSelection.SYSTEM_THEME.name)


    var showMiniPlayer by savedStateHandle.saveable { mutableStateOf(true) }
        private set

    fun setMiniPlayerVisibility(value:Boolean){
        showMiniPlayer=value
    }

    fun deleteAudioFromPlaylistAndFavorite(audio: Audio){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAudioFromPlaylistsAndFavorite(audio)
        }
    }
    fun setAllSongs(allSongs:ArrayList<Audio>){
        _allSongs.postValue(allSongs)
    }
    fun setRecentSongs(recentSongs:ArrayList<Audio>){
        _recentSongs.postValue(recentSongs)
    }
    fun setAlbums(songList:ArrayList<Audio>){
        _albums.postValue(repository.getAlbums(songList))
    }
    fun addFavorite(audio: Audio){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addFavorite(audio)
        }
    }
    fun deleteFavorite(audio: Audio){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteFavorite(audio)
        }
    }

    fun deletePlaylist(playlist: Playlist){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePlaylist(playlist)
        }
    }

    fun addRecentPlaylist(list: List<Audio>){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addAudioListToDb(list)
        }
    }
    fun setLastPosition(pos:Long){
        viewModelScope.launch(Dispatchers.IO) {
            dataPrefService.setLastPosition(pos)
        }
    }

    fun setCurrentTheme(selectedTheme: String){
        viewModelScope.launch(Dispatchers.IO) {
            dataPrefService.setCurrentTheme(selectedTheme)
        }
    }
    fun getLastPosition():Flow<Long>{
        return dataPrefService.getLastPosition()
    }
}