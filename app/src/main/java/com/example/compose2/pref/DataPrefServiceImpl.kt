package com.example.compose2.pref

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.compose2.dataStore
import com.example.compose2.model.ThemeSelection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataPrefServiceImpl(private val context:Context) : DataPrefService {

    private val lastPosition= longPreferencesKey("lastAudioPosition")

    private val currentTheme= stringPreferencesKey("currentTheme")
    override suspend fun setLastPosition(position: Long) {
        context.dataStore.edit {mutablePreferences ->
            mutablePreferences[lastPosition]=position
        }
    }

    override suspend fun setCurrentTheme(selectedTheme: String) {
        context.dataStore.edit { themepref->
            themepref[currentTheme]=selectedTheme
        }
    }

    override fun getLastPosition(): Flow<Long> {
        return context.dataStore.data.map { pref->
            pref[lastPosition] ?: 0L
        }
    }

    override fun getCurrentTheme(): Flow<String> {
        return context.dataStore.data.map { pref->
            pref[currentTheme] ?: ThemeSelection.SYSTEM_THEME.name
        }
    }
}