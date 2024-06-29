package com.github.tedblair2.muziki3

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.github.tedblair2.muziki3.features.createplaylist.ui.createPlaylistScreen
import com.github.tedblair2.muziki3.features.createplaylist.ui.navigateToCreatePlaylist
import com.github.tedblair2.muziki3.features.details.ui.detailsScreen
import com.github.tedblair2.muziki3.features.details.ui.navigateToDetails
import com.github.tedblair2.muziki3.features.home.ui.Home
import com.github.tedblair2.muziki3.features.home.ui.homeScreen
import com.github.tedblair2.muziki3.features.player.ui.navigateToPlayer
import com.github.tedblair2.muziki3.features.player.ui.playerScreen
import com.github.tedblair2.muziki3.features.search.ui.navigateToSearch
import com.github.tedblair2.muziki3.features.search.ui.searchScreen

@Composable
fun MainApp(
    modifier: Modifier = Modifier
) {

    val rootNavController=rememberNavController()

    NavHost(
        navController = rootNavController ,
        startDestination = Home,
        modifier = modifier
    ){

        homeScreen(
            navigateToDetailsScreen = {screenType, albumName, playlistId ->
                rootNavController.navigateToDetails(screenType, albumName, playlistId)
            },
            onNavigateToPlayer = {
                rootNavController.navigateToPlayer()
            },
            onNavigateToCreatePlaylist = {age,parent,playlistId,title->
                rootNavController.navigateToCreatePlaylist(age, parent, playlistId, title)
            },
            onNavigateToSearch = {
                rootNavController.navigateToSearch()
            }
        )

        detailsScreen(
            onNavigateUp = {
                rootNavController.navigateUp()
            },
            onNavigateToPlayer = {
                rootNavController.navigateToPlayer()
            },
            onNavigateToCreatePlaylist = {age,parent,playlistId,title->
                rootNavController.navigateToCreatePlaylist(age, parent, playlistId, title)
            }
        )

        playerScreen(
            onNavigateUp = {
                rootNavController.navigateUp()
            }
        )

        createPlaylistScreen(
            onNavigateUp = {
                rootNavController.navigateUp()
            }
        )

        searchScreen(
            onNavigateUp = {
                rootNavController.navigateUp()
            }
        )
    }
}