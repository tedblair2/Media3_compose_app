package com.github.tedblair2.muziki3.features.createplaylist.ui

import com.github.tedblair2.muziki3.core.local.model.Audio

data class CreatePlaylistScreenState(
    val songs:List<Audio> = emptyList(),
    val title:String = "",
    val selectedSongs:List<Audio> = emptyList(),
    val parent: Parent=Parent.FAVORITE,
    val age: Age=Age.EXISTS,
    val playlistId:Int=0,
    val isError:Boolean=false
)
