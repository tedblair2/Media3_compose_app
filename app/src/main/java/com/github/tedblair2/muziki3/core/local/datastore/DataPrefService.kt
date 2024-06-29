package com.github.tedblair2.muziki3.core.local.datastore

import com.github.tedblair2.muziki3.core.local.model.CurrentTheme
import kotlinx.coroutines.flow.Flow

interface DataPrefService {
    suspend fun setLastPosition(position: Long)
    suspend fun setCurrentTheme(currentTheme: CurrentTheme)
    fun getLastPosition(): Flow<Long>
    fun getCurrentTheme():Flow<CurrentTheme>
}