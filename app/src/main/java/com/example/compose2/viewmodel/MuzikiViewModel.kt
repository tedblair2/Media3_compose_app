package com.example.compose2.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.example.compose2.dataStore
import com.example.compose2.model.Audio
import com.example.compose2.model.AudioRecent
import com.example.compose2.model.Playlist
import com.example.compose2.model.ThemeSelection
import com.example.compose2.repository.MuzikiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(SavedStateHandleSaveableApi::class)
class MuzikiViewModel(private val repository: MuzikiRepository, private val application: Application,
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

    private val lastPosition= intPreferencesKey("lastPosition")

    private val currentTheme= stringPreferencesKey("currentTheme")


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
    fun setLastPosition(pos:Int){
        viewModelScope.launch(Dispatchers.IO) {
            application.dataStore.edit {mutablePreferences ->
                mutablePreferences[lastPosition]=pos
            }
        }
    }

    fun setCurrentTheme(selectedTheme: String){
        viewModelScope.launch(Dispatchers.IO) {
            application.dataStore.edit { themepref->
                themepref[currentTheme]=selectedTheme
            }
        }
    }
    fun getLastPosition():Flow<Int>{
        return application.dataStore.data.map { pref->
            pref[lastPosition] ?: 0
        }
    }

    fun getCurrentTheme():Flow<String>{
        return application.dataStore.data.map { pref->
            pref[currentTheme] ?: ThemeSelection.SYSTEM_THEME.name
        }
    }
}