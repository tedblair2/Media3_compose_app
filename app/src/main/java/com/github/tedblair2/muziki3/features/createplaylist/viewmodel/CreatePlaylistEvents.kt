package com.github.tedblair2.muziki3.features.createplaylist.viewmodel

import com.github.tedblair2.muziki3.core.local.model.Audio
import com.github.tedblair2.muziki3.features.createplaylist.ui.Age
import com.github.tedblair2.muziki3.features.createplaylist.ui.Parent

sealed interface CreatePlaylistEvents {
    data class OnSelect(val audio: Audio):CreatePlaylistEvents
    data class OnNavigateToScreen(val age: Age,val parent: Parent,val playlistId:Int,val title:String):CreatePlaylistEvents
    data object OnAddToPlaylist:CreatePlaylistEvents
    data object OnError:CreatePlaylistEvents
}