package com.example.compose2.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.example.compose2.model.RepeatMode

@OptIn(SavedStateHandleSaveableApi::class)
class PlayerViewModel(savedStateHandle: SavedStateHandle):ViewModel() {
    var songName by savedStateHandle.saveable { mutableStateOf("") }
        private set
    var songArtist by savedStateHandle.saveable { mutableStateOf("") }
        private set
    var isPlaying by savedStateHandle.saveable { mutableStateOf(false) }
        private set
    var repeat by savedStateHandle.saveable { mutableStateOf(RepeatMode.REPEAT_OFF) }
        private set
    var shuffle by savedStateHandle.saveable { mutableStateOf(false) }
        private set
    var currentPosition by savedStateHandle.saveable { mutableStateOf(0f) }
        private set
    var songDuration by savedStateHandle.saveable { mutableStateOf(0f) }
        private set
    var currentDurationString by savedStateHandle.saveable { mutableStateOf("00:00") }
        private set

    private val _artWorkLive=MutableLiveData<ByteArray?>()
    val artWorkLive:LiveData<ByteArray?> = _artWorkLive

    fun setImgUri(value: ByteArray?){
        _artWorkLive.postValue(value)
    }
    fun setSongTitle(name:String){
        songName=name
    }
    fun setSongArtistTitle(artist:String){
        songArtist=artist
    }
    fun setIsPlayingValue(value:Boolean){
        isPlaying=value
    }
    fun setCurrentPositionValue(value:Float){
        currentPosition=value
    }
    fun setSongDurationValue(value: Float){
        songDuration=value
    }
    fun setCurrentPositionStringValue(value:String){
        currentDurationString=value
    }
    fun setRepeatMode(value: RepeatMode){
        repeat=value
    }
    fun setShuffleMode(value: Boolean){
        shuffle=value
    }
}