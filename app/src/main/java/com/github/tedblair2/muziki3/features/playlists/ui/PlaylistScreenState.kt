package com.github.tedblair2.muziki3.features.playlists.ui

import com.github.tedblair2.muziki3.core.local.model.Playlist

data class PlaylistScreenState(
    val playlists: List<Playlist> = emptyList() ,
    val recentCount:String="0 Songs" ,
    val favouriteCount:String="0 Songs"
)
