package com.github.tedblair2.muziki3.features.search.viewmodel

import com.github.tedblair2.muziki3.core.local.model.Audio

sealed interface SearchScreenEvents {
    data class OnSearch(val query:String):SearchScreenEvents
    data class OnAudioClick(val songs:List<Audio> , val position:Int):SearchScreenEvents
    data class OnAddToFavorites(val audio: Audio): SearchScreenEvents
    data class RemoveAudioFromPlayer(val audio: Audio): SearchScreenEvents
}