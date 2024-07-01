package com.github.tedblair2.muziki3.features.home.ui

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowCompat
import com.github.tedblair2.muziki3.R
import com.github.tedblair2.muziki3.core.local.model.Audio
import com.github.tedblair2.muziki3.core.local.model.CurrentTheme
import com.github.tedblair2.muziki3.features.album.ui.AlbumsScreen
import com.github.tedblair2.muziki3.features.createplaylist.ui.Age
import com.github.tedblair2.muziki3.features.createplaylist.ui.Parent
import com.github.tedblair2.muziki3.features.details.ui.DetailScreenType
import com.github.tedblair2.muziki3.features.home.ui.tabs.SongsScreen
import com.github.tedblair2.muziki3.features.home.viewmodel.HomeScreenEvents
import com.github.tedblair2.muziki3.features.miniplayer.ui.MiniPlayerScreen
import com.github.tedblair2.muziki3.features.playlists.ui.PlaylistsScreen
import com.github.tedblair2.muziki3.helpers.TabScreens
import com.github.tedblair2.muziki3.helpers.isScrollingUp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeScreenState: HomeScreenState,
    onEvent:(HomeScreenEvents)->Unit={},
    navigateToDetailsScreen:(screenType:DetailScreenType,albumName:String,playlistId:Int)->Unit,
    onNavigateToPlayer:()->Unit={},
    onNavigateToCreatePlaylist:(age: Age, parent: Parent, playlist:Int, title:String)->Unit,
    onNavigateToSearch:()->Unit={}
) {
    val pagerState= rememberPagerState { TabScreens.entries.size }
    val scope= rememberCoroutineScope()
    val view=LocalView.current
    val window=(view.context as Activity).window
    val mainColor=MaterialTheme.colorScheme.surfaceVariant.toArgb()
    var showDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember {
        mutableStateOf(false)
    }
    val lazyListState=rememberLazyListState()
    val isDark= isSystemInDarkTheme()
    val themeIcon=when(homeScreenState.currentTheme){
        CurrentTheme.LIGHT_THEME->{
            painterResource(id=R.drawable.baseline_sunny_24)
        }
        CurrentTheme.DARK_THEME->{
            painterResource(id=R.drawable.baseline_dark_mode_24)
        }
        else->{
            if (isDark) painterResource(id=R.drawable.baseline_dark_mode_24) else painterResource(
                id=R.drawable.baseline_sunny_24
            )
        }
    }

    val statusBarState=when(homeScreenState.currentTheme){
        CurrentTheme.LIGHT_THEME->true
        CurrentTheme.DARK_THEME->false
        else->!isDark
    }

    if (!view.isInEditMode){
        SideEffect {
            window.statusBarColor=mainColor
            window.navigationBarColor=mainColor
            WindowCompat.getInsetsController(window,view).isAppearanceLightStatusBars=statusBarState
            WindowCompat.getInsetsController(window,view).isAppearanceLightNavigationBars=statusBarState
        }
    }

    LaunchedEffect(key1 = pagerState.currentPage) {
        val screen= TabScreens.entries[pagerState.currentPage]
        onEvent(HomeScreenEvents.CurrentTabScreen(screen))
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title={
                    Text(text=stringResource(id=R.string.title))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                actions = {
                    AnimatedVisibility(visible = homeScreenState.currentTab==TabScreens.SONGS) {
                        IconButton(onClick = { onNavigateToSearch() }) {
                            Icon(imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                        }
                    }
                    IconButton(onClick = { showThemeDialog=true }) {
                        Icon(painter = themeIcon,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = homeScreenState.currentTab==TabScreens.PLAYLISTS && lazyListState.isScrollingUp()
            ) {

                val customModifier=if (homeScreenState.isMiniPlayerVisible) Modifier.padding(bottom = 75.dp)
                    else Modifier

                FloatingActionButton(
                    onClick = { showDialog=true },
                    modifier = customModifier
                ) {
                    Icon(imageVector = Icons.Default.Add ,
                        contentDescription = null
                    )
                }
            }
        },
    ) {paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)){
            Column(modifier = Modifier
                .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                TabRow(selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicator = {tabPositions ->
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                            color = MaterialTheme.colorScheme.primary,
                            width = 65.dp,
                            shape = RoundedCornerShape(9.dp)
                        )
                    },
                    divider = {} ) {
                    TabScreens.entries.forEachIndexed { index , tabScreen ->
                        Tab(selected = pagerState.currentPage == index,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = { Text(text = tabScreen.name) },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) {page->
                    when(page){
                        0->{
                            SongsScreen()
                        }
                        1->{
                            AlbumsScreen(
                                navigateToDetailsScreen = navigateToDetailsScreen
                            )
                        }
                        2->{
                            PlaylistsScreen(
                                navigateToDetailsScreen = navigateToDetailsScreen,
                                lazyListState = lazyListState
                            )
                        }
                    }
                }

                MiniPlayerScreen(
                    onNavigateToPlayer = onNavigateToPlayer
                )
            }

            if (showDialog){
                AlertCreatePlaylist(
                    dismissAlert = { showDialog=false },
                    onCreate = {
                        showDialog=false
                        onNavigateToCreatePlaylist(Age.NEW,Parent.PLAYLIST,0,it)
                    }
                )
            }

            if (showThemeDialog){
                ThemeSelectionDialog(
                    onDismissRequest={ showThemeDialog=false } ,
                    currentTheme= homeScreenState.currentTheme,
                    onThemeClick = {
                        onEvent(HomeScreenEvents.OnThemeSelection(it))
                    }
                )
            }
        }
    }
}

@Composable
fun NoMediaScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {

        Image(painter = painterResource(id = R.drawable.folder) ,
            contentDescription = null,
            modifier = Modifier
                .size(100.dp))
        Text(text = "No Results!" , fontSize = 25.sp)
    }
}

@Composable
fun AlertDialogExample(
    onDismissRequest: () -> Unit ,
    onConfirmation: () -> Unit ,
    dialogTitle: String ,
    dialogText: String ,
    icon: ImageVector ,
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = "Example Icon")
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun AlertCreatePlaylist(
    modifier: Modifier = Modifier,
    dismissAlert:()->Unit,
    onCreate:(String)->Unit
) {
    var text by remember { mutableStateOf("") }
    val focusRequester= remember { FocusRequester() }

    Dialog(onDismissRequest = { dismissAlert() }) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(20.dp),
            shape = RoundedCornerShape(15.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {

                Text(
                    text = "New Playlist",
                    fontSize = 22.sp,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    textAlign = TextAlign.Start
                )

                TextField(
                    value = text,
                    onValueChange ={text=it},
                    modifier = Modifier
                        .padding(12.dp)
                        .focusRequester(focusRequester)
                        .onGloballyPositioned { focusRequester.requestFocus() },
                    singleLine = true,
                    placeholder = { Text(text = "Enter playlist name",
                        modifier = Modifier.alpha(0.5f))})

                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { dismissAlert() }, modifier = Modifier.padding(8.dp)) {
                        Text(text = "Cancel")
                    }
                    Button(onClick = { if (text != "") onCreate(text) },modifier = Modifier.padding(8.dp)) {
                        Text(text = "Create")
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteAudioWrapper(
    audioToDelete:Audio,
    isDelete:Boolean=false,
    isVersionQ:()->Unit,
    onSuccessfulDelete:()->Unit,
    unSuccessfulDelete:()->Unit,
    content: @Composable ()->Unit
) {
    val context=LocalContext.current

    val deleteAudioLauncher=rememberLauncherForActivityResult(contract=ActivityResultContracts.StartIntentSenderForResult()) {
        if (it.resultCode==RESULT_OK){
            onSuccessfulDelete()
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q){
                isVersionQ()
            }
            Toast.makeText(context, "File deleted successfully", Toast.LENGTH_LONG).show()
        }else{
            unSuccessfulDelete()
            Toast.makeText(context, "File cannot be deleted", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(key1=isDelete) {
        if (isDelete){
            val mediaStoreUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            val uri= ContentUris.withAppendedId(mediaStoreUri,audioToDelete.id.toLong())

            try {
                context.contentResolver.delete(uri,null,null)
                Toast.makeText(context, "File deleted successfully", Toast.LENGTH_LONG).show()
                onSuccessfulDelete()
            }catch (e:SecurityException){
                val intentSender=when{
                    Build.VERSION.SDK_INT>=Build.VERSION_CODES.R ->{
                        MediaStore.createDeleteRequest(context.contentResolver, listOf(uri)).intentSender
                    }
                    Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q ->{
                        val recoverableSecurityException=e as? RecoverableSecurityException
                        recoverableSecurityException?.userAction?.actionIntent?.intentSender
                    }
                    else->null
                }
                intentSender?.let { sender->
                    deleteAudioLauncher.launch(
                        IntentSenderRequest.Builder(sender).build()
                    )
                }
            }
        }
    }

    content()
}

@Composable
fun ThemeSelectionDialog(
    modifier: Modifier=Modifier,
    onDismissRequest: () -> Unit,
    currentTheme: CurrentTheme,
    onThemeClick:(CurrentTheme)->Unit
) {
    val themeListMap=remember {
        linkedMapOf(
            CurrentTheme.SYSTEM_THEME to "Use System Theme",
            CurrentTheme.LIGHT_THEME to "Light Theme",
            CurrentTheme.DARK_THEME to "Dark Theme"
        )
    }

    Dialog(onDismissRequest = { onDismissRequest() }){
        Card(modifier = modifier
            .fillMaxWidth()
            .height(290.dp)
            .padding(30.dp),
            shape = RoundedCornerShape(6.dp)
        ){
            Text(
                text = "Select App Theme",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                modifier = Modifier.padding(10.dp)
            )

            themeListMap.forEach {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onThemeClick(it.key)
                        onDismissRequest()
                    }
                    .padding(horizontal = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    RadioButton(selected = it.key==currentTheme, onClick = {
                        onThemeClick(it.key)
                        onDismissRequest()
                    })
                    Text(
                        text = it.value,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 5.dp)
                    )
                }
            }
        }
    }
}



