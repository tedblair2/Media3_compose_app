package com.example.compose2.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.compose2.model.Audio
import com.example.compose2.model.Playlist
import com.example.compose2.model.ScreenWidget
import com.example.compose2.musicUi.CreatePlaylist
import com.example.compose2.musicUi.MusicDetails
import com.example.compose2.musicUi.MusicHome
import com.example.compose2.musicUi.MusicPlayer
import com.example.compose2.musicUi.MusicSearch
import com.example.compose2.musicUi.getSongsForAlbum
import com.example.compose2.viewmodel.PlayerViewModel
import com.example.compose2.viewmodel.PlaylistViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun NavGraphSetUp(
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: PlaylistViewModel = koinViewModel(),
    playerViewModel: PlayerViewModel,
    songList: ArrayList<Audio>,
    albumList: ArrayList<Audio>,
    recentList: ArrayList<Audio>,
    favoritesList: List<Audio>,
    playlists: List<Playlist>,
    showFloatingButton: (Boolean) -> Unit,
    onFavoriteClick: (Audio) -> Unit,
    onRemoveFavorite: (Audio) -> Unit,
    onDeletePlaylist: (Playlist) -> Unit,
    checkDetailScreen:(ScreenWidget,Int)->Unit,
    onItemClick: (List<Audio>, Int) -> Unit,
    onPreviousClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onShuffleClick:()->Unit,
    onRepeatClick:()->Unit,
    onPositionChange:(Float)->Unit,
    onDelete: (Audio) -> Unit,
    isRefreshing: Boolean=false,
    onRefresh: () -> Unit
) {
    NavHost(navController = navHostController, startDestination =Screens.Home.route){
        composable(route = Screens.Home.route){
            MusicHome(navHostController = navHostController,
                modifier = modifier, songList = songList,
                albumList = albumList,
                getRecentCount = "${recentList.size} songs",
                favoritesCount = "${favoritesList.size} songs" ,
                onFavoriteClick = {onFavoriteClick(it)},
                onRemoveFavorite = {onRemoveFavorite(it)},
                playLists = playlists,
                onDeletePlaylist = {onDeletePlaylist(it)},
                onAddToPlaylist = {audio,playlist->
                    viewModel.addAudioToPlaylist(audio, playlist)
                }, onItemClick = {list,pos->onItemClick(list,pos)},
                onDelete = {onDelete(it)},
                isRefreshing = isRefreshing,
                onRefresh = {onRefresh()})
        }
        composable(route = Screens.SearchSong.route){_->
            MusicSearch(navHostController = navHostController,
                onAddToPlaylist = {audio,playlist->viewModel.addAudioToPlaylist(audio, playlist)},
                onItemClick = {it,it1->onItemClick(it,it1)},
                onFavoriteClick = {onFavoriteClick(it)},
                playLists = playlists, list = songList,
                onDelete = {onDelete(it)})
        }
        composable(route = Screens.Player.route,
            deepLinks = listOf(navDeepLink { uriPattern="myapp://player_route" })){
            MusicPlayer(modifier = modifier,
                onMinimize = {navHostController.popBackStack()},
                onPreviousClick = onPreviousClick,
                onShuffleClick = onShuffleClick,
                onRepeatClick = onRepeatClick,
                onNextClick = onNextClick,
                onPlayPauseClick = onPlayPauseClick,
                playerViewModel = playerViewModel,
                onPositionChange = {onPositionChange(it)})
        }
        composable(route = Screens.CreatePlaylist.route,
            arguments = listOf(
                navArgument(ARG_3){
                    type= NavType.StringType
                },
                navArgument(ARG_4){
                    type= NavType.StringType
                }
            ) ){navBackStackEntry->
            val age=navBackStackEntry.arguments?.getString(ARG_3).toString()
            val playlistName=navBackStackEntry.arguments?.getString(ARG_4).toString()
            if (age=="new" ||(age=="exists" && playlistName=="Favorites")){
                CreatePlaylist(
                    songList = songList,
                    title = playlistName, modifier = modifier,
                    navHostController = navHostController
                )
            }else{
                val playlist by viewModel.getPlaylistById(playlistName.toInt()).observeAsState()
                CreatePlaylist(
                    songList = songList,
                    currentPlaylist = playlist,
                    title = playlist?.name ?: "", modifier = modifier,
                    navHostController = navHostController
                )
            }
        }
        composable(route = Screens.Details.route,
            arguments = listOf(
                navArgument(ARG_1){
                    type= NavType.StringType
                },
                navArgument(ARG_2){
                    type= NavType.StringType
                }
            ) ){ navBackStackEntry ->
            val type=navBackStackEntry.arguments?.getString(ARG_1).toString()
            val name=navBackStackEntry.arguments?.getString(ARG_2).toString()
            if (type=="album"){
                val detailsList= getSongsForAlbum(songList,name)
                showFloatingButton(false)
                MusicDetails(navHostController = navHostController, modifier = modifier, playLists = playlists,
                    detailsList = detailsList, onFavoriteClick = {onFavoriteClick(it)},
                    onRemoveFavorite ={} , onRemovePlaylistItem = {}, checkScreen = {checkDetailScreen(it,0)},
                    onAddToPlaylist = {it,it1->viewModel.addAudioToPlaylist(it,it1)},
                    onItemClick = {list,pos->onItemClick(list,pos)}, onDelete = {onDelete(it)})
            }else if (type=="playlist" && name=="Recently Added"){
                showFloatingButton(false)
                MusicDetails(navHostController = navHostController, modifier = modifier, detailsList = recentList,
                    title = "Recently Added", onFavoriteClick = {onFavoriteClick(it)}, onRemoveFavorite = {},
                    onRemovePlaylistItem = {}, checkScreen = {checkDetailScreen(it,0)}, playLists = playlists,
                    onAddToPlaylist = {it,it1->viewModel.addAudioToPlaylist(it,it1)},
                    onItemClick = {list,pos->onItemClick(list,pos)}, onDelete = {onDelete(it)})
            }else if (type=="playlist" && name=="Favorites"){
                showFloatingButton(true)
                MusicDetails(navHostController = navHostController, modifier = modifier, detailsList = favoritesList,
                    screenWidget = ScreenWidget.FAVORITE, title = "Favorites", onFavoriteClick = {},
                    onRemoveFavorite = {onRemoveFavorite(it)},
                    onRemovePlaylistItem = {}, checkScreen = {checkDetailScreen(it,0)},
                    onAddToPlaylist = {_,_->},onItemClick = {list,pos->onItemClick(list,pos)}, onDelete = {})
            }else{
                showFloatingButton(true)
                val playlist by viewModel.getPlaylistById(name.toInt()).observeAsState()
                val list=playlist?.songs ?: emptyList()
                MusicDetails(navHostController = navHostController, modifier = modifier,
                    screenWidget = ScreenWidget.PLAYLISTDETAIL, detailsList = list, title = playlist?.name ?: "",
                    onFavoriteClick = {},
                    onRemoveFavorite = {},
                    checkScreen = {checkDetailScreen(it,playlist?.id ?: 0)},
                    onRemovePlaylistItem = { playlist?.let { it1 -> viewModel.removeAudioFromPlaylist(it,it1) } },
                    onAddToPlaylist = {_,_->},onItemClick = {list2,pos->onItemClick(list2,pos)}, onDelete = {})
            }
        }
    }
}