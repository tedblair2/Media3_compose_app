package com.github.tedblair2.muziki3.features.home.viewmodel

import com.github.tedblair2.muziki3.core.local.model.Audio

sealed interface SongsScreenEvents {
    data class OnAudioClick(val songs:List<Audio> , val position:Int):SongsScreenEvents
    data class OnAddToFavorites(val audio: Audio):SongsScreenEvents
    data class RemoveAudioFromPlayer(val audio: Audio):SongsScreenEvents
}