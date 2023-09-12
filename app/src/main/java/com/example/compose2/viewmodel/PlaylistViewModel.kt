package com.example.compose2.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.compose2.model.Audio
import com.example.compose2.model.Playlist
import com.example.compose2.repository.MuzikiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlaylistViewModel(private val repository: MuzikiRepository):ViewModel() {
    private val _selectedList= mutableStateListOf<Audio>()
    val selectedList:List<Audio> = _selectedList

    fun addAudio(audio: Audio){
        if (_selectedList.contains(audio)){
            _selectedList.remove(audio)
        }else{
            _selectedList.add(audio)
        }
    }
    fun addFavorite(audio: Audio){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addFavorite(audio)
        }
    }
    fun addPlaylist(playlist: Playlist){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addPlaylist(playlist)
        }
    }

    fun addAudioListToPlaylist(audioList:List<Audio>,playlist: Playlist){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addAudioListToPlaylist(audioList, playlist)
        }
    }

    fun addAudioToPlaylist(audio: Audio,playlist: Playlist){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addAudioToPlaylist(audio, playlist)
        }
    }

    fun removeAudioFromPlaylist(audio: Audio,playlist: Playlist){
        viewModelScope.launch(Dispatchers.IO){
            repository.removeAudioFromPlaylist(audio, playlist)
        }
    }

    fun getPlaylistById(id:Int): LiveData<Playlist> {
        return repository.getPlaylistById(id)
    }
}