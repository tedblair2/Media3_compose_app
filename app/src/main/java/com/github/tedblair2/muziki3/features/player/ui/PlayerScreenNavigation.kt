package com.github.tedblair2.muziki3.features.player.ui

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.github.tedblair2.muziki3.features.player.viewmodel.PlayerScreenViewModel
import com.github.tedblair2.muziki3.helpers.Util
import kotlinx.serialization.Serializable

@Serializable
data object Player

fun NavGraphBuilder.playerScreen(
    onNavigateUp:()->Unit
){
    composable<Player>(
        deepLinks = listOf(navDeepLink { uriPattern=Util.PLAYER_URI })
    ){
        val viewModel=hiltViewModel<PlayerScreenViewModel>()
        val playerScreenState by viewModel.playerScreenState.collectAsStateWithLifecycle()

        PlayerScreen(
            onNavigateUp = onNavigateUp,
            playerScreenState = playerScreenState,
            onEvent = viewModel::onEvent
        )
    }
}

fun NavController.navigateToPlayer(
    options:NavOptionsBuilder.()->Unit={}
){
    navigate(Player){
        options()
    }
}