package com.github.tedblair2.muziki3.features.details.ui

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.github.tedblair2.muziki3.features.createplaylist.ui.Age
import com.github.tedblair2.muziki3.features.createplaylist.ui.Parent
import com.github.tedblair2.muziki3.features.details.viewmodel.DetailsScreenEvents
import com.github.tedblair2.muziki3.features.details.viewmodel.DetailsViewModel
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

@Serializable
data class Details(val screenType: DetailScreenType,val albumName:String="",val playlistId:Int=0)

fun NavGraphBuilder.detailsScreen(
    onNavigateUp: () -> Unit={},
    onNavigateToPlayer:()->Unit={},
    onNavigateToCreatePlaylist:(age: Age, parent: Parent, playlist:Int, title:String)->Unit
){
    composable<Details>(
        typeMap = mapOf(typeOf<DetailScreenType>() to DetailScreenNavType())
    ){
        val arguments=it.toRoute<Details>()
        val viewModel= hiltViewModel<DetailsViewModel>()
        val detailsScreenState by viewModel.detailsScreenState.collectAsState()

        LaunchedEffect(key1 = arguments.screenType) {
            when(arguments.screenType){
                DetailScreenType.ALBUM -> {
                    viewModel.onEvent(DetailsScreenEvents.GetAlbumSongs(arguments.albumName))
                }
                DetailScreenType.PLAYLIST -> {
                    viewModel.onEvent(DetailsScreenEvents.GetPlaylistSongs(arguments.playlistId))
                }
                DetailScreenType.FAVORITE -> {
                    viewModel.onEvent(DetailsScreenEvents.GetFavoriteSongs)
                }
                DetailScreenType.RECENT -> {
                    viewModel.onEvent(DetailsScreenEvents.GetRecentSongs)
                }
            }
        }

        DetailsScreen(
            detailsScreenState = detailsScreenState,
            onNavigateUp = onNavigateUp,
            onEvent = viewModel::onEvent,
            onNavigateToPlayer = onNavigateToPlayer,
            onNavigateToCreatePlaylist = onNavigateToCreatePlaylist
        )
    }
}

fun NavController.navigateToDetails(
    screenType: DetailScreenType,
    albumName:String="",
    playlistId: Int=0,
    options:NavOptionsBuilder.()->Unit={}
){
    navigate(Details(screenType,albumName,playlistId)){
        options()
    }
}