package com.github.tedblair2.muziki3.data.redux

import kotlinx.coroutines.flow.Flow

interface Store{
    fun applyMiddleWare(middleWare: MiddleWare):Store
    fun applyReducer(reducer: Reducer<AppState>):Store
    fun getCurrentState(subscriber:Subscriber): Flow<AppState>
}