package com.github.tedblair2.muziki3.features.home.ui.tabs

import com.github.tedblair2.muziki3.core.local.model.Audio

data class SongsScreenState(
    val songs:List<Audio> = emptyList() ,
    val permissionGranted:Boolean=false,
    val isLoading:Boolean=true
)
