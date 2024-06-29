package com.github.tedblair2.muziki3

import android.Manifest
import android.app.AlertDialog
import android.content.ComponentName
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.github.tedblair2.muziki3.core.local.AppCoroutineContext
import com.github.tedblair2.muziki3.core.local.MusicService
import com.github.tedblair2.muziki3.core.local.RoomRepository
import com.github.tedblair2.muziki3.core.local.datastore.DataPrefService
import com.github.tedblair2.muziki3.core.local.model.Audio
import com.github.tedblair2.muziki3.core.local.model.AudioRecent
import com.github.tedblair2.muziki3.core.local.model.CurrentTheme
import com.github.tedblair2.muziki3.data.redux.AppState
import com.github.tedblair2.muziki3.data.redux.Dispatch
import com.github.tedblair2.muziki3.data.redux.InitialEvents
import com.github.tedblair2.muziki3.data.redux.MiddleWare
import com.github.tedblair2.muziki3.data.redux.Reducer
import com.github.tedblair2.muziki3.data.redux.Store
import com.github.tedblair2.muziki3.data.service.MediaPlayerService
import com.github.tedblair2.muziki3.features.player.ui.PlayerScreenState
import com.github.tedblair2.muziki3.features.player.ui.RepeatMode
import com.github.tedblair2.muziki3.features.player.viewmodel.PlayerEvents
import com.github.tedblair2.muziki3.helpers.formatTime
import com.github.tedblair2.muziki3.ui.theme.Muziki3Theme
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val permissions=if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_AUDIO,Manifest.permission.POST_NOTIFICATIONS)
    } else {
        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
    @Inject
    lateinit var store: Store
    @Inject
    lateinit var appCoroutineContext: AppCoroutineContext
    @Inject
    lateinit var musicService: MusicService

    private var dispatch:Dispatch?=null

    private var playerProgressUpdate=true

    @Inject
    lateinit var dataPrefService: DataPrefService

    @Inject
    lateinit var roomRepository: RoomRepository

    private lateinit var controllerFuture: ListenableFuture<MediaController>

    private var controller:MediaController?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val currentTheme by dataPrefService.getCurrentTheme().collectAsStateWithLifecycle(
                initialValue= CurrentTheme.SYSTEM_THEME
            )

            val darkTheme=when(currentTheme){
                CurrentTheme.SYSTEM_THEME->isSystemInDarkTheme()
                CurrentTheme.LIGHT_THEME->false
                CurrentTheme.DARK_THEME->true
            }

            Muziki3Theme(
                darkTheme = darkTheme
            ) {
                MainApp(
                    modifier = Modifier.fillMaxSize())
            }
        }

        store.getCurrentState { dispatch=it }

        val playerReducer:Reducer<PlayerScreenState> = {old, action ->
            when(action){
                is PlayerEvents.SetSongName->old.copy(songName = action.name)
                is PlayerEvents.SetSongArtist->old.copy(songArtist = action.artist)
                is PlayerEvents.SetIsPlaying->old.copy(isPlaying = action.isPlaying)
                is PlayerEvents.SetShuffle->old.copy(shuffle = action.isShuffle)
                is PlayerEvents.SetCurrentPosition->old.copy(currentPosition = action.position, currentPositionString = formatTime(action.position.toLong()))
                is PlayerEvents.SetSongDuration->old.copy(songDuration = action.duration)
                is PlayerEvents.SetRepeatMode->old.copy(repeatMode = action.repeatMode)
                is PlayerEvents.SetSongImage->old.copy(songImage = action.image)
                is PlayerEvents.MiniPlayerVisibility->old.copy(isMiniPlayerVisible = action.visibility)
                else->old
            }
        }

        val appReducer:Reducer<AppState> = {old, action ->
            when(action){
                is InitialEvents.Songs->{
                    old.copy(songs = action.songs,isLoading = false, permissionGranted = action.permissionGranted)
                }
                else->old.copy(playerScreenState = playerReducer(old.playerScreenState,action))
            }
        }

        val playerMiddleWare:MiddleWare = {state, action, dispatch, next ->
            when(action){
                is PlayerEvents.OnAudioClick->{
                    kotlin.run {
                        val player = this.controller ?: return@run
                        player.setMediaItems(setMediaItems(action.songs), action.position, 0)
                        player.prepare()
                        player.play()
                    }
                    action
                }
                is PlayerEvents.OnNext->{
                    kotlin.run {
                        val player = this.controller ?: return@run
                        if (player.hasNextMediaItem()){
                            player.seekToNext()
                        }
                    }
                    action
                }
                is PlayerEvents.OnPrevious->{
                    kotlin.run {
                        val player = this.controller ?: return@run
                        if (player.hasPreviousMediaItem()) {
                            player.seekToPrevious()
                        }
                    }
                    action
                }
                is PlayerEvents.OnPlayPause->{
                    kotlin.run {
                        val player = this.controller ?: return@run
                        if (player.isPlaying) {
                            player.pause()
                        } else {
                            player.play()
                        }
                    }
                    action
                }
                is PlayerEvents.OnShuffleClick->{
                    kotlin.run {
                        val player = this.controller ?: return@run
                        player.shuffleModeEnabled = !player.shuffleModeEnabled
                    }
                    action
                }
                is PlayerEvents.OnRepeatClick->{
                    kotlin.run {
                        val player = this.controller ?: return@run
                        when(player.repeatMode){
                            ExoPlayer.REPEAT_MODE_OFF->player.repeatMode=ExoPlayer.REPEAT_MODE_ALL
                            ExoPlayer.REPEAT_MODE_ALL->player.repeatMode=ExoPlayer.REPEAT_MODE_ONE
                            ExoPlayer.REPEAT_MODE_ONE->player.repeatMode=ExoPlayer.REPEAT_MODE_OFF
                        }
                    }
                    action
                }
                is PlayerEvents.OnPositionChange->{
                    kotlin.run {
                        val player = this.controller ?: return@run
                        player.seekTo(action.position)
                    }
                    action
                }
                is PlayerEvents.OnDelete->{
                    action
                }
                is PlayerEvents.StartStopProgress->{
                    playerProgressUpdate=action.update
                    action
                }
                else->next(state,action,dispatch)
            }
        }

        val appMiddleWare:MiddleWare = {state, action, dispatch, next ->
            when(action){
                is InitialEvents.GetSongs->{
                    lifecycleScope.launch {
                        musicService.getSongs(action.permissionGranted).collect{
                            dispatch(InitialEvents.Songs(it,action.permissionGranted))
                        }
                    }
                    action
                }
                else->playerMiddleWare(state,action,dispatch,next)
            }
        }
        store.applyReducer(appReducer)
            .applyMiddleWare(appMiddleWare)

        requestPermissions()
    }

    @OptIn(UnstableApi::class)
    override fun onStart() {
        super.onStart()
        val sessionToken=SessionToken(this, ComponentName(this, MediaPlayerService::class.java))
        controllerFuture=MediaController.Builder(this,sessionToken).buildAsync()
        controllerFuture.addListener({addPlayerListener()},ContextCompat.getMainExecutor(this))
    }

    private fun addPlayerListener() {
        controller=try {
            controllerFuture.get()
        }catch (e:Exception){
            null
        }
        val player=this.controller ?: return

        if (player.playbackState==ExoPlayer.STATE_IDLE){
            lifecycleScope.launch {
                val pos=dataPrefService.getLastPosition().first()
                val songs=roomRepository.getAudioRecentList().first()
                if (songs.isNotEmpty()){
                    player.clearMediaItems()
                    val mediaItems=setMediaItems(songs.map { it.toAudio() })
                    if (pos<songs.size){
                        player.setMediaItems(mediaItems,pos.toInt(),0)
                    }else{
                        player.setMediaItems(mediaItems,0,0)
                    }
                    player.prepare()
                    dispatch?.invoke(PlayerEvents.MiniPlayerVisibility(true))
                }else{
                    dispatch?.invoke(PlayerEvents.MiniPlayerVisibility(false))
                }
            }
        }
        val songName=(player.currentMediaItem?.mediaMetadata?.title ?: "Current Song").toString()
        if (songName != "Current Song"){
            dispatch?.invoke(PlayerEvents.MiniPlayerVisibility(true))
        }else{
            dispatch?.invoke(PlayerEvents.MiniPlayerVisibility(false))
        }
        dispatch?.invoke(PlayerEvents.SetSongName(songName))
        dispatch?.invoke(PlayerEvents.SetIsPlaying(player.isPlaying))
        dispatch?.invoke(PlayerEvents.SetSongArtist((player.currentMediaItem?.mediaMetadata?.artist ?: "Artist").toString()))
        dispatch?.invoke(PlayerEvents.SetSongImage(player.currentMediaItem?.mediaMetadata?.artworkData))
        dispatch?.invoke(PlayerEvents.SetShuffle(player.shuffleModeEnabled))
        if (player.duration>0){
            dispatch?.invoke(PlayerEvents.SetSongDuration(player.duration.toFloat()))
        }else{
            dispatch?.invoke(PlayerEvents.SetSongDuration(0f))
        }
        when(player.repeatMode){
            ExoPlayer.REPEAT_MODE_ALL->dispatch?.invoke(PlayerEvents.SetRepeatMode(RepeatMode.REPEAT_ALL))
            ExoPlayer.REPEAT_MODE_OFF->dispatch?.invoke(PlayerEvents.SetRepeatMode(RepeatMode.REPEAT_OFF))
            ExoPlayer.REPEAT_MODE_ONE->dispatch?.invoke(PlayerEvents.SetRepeatMode(RepeatMode.REPEAT_ONE))
        }
        updatePlayerProgress(player)

        player.addListener(object : Player.Listener{
            override fun onMediaItemTransition(mediaItem: MediaItem? , reason: Int) {
                super.onMediaItemTransition(mediaItem , reason)
                dispatch?.invoke(PlayerEvents.SetSongName(mediaItem?.mediaMetadata?.title.toString()))
                dispatch?.invoke(PlayerEvents.SetSongArtist((mediaItem?.mediaMetadata?.artist.toString())))
                dispatch?.invoke(PlayerEvents.SetSongImage(mediaItem?.mediaMetadata?.artworkData))
                updatePlayerProgress(player)
                if (player.duration>0){
                    dispatch?.invoke(PlayerEvents.SetSongDuration(player.duration.toFloat()))
                }else{
                    dispatch?.invoke(PlayerEvents.SetSongDuration(0f))
                }
                mediaItem?.mediaMetadata?.title?.let {
                    dispatch?.invoke(PlayerEvents.MiniPlayerVisibility(true))
                }
                setLastPosition(player.currentMediaItemIndex)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState==ExoPlayer.STATE_READY){
                    dispatch?.invoke(PlayerEvents.SetSongName((player.currentMediaItem?.mediaMetadata?.title ?: "Song Name").toString()))
                    dispatch?.invoke(PlayerEvents.SetSongArtist((player.currentMediaItem?.mediaMetadata?.artist ?: "").toString()))
                    dispatch?.invoke(PlayerEvents.SetSongImage(player.currentMediaItem?.mediaMetadata?.artworkData))
                    if (player.duration>0){
                        dispatch?.invoke(PlayerEvents.SetSongDuration(player.duration.toFloat()))
                    }else{
                        dispatch?.invoke(PlayerEvents.SetSongDuration(0f))
                    }
                    updatePlayerProgress(player)
                    dispatch?.invoke(PlayerEvents.MiniPlayerVisibility(true))
                }else if (playbackState==ExoPlayer.STATE_ENDED){
                    setLastPosition(player.currentMediaItemIndex)
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                dispatch?.invoke(PlayerEvents.SetIsPlaying(isPlaying))
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                super.onRepeatModeChanged(repeatMode)
                when(repeatMode){
                    ExoPlayer.REPEAT_MODE_ALL->dispatch?.invoke(PlayerEvents.SetRepeatMode(RepeatMode.REPEAT_ALL))
                    ExoPlayer.REPEAT_MODE_OFF->dispatch?.invoke(PlayerEvents.SetRepeatMode(RepeatMode.REPEAT_OFF))
                    ExoPlayer.REPEAT_MODE_ONE->dispatch?.invoke(PlayerEvents.SetRepeatMode(RepeatMode.REPEAT_ONE))
                }
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                super.onShuffleModeEnabledChanged(shuffleModeEnabled)
                dispatch?.invoke(PlayerEvents.SetShuffle(shuffleModeEnabled))
            }
        })
    }

    private fun updatePlayerProgress(player:MediaController){
        lifecycleScope.launch {
            delay(1000)
            if (player.isPlaying && playerProgressUpdate){
                dispatch?.invoke(PlayerEvents.SetCurrentPosition(player.currentPosition.toFloat()))
            }
            updatePlayerProgress(player)
        }
    }

    private fun setLastPosition(position:Int){
        lifecycleScope.launch {
            dataPrefService.setLastPosition(position.toLong())
        }
    }

    private suspend fun AudioRecent.toAudio():Audio{
        val artWork=getSongImage(path)
        return Audio(
            id = id,
            name = name,
            album = album,
            artist = artist,
            path = path,
            duration = duration,
            dateAdded = dateAdded,
            artWork = artWork
        )
    }

    private suspend fun getSongImage(uri:String):ByteArray?= withContext(appCoroutineContext.io){
        val retriever= MediaMetadataRetriever()
        try {
            retriever.setDataSource(uri)
            retriever.embeddedPicture
        }catch (e:Exception){
            e.printStackTrace()
            null
        }finally {
            retriever.release()
        }
    }

    private fun setMediaItems(songs:List<Audio>):List<MediaItem>{
        return songs.map {
            MediaItem.Builder()
                .setMediaMetadata(getMediaMetadata(it))
                .setUri(Uri.parse(it.path))
                .build()
        }
    }

    private fun getMediaMetadata(song: Audio): MediaMetadata {
        return MediaMetadata.Builder()
            .setTitle(song.name)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .setArtworkUri(Uri.parse(song.path))
            .setArtworkData(song.artWork,MediaMetadata.PICTURE_TYPE_FILE_ICON)
            .build()
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionMap ->
        val isGranted=if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionMap[Manifest.permission.READ_MEDIA_AUDIO]?:false
        }else{
            permissionMap[Manifest.permission.WRITE_EXTERNAL_STORAGE]?:false
        }
        dispatch?.invoke(InitialEvents.GetSongs(isGranted))
        if (!isGranted) {
            requestPermissions()
        }
    }

    private fun requestPermissions(){
        val notGrantedPermissions=permissions.filterNot {permission->
            ContextCompat.checkSelfPermission(this,permission)== PackageManager.PERMISSION_GRANTED
        }
        if (notGrantedPermissions.isNotEmpty()){
            val showRational= notGrantedPermissions.any {permission->
                ActivityCompat.shouldShowRequestPermissionRationale(this,permission)
            }
            if (showRational){
                AlertDialog.Builder(this)
                    .setTitle("App Permissions")
                    .setMessage("In order to ensure a smooth experience while using this application, please grant the asked permissions.")
                    .setNegativeButton("Cancel"){dialog,_->
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                        dispatch?.invoke(InitialEvents.GetSongs(false))
                        dialog.dismiss()
                    }
                    .setPositiveButton("OK"){dialog,_->
                        dialog.dismiss()
                        permissionLauncher.launch(notGrantedPermissions.toTypedArray())
                    }
                    .show()
            }else{
                permissionLauncher.launch(notGrantedPermissions.toTypedArray())
            }
        }else{
            dispatch?.invoke(InitialEvents.GetSongs(true))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MediaController.releaseFuture(controllerFuture)
        dispatch=null
    }
}
