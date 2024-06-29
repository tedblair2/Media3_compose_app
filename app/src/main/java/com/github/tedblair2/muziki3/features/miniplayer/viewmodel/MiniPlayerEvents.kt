package com.github.tedblair2.muziki3.features.miniplayer.viewmodel

sealed interface MiniPlayerEvents {
    data object OnPrevious:MiniPlayerEvents
    data object OnNext:MiniPlayerEvents
    data object OnPlayPause:MiniPlayerEvents
}