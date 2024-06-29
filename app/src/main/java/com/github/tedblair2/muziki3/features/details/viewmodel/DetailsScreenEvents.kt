package com.github.tedblair2.muziki3.features.details.viewmodel

import com.github.tedblair2.muziki3.core.local.model.Audio

sealed interface DetailsScreenEvents {
    data class GetAlbumSongs(val albumName:String):DetailsScreenEvents
    data object GetRecentSongs:DetailsScreenEvents
    data object GetFavoriteSongs:DetailsScreenEvents
    data class GetPlaylistSongs(val playlistId:Int):DetailsScreenEvents
    data class OnAudioClick(val songs:List<Audio> , val position:Int):DetailsScreenEvents
    data class OnAddToFavorite(val audio: Audio):DetailsScreenEvents
    data class OnRemoveFavorite(val audio: Audio):DetailsScreenEvents
    data class OnRemoveFromPlaylist(val audio: Audio):DetailsScreenEvents
    data class RemoveAudioFromPlayer(val audio: Audio):DetailsScreenEvents
}