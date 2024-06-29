package com.github.tedblair2.muziki3.features.home.viewmodel

import com.github.tedblair2.muziki3.core.local.model.CurrentTheme
import com.github.tedblair2.muziki3.helpers.TabScreens

sealed interface HomeScreenEvents {
    data class CurrentTabScreen(val tabScreen: TabScreens):HomeScreenEvents
    data class OnThemeSelection(val currentTheme: CurrentTheme):HomeScreenEvents
}