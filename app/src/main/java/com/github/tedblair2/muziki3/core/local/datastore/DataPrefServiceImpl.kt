package com.github.tedblair2.muziki3.core.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.github.tedblair2.muziki3.core.local.model.CurrentTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DataPrefServiceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
): DataPrefService {

    private val lastPosition = longPreferencesKey("lastAudioPosition")
    private val theme=stringPreferencesKey("current_theme")

    override suspend fun setLastPosition(position: Long) {
        dataStore.edit {
            it[lastPosition]=position
        }
    }

    override suspend fun setCurrentTheme(currentTheme: CurrentTheme) {
        dataStore.edit {
            it[theme]=currentTheme.name
        }
    }

    override fun getLastPosition(): Flow<Long> {
        return dataStore.data.map {
            it[lastPosition] ?: 0L
        }
    }

    override fun getCurrentTheme(): Flow<CurrentTheme> {
        return dataStore.data.map {
            CurrentTheme.valueOf(it[theme] ?: CurrentTheme.SYSTEM_THEME.name)
        }
    }
}