package com.github.tedblair2.muziki3.features.playlistbottom.viewmodel

import com.github.tedblair2.muziki3.core.local.model.Audio
import com.github.tedblair2.muziki3.core.local.model.Playlist

sealed interface PlaylistBottomScreenEvents {
    data class OnPlaylistClick(val playlist: Playlist,val audio: Audio):PlaylistBottomScreenEvents
}