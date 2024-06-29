package com.github.tedblair2.muziki3.features.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.tedblair2.muziki3.core.local.AppCoroutineContext
import com.github.tedblair2.muziki3.core.local.datastore.DataPrefService
import com.github.tedblair2.muziki3.core.local.model.CurrentTheme
import com.github.tedblair2.muziki3.data.redux.Store
import com.github.tedblair2.muziki3.features.home.ui.HomeScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val dataPref:DataPrefService,
    private val appCoroutineContext: AppCoroutineContext,
    private val store:Store
):ViewModel() {

    private val _homeScreenState= MutableStateFlow(HomeScreenState())
    val homeScreenState=_homeScreenState.asStateFlow()

    init {
        getCurrentTheme()
        setMiniPlayerVisibility()
    }

    fun onEvent(event: HomeScreenEvents){
        when(event){
            is HomeScreenEvents.CurrentTabScreen->{
                _homeScreenState.update {
                    it.copy(
                        currentTab = event.tabScreen
                    )
                }
            }

            is HomeScreenEvents.OnThemeSelection -> {
                setCurrentTheme(event.currentTheme)
            }
        }
    }

    private fun setMiniPlayerVisibility(){
        viewModelScope.launch {
            store.getCurrentState {  }
                .collect{appState->
                    _homeScreenState.update {
                        it.copy(isMiniPlayerVisible = appState.playerScreenState.isMiniPlayerVisible)
                    }
                }
        }
    }

    private fun getCurrentTheme(){
        viewModelScope.launch {
            dataPref.getCurrentTheme()
                .collect{currentTheme->
                    _homeScreenState.update {
                        it.copy(
                            currentTheme = currentTheme
                        )
                    }
                }
        }
    }

    private fun setCurrentTheme(currentTheme: CurrentTheme){
        viewModelScope.launch(appCoroutineContext.default) {
            dataPref.setCurrentTheme(currentTheme)
        }
    }
}