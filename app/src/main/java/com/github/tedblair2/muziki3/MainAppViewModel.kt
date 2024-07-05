package com.github.tedblair2.muziki3

import android.net.Uri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MainAppViewModel @Inject constructor():ViewModel() {

    private val _mainAppState=MutableStateFlow(MainAppScreenState())
    val mainAppState=_mainAppState.asStateFlow()

    fun onEvent(event: MainEvent){
        when(event){
            is MainEvent.NavigateWithDeeplink->{
                _mainAppState.update {
                    it.copy(uri = event.deeplink)
                }
            }
            MainEvent.ConsumeEvent->{
                _mainAppState.update {
                    it.copy(uri = null)
                }
            }
        }
    }
}

data class MainAppScreenState(
    val uri: Uri?=null
)

sealed interface MainEvent {
    data class NavigateWithDeeplink(val deeplink: Uri) : MainEvent
    data object ConsumeEvent : MainEvent
}