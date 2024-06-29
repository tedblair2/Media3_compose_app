package com.github.tedblair2.muziki3.features.playlists.viewmodel

import com.github.tedblair2.muziki3.core.local.model.Playlist

sealed interface PlaylistScreenEvents {
    data class OnDeletePlaylist(val playlist: Playlist) : PlaylistScreenEvents
}