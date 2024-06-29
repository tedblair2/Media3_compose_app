package com.github.tedblair2.muziki3.features.home.ui

import com.github.tedblair2.muziki3.core.local.model.CurrentTheme
import com.github.tedblair2.muziki3.helpers.TabScreens

data class HomeScreenState(
    val currentTab: TabScreens=TabScreens.SONGS,
    val currentTheme: CurrentTheme=CurrentTheme.SYSTEM_THEME,
    val isMiniPlayerVisible:Boolean=false
)
