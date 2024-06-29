package com.github.tedblair2.muziki3.features.details.ui

import com.github.tedblair2.muziki3.core.local.model.Audio
import com.github.tedblair2.muziki3.core.local.model.CurrentTheme
import com.github.tedblair2.muziki3.core.local.model.Playlist

data class DetailsScreenState(
    val songs:List<Audio> = emptyList(),
    val currentScreenType: DetailScreenType=DetailScreenType.RECENT,
    val isLoading:Boolean=true,
    val title:String="",
    val playlist: Playlist?=null,
    val currentTheme: CurrentTheme=CurrentTheme.SYSTEM_THEME,
    val isMiniPlayerVisible:Boolean=false
)
