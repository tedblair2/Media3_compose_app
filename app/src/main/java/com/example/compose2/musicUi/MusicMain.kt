package com.example.compose2.musicUi

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.core.view.ViewCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.compose2.R
import com.example.compose2.model.Audio
import com.example.compose2.model.ScreenWidget
import com.example.compose2.model.ThemeSelection
import com.example.compose2.routes.NavGraphSetUp
import com.example.compose2.routes.Screens
import com.example.compose2.viewmodel.MuzikiViewModel
import com.example.compose2.viewmodel.PlayerViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicMain(navHostController: NavHostController,
              viewModel: MuzikiViewModel= koinViewModel(),
              playerViewModel: PlayerViewModel= koinViewModel(),
              currentTheme:String=ThemeSelection.SYSTEM_THEME.name,
              onItemClick: (List<Audio>, Int) -> Unit,
              onPreviousClick: () -> Unit,
              onPlayPauseClick: () -> Unit,
              onNextClick: () -> Unit,
              onShuffleClick:()->Unit,
              onRepeatClick:()->Unit,
              onPositionChange:(Float)->Unit,
              onDelete: (Audio) -> Unit,
              isRefreshing: Boolean=false,
              onRefresh: () -> Unit) {
    val navBackStackEntry by navHostController.currentBackStackEntryAsState()
    val currentDestination=navBackStackEntry?.destination
    val isDetailScreen=currentDestination?.hierarchy?.any { it.route == Screens.Details.route } ?: false
    val isPlayerScreen=currentDestination?.hierarchy?.any { it.route ==Screens.Player.route } ?: false
    val isHomeScreen=currentDestination?.hierarchy?.any { it.route == Screens.Home.route } ?: false
    val isCreateScreen=currentDestination?.hierarchy?.any { it.route == Screens.CreatePlaylist.route } ?: false
    val isSearchScreen=currentDestination?.hierarchy?.any { it.route == Screens.SearchSong.route } ?: false
    val view = LocalView.current
    val mainColor=MaterialTheme.colorScheme.surfaceVariant.toArgb()
    val playerColor=Color.DarkGray.toArgb()
    val isDark= isSystemInDarkTheme()
    var showFloatingButton by remember { mutableStateOf(false) }
    val songList=viewModel.allSongs.observeAsState()
    val albumList=viewModel.albums.observeAsState()
    val recentList=viewModel.recentSongs.observeAsState()
    val favoriteList by viewModel.favorites.observeAsState()
    val playLists by viewModel.playlists.observeAsState()
    var checkDetailScreen by remember { mutableStateOf(ScreenWidget.MAIN) }
    var playListId by remember { mutableStateOf(0) }
    val showMiniPlayer=viewModel.showMiniPlayer
    var showAlert by rememberSaveable { mutableStateOf(false) }
    val themeIcon=when(currentTheme){
        ThemeSelection.DARK_THEME.name->painterResource(id = R.drawable.ic_dark_theme)
        ThemeSelection.LIGHT_THEME.name-> painterResource(id = R.drawable.ic_light_theme)
        else->{
            if (isDark) painterResource(id = R.drawable.ic_dark_theme) else painterResource(id = R.drawable.ic_light_theme)
        }
    }
    val themeState=when(currentTheme){
        ThemeSelection.DARK_THEME.name->false
        ThemeSelection.LIGHT_THEME.name-> true
        else->!isDark
    }
    if (!view.isInEditMode) {
        SideEffect {
            if (isPlayerScreen){
                (view.context as Activity).window.statusBarColor = playerColor
                (view.context as Activity).window.navigationBarColor=playerColor
                ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars =false
                ViewCompat.getWindowInsetsController(view)?.isAppearanceLightNavigationBars=false
            }else{
                (view.context as Activity).window.statusBarColor = mainColor
                (view.context as Activity).window.navigationBarColor=mainColor
                ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars =themeState
                ViewCompat.getWindowInsetsController(view)?.isAppearanceLightNavigationBars=themeState
            }
        }
    }
    
    Scaffold(topBar = {
                      AnimatedVisibility(visible = isHomeScreen) {
                          TopAppBar(
                              title = { Text(text = "Muziki")},
                              actions = {
                                  IconButton(onClick = { navHostController.navigate(Screens.SearchSong.route) }) {
                                      Icon(imageVector = Icons.Default.Search,
                                          contentDescription ="search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                  }
                                  IconButton(onClick = { showAlert=true }) {
                                      Icon(painter =themeIcon , contentDescription ="theme change",
                                          tint =MaterialTheme.colorScheme.onSurfaceVariant)
                                  }
                              }, colors = TopAppBarDefaults.topAppBarColors(
                                  containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                  titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                              ))
                      }
    },
        floatingActionButton = {
            AnimatedVisibility(visible =isDetailScreen && showFloatingButton) {
                FloatingActionButton(onClick = {
                    if (checkDetailScreen==ScreenWidget.FAVORITE){
                        navHostController.navigate(Screens.CreatePlaylist.passParams("exists","Favorite"))
                    }else if (checkDetailScreen==ScreenWidget.PLAYLISTDETAIL){
                        navHostController.navigate(Screens.CreatePlaylist.passParams("exists",playListId.toString()))
                    }
                }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(visible = !isPlayerScreen && !isCreateScreen && showMiniPlayer && !isSearchScreen) {
                MiniPlayer(onMiniPlayerClick = {
                    navHostController.navigate(Screens.Player.route)
                }, onPreviousClick = onPreviousClick,
                    onPlayPauseClick = onPlayPauseClick,
                    onNextClick = onNextClick, playerViewModel = playerViewModel)
            }
        }
    ) {contentPadding->
        NavGraphSetUp(navHostController = navHostController,
            modifier = Modifier.padding(contentPadding),
            songList = songList.value ?: ArrayList(),
            albumList = albumList.value ?: ArrayList(),
            recentList = recentList.value ?: ArrayList(),
            favoritesList = favoriteList ?: emptyList(),
            playlists = playLists ?: emptyList(),
            showFloatingButton = {showFloatingButton=it},
            onFavoriteClick = {viewModel.addFavorite(it)},
            onRemoveFavorite ={viewModel.deleteFavorite(it)},
            onDeletePlaylist = {viewModel.deletePlaylist(it)},
            checkDetailScreen = {screen,id->
                checkDetailScreen=screen
                playListId=id
            },
            onItemClick = {list,pos-> onItemClick(list,pos)},
            onNextClick = onNextClick,
            onRepeatClick = onRepeatClick,
            onPlayPauseClick = onPlayPauseClick,
            onShuffleClick = onShuffleClick,
            onPreviousClick = onPreviousClick,
            playerViewModel = playerViewModel,
            onPositionChange = {onPositionChange(it)},
            onDelete = {onDelete(it)},
            isRefreshing = isRefreshing,
            onRefresh = onRefresh
        )

        if (showAlert){
            ThemeSelectionDialog(dismissAlert = {showAlert=false},
                onThemeClick = {
                    viewModel.setCurrentTheme(it)
                }, currentTheme = currentTheme)
        }
    }
}
