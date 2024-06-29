package com.github.tedblair2.muziki3.data.redux

import com.github.tedblair2.muziki3.core.local.model.Audio

sealed interface InitialEvents:Action {
    data class GetSongs(val permissionGranted:Boolean):InitialEvents
    data class Songs(val songs:List<Audio>,val permissionGranted:Boolean):InitialEvents
}