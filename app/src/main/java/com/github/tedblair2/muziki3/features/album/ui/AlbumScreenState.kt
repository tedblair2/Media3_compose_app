package com.github.tedblair2.muziki3.features.album.ui

import com.github.tedblair2.muziki3.core.local.model.Audio

data class AlbumScreenState(
    val albumList:List<Audio> = emptyList(),
    val isLoading:Boolean = true,
    val permissionGranted:Boolean=false,
)
