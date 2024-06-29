package com.github.tedblair2.muziki3.features.playlistbottom.ui

import com.github.tedblair2.muziki3.core.local.model.Playlist

data class PlaylistBottomScreenState(
    val playlists: List<Playlist> = emptyList(),
    val isLoading:Boolean=true
)
