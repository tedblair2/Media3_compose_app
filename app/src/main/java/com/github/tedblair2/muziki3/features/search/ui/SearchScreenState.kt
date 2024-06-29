package com.github.tedblair2.muziki3.features.search.ui

import com.github.tedblair2.muziki3.core.local.model.Audio

data class SearchScreenState(
    val songs:List<Audio> = emptyList(),
    val isLoading:Boolean=true,
    val isPermissionGranted:Boolean=false
)
