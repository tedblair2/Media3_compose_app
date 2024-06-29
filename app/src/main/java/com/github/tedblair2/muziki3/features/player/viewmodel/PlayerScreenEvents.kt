package com.github.tedblair2.muziki3.features.player.viewmodel

sealed interface PlayerScreenEvents {
    data object OnPrevious:PlayerScreenEvents
    data object OnNext:PlayerScreenEvents
    data object OnPlayPause:PlayerScreenEvents
    data object OnShuffleClick:PlayerScreenEvents
    data object OnRepeatClick:PlayerScreenEvents
    data class OnPositionChange(val position:Long):PlayerScreenEvents
}