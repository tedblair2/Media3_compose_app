package com.example.compose2.pref

import kotlinx.coroutines.flow.Flow

interface DataPrefService {
    suspend fun setLastPosition(position:Long)
    suspend fun setCurrentTheme(selectedTheme:String)
    fun getLastPosition():Flow<Long>
    fun getCurrentTheme():Flow<String>
}