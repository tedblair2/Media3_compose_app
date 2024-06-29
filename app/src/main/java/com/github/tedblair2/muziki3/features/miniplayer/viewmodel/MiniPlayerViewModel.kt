package com.github.tedblair2.muziki3.features.miniplayer.viewmodel

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class MiniPlayerViewModel @Inject constructor(
    private val store:Store ,
    private val controllerFuture: ListenableFuture<MediaController> ,
    @ApplicationContext private val context: Context
):ViewModel() {

    private var controller: MediaController?=null
    val playerScreenState=store.getCurrentState {  }
        .map { it.playerScreenState }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerScreenState())

    init {
        initMediaController()
    }

    fun onEvent(event: MiniPlayerEvents){
        when(event){
            MiniPlayerEvents.OnNext -> {
                kotlin.run {
                    val player = this.controller ?: return@run
                    if (player.hasNextMediaItem()){
                        player.seekToNext()
                    }
                }
            }
            MiniPlayerEvents.OnPlayPause -> {
                kotlin.run {
                    val player = this.controller ?: return@run
                    if (player.isPlaying) {
                        player.pause()
                    } else {
                        player.play()
                    }
                }
            }
            MiniPlayerEvents.OnPrevious -> {
                kotlin.run {
                    val player = this.controller ?: return@run
                    if (player.hasPreviousMediaItem()) {
                        player.seekToPrevious()
                    }
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