package com.github.tedblair2.muziki3.features.player.viewmodel

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import com.github.tedblair2.muziki3.data.redux.Store
import com.github.tedblair2.muziki3.features.player.ui.PlayerScreenState
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PlayerScreenViewModel @Inject constructor(
    private val store: Store ,
    private val controllerFuture: ListenableFuture<MediaController> ,
    @ApplicationContext private val context: Context
):ViewModel() {

    private var controller: MediaController?=null
    val playerScreenState = store.getCurrentState { }
        .map { it.playerScreenState }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),PlayerScreenState())

    init {
        initMediaController()
    }

    fun onEvent(event: PlayerScreenEvents){
        when(event){
            PlayerScreenEvents.OnNext -> {
                kotlin.run {
                    val player = this.controller ?: return@run
                    if (player.hasNextMediaItem()){
                        player.seekToNext()
                    }
                }
            }
            PlayerScreenEvents.OnPlayPause -> {
                kotlin.run {
                    val player = this.controller ?: return@run
                    if (player.isPlaying) {
                        player.pause()
                    } else {
                        player.play()
                    }
                }
            }
            is PlayerScreenEvents.OnPositionChange -> {
                kotlin.run {
                    val player = this.controller ?: return@run
                    player.seekTo(event.position)
                }
            }
            PlayerScreenEvents.OnPrevious -> {
                kotlin.run {
                    val player = this.controller ?: return@run
                    if (player.hasPreviousMediaItem()) {
                        player.seekToPrevious()
                    }
                }
            }
            PlayerScreenEvents.OnRepeatClick -> {
                kotlin.run {
                    val player = this.controller ?: return@run
                    when(player.repeatMode){
                        ExoPlayer.REPEAT_MODE_OFF->player.repeatMode=ExoPlayer.REPEAT_MODE_ALL
                        ExoPlayer.REPEAT_MODE_ALL->player.repeatMode=ExoPlayer.REPEAT_MODE_ONE
                        ExoPlayer.REPEAT_MODE_ONE->player.repeatMode=ExoPlayer.REPEAT_MODE_OFF
                    }
                }
            }
            PlayerScreenEvents.OnShuffleClick -> {
                kotlin.run {
                    val player = this.controller ?: return@run
                    player.shuffleModeEnabled = !player.shuffleModeEnabled
                }
            }
        }
    }

    private fun initMediaController(){
        controllerFuture.addListener({
            controller=try {
                controllerFuture.get()
            }catch (e:Exception){
                null
            }
        }, ContextCompat.getMainExecutor(context))
    }
}