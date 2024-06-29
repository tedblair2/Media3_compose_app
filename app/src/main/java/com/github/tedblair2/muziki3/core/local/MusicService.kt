package com.github.tedblair2.muziki3.core.local

import com.github.tedblair2.muziki3.core.local.model.Audio
import kotlinx.coroutines.flow.Flow

interface MusicService {
    fun getSongs(permissionGranted:Boolean):Flow<List<Audio>>
}