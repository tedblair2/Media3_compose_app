package com.github.tedblair2.muziki3.features.home.ui

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.github.tedblair2.muziki3.features.createplaylist.ui.Age
import com.github.tedblair2.muziki3.features.createplaylist.ui.Parent
import com.github.tedblair2.muziki3.features.details.ui.DetailScreenType
import com.github.tedblair2.muziki3.features.home.viewmodel.HomeScreenViewModel
import kotlinx.serialization.Serializable

@Serializable
data object Home

fun NavGraphBuilder.homeScreen(
    navigateToDetailsScreen:(screenType:DetailScreenType,albumName:String,playlistId:Int)->Unit,
    onNavigateToPlayer:()->Unit={},
    onNavigateToCreatePlaylist:(age: Age, parent: Parent, playlist:Int, title:String)->Unit,
    onNavigateToSearch:()->Unit
){
    composable<Home>{
        val viewModel= hiltViewModel<HomeScreenViewModel>()
        val homeScreenState by viewModel.homeScreenState.collectAsStateWithLifecycle()

        HomeScreen(
            homeScreenState = homeScreenState,
            onEvent = viewModel::onEvent,
            navigateToDetailsScreen = navigateToDetailsScreen,
            onNavigateToPlayer = onNavigateToPlayer,
            onNavigateToCreatePlaylist = onNavigateToCreatePlaylist,
            onNavigateToSearch = onNavigateToSearch
        )
    }
}

fun NavController.navigateToHome(options:NavOptionsBuilder.()->Unit){
    navigate(Home){
        options()
    }
}