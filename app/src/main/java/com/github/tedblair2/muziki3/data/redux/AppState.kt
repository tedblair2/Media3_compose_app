package com.github.tedblair2.muziki3.data.redux

import com.github.tedblair2.muziki3.core.local.model.Audio
import com.github.tedblair2.muziki3.features.player.ui.PlayerScreenState

data class AppState(
    val songs:List<Audio> = emptyList(),
    val isLoading:Boolean=true,
    val permissionGranted:Boolean=false,
    val playerScreenState: PlayerScreenState=PlayerScreenState()
)
