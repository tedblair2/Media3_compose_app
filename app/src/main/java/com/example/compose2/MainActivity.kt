package com.example.compose2

import android.Manifest.permission.*
import android.app.RecoverableSecurityException
import android.content.ComponentName
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.compose.rememberNavController
import com.example.compose2.model.Audio
import com.example.compose2.model.RepeatMode
import com.example.compose2.musicUi.MusicMain
import com.example.compose2.musicUi.formatTime
import com.example.compose2.musicUi.getAlbumArt
import com.example.compose2.services.MediaPlayerService
import com.example.compose2.ui.theme.Compose2Theme
import com.example.compose2.viewmodel.MuzikiViewModel
import com.example.compose2.viewmodel.PlayerViewModel
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "position")
@UnstableApi class MainActivity : ComponentActivity() {
    private val write_storage=WRITE_EXTERNAL_STORAGE
    private val read_storage=READ_MEDIA_AUDIO
    private var post_notifications=POST_NOTIFICATIONS
    private val permissions= arrayOf(read_storage,post_notifications)
    private val viewModel by viewModel<MuzikiViewModel>()
    private val playerViewModel by viewModel<PlayerViewModel>()
    private lateinit var controllerFuture:ListenableFuture<MediaController>
    private val controller:MediaController?
        get() = if (controllerFuture.isDone) controllerFuture.get() else null
    private var audioToDelete:Audio?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Compose2Theme {
                val navHostController= rememberNavController()
                val scope= rememberCoroutineScope()
                var refresh by remember { mutableStateOf(false) }
                requestPermission()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MusicMain(
                        navHostController = navHostController,
                        onItemClick = { list, pos ->
                            kotlin.run {
                                val player = this.controller ?: return@run
                                viewModel.addRecentPlaylist(list)
                                viewModel.setLastPosition(pos)
                                player.setMediaItems(setMediaItems(list), pos, 0)
                                player.prepare()
                                player.play()
                                viewModel.setMiniPlayerVisibility(true)
                            }
                        },
                        onPreviousClick = {
                            kotlin.run {
                                val player = this.controller ?: return@run
                                if (player.hasPreviousMediaItem()) {
                                    player.seekToPrevious()
                                }
                            }
                        },
                        onPlayPauseClick = {
                            kotlin.run {
                                val player = this.controller ?: return@run
                                if (player.isPlaying) {
                                    player.pause()
                                } else {
                                    player.play()
                                }
                            }
                        },
                        onNextClick = {
                            kotlin.run {
                                val player = this.controller ?: return@run
                                if (player.hasNextMediaItem()) {
                                    player.seekToNext()
                                }
                            }
                        },
                        onShuffleClick = {
                            kotlin.run {
                                val player = this.controller ?: return@run
                                player.shuffleModeEnabled = !player.shuffleModeEnabled
                            }
                        },
                        onRepeatClick = {
                            kotlin.run {
                                val player = this.controller ?: return@run
                                when(player.repeatMode){
                                    ExoPlayer.REPEAT_MODE_OFF->player.repeatMode=ExoPlayer.REPEAT_MODE_ALL
                                    ExoPlayer.REPEAT_MODE_ALL->player.repeatMode=ExoPlayer.REPEAT_MODE_ONE
                                    ExoPlayer.REPEAT_MODE_ONE->player.repeatMode=ExoPlayer.REPEAT_MODE_OFF
                                }
                            }
                        },
                        onPositionChange = {newPosition->
                            kotlin.run {
                                val player = this.controller ?: return@run
                                player.seekTo(newPosition.toLong())
                            }
                        },
                        onDelete = {
                            android.app.AlertDialog.Builder(this)
                                .setTitle("Delete Request")
                                .setMessage("Are you sure you want to delete ${it.name}?")
                                .setNegativeButton("No"){dialog,_->
                                    dialog.dismiss()
                                }
                                .setPositiveButton("Yes"){_,_->
                                    deleteAudio(it)
                                }
                                .show()
                        }, isRefreshing = refresh,
                        onRefresh = {
                            scope.launch {
                                refresh=true
                                requestPermission()
                                delay(2000)
                                refresh=false
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(this, ComponentName(this, MediaPlayerService::class.java))
        controllerFuture=MediaController.Builder(this,sessionToken).buildAsync()
        controllerFuture.addListener({addPlayerListiner()},ContextCompat.getMainExecutor(this))
    }

    override fun onStop() {
        super.onStop()
        MediaController.releaseFuture(controllerFuture)
    }
    private fun removeAudioFileFromPlayer(audio: Audio){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.recentPlaylist.collect{list->
                    val audioList=list.map { it.audioRecentToAudio() }
                    audioList.forEachIndexed { index, audio2 ->
                        if (audio2.id==audio.id){
                            removeMediaItemFromList(index)
                        }
                    }
                }
            }
        }
    }
    private fun removeMediaItemFromList(position:Int){
        kotlin.run {
            val player = this.controller ?: return@run
            player.removeMediaItem(position)
        }
    }
    private val intentSenderLauncher=registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()){
        if (it.resultCode== RESULT_OK){
            viewModel.deleteAudioFromPlaylistAndFavorite(audioToDelete!!)
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q){
                deleteAudio(audioToDelete!!)
            }
            requestPermission()
            removeAudioFileFromPlayer(audioToDelete!!)
            Toast.makeText(this, "File deleted successfully", Toast.LENGTH_LONG).show()
        }else{
            Toast.makeText(this, "File cannot be deleted", Toast.LENGTH_LONG).show()
        }
    }
    private fun deleteAudio(audio: Audio){
        val uri= ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,audio.id.toLong())
        audioToDelete=audio
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                contentResolver.delete(uri,null,null)
                viewModel.deleteAudioFromPlaylistAndFavorite(audio)
                requestPermission()
                removeAudioFileFromPlayer(audio)
                Toast.makeText(this@MainActivity, "File deleted successfully", Toast.LENGTH_LONG).show()
            }catch (e:SecurityException){
                val intentSender=when{
                    Build.VERSION.SDK_INT>=Build.VERSION_CODES.R ->{
                        MediaStore.createDeleteRequest(contentResolver, listOf(uri)).intentSender
                    }
                    Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q ->{
                        val recoverableSecurityException=e as? RecoverableSecurityException
                        recoverableSecurityException?.userAction?.actionIntent?.intentSender
                    }
                    else->null
                }
                intentSender?.let { sender->
                    intentSenderLauncher.launch(
                        IntentSenderRequest.Builder(sender).build()
                    )
                }
            }
        }
    }
    private fun addPlayerListiner(){
        val player=this.controller ?: return
        if (player.playbackState == ExoPlayer.STATE_IDLE){
            lifecycleScope.launch {
                val pos=viewModel.getLastPosition().first()
                val list=viewModel.recentPlaylist.first()
                if (list.isEmpty()){
                    viewModel.setMiniPlayerVisibility(false)
                }else{
                    player.clearMediaItems()
                    val mediaItems=setMediaItems(list.map { it.audioRecentToAudio() })
                    player.setMediaItems(mediaItems,pos,0)
                    player.prepare()
                    viewModel.setMiniPlayerVisibility(true)
                }
            }
        }
        playerViewModel.setIsPlayingValue(player.isPlaying)
        playerViewModel.setSongTitle((player.currentMediaItem?.mediaMetadata?.title ?: "Current Song").toString())
        playerViewModel.setSongArtistTitle((player.currentMediaItem?.mediaMetadata?.artist ?: "").toString())
        playerViewModel.setImgUri(player.currentMediaItem?.mediaMetadata?.artworkData)

        if (player.duration>0){
            playerViewModel.setSongDurationValue(player.duration.toFloat())
        }else{
            playerViewModel.setSongDurationValue(0f)
        }
        when(player.repeatMode){
            ExoPlayer.REPEAT_MODE_ALL->playerViewModel.setRepeatMode(RepeatMode.REPEAT_ALL)
            ExoPlayer.REPEAT_MODE_OFF->playerViewModel.setRepeatMode(RepeatMode.REPEAT_OFF)
            ExoPlayer.REPEAT_MODE_ONE->playerViewModel.setRepeatMode(RepeatMode.REPEAT_ONE)
        }
        playerViewModel.setShuffleMode(player.shuffleModeEnabled)
        updatePlayerProgress(player)
        player.addListener(object :Player.Listener{
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                playerViewModel.setSongTitle(mediaItem?.mediaMetadata?.title.toString())
                playerViewModel.setSongArtistTitle(mediaItem?.mediaMetadata?.artist.toString())
                playerViewModel.setImgUri(mediaItem?.mediaMetadata?.artworkData)
                if (player.duration>0){
                    playerViewModel.setSongDurationValue(player.duration.toFloat())
                }else{
                    playerViewModel.setSongDurationValue(0f)
                }
                viewModel.setLastPosition(player.currentMediaItemIndex)
                updatePlayerProgress(player)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState==ExoPlayer.STATE_READY){
                    playerViewModel.setSongTitle((player.currentMediaItem?.mediaMetadata?.title ?: "Song Name").toString())
                    playerViewModel.setSongArtistTitle((player.currentMediaItem?.mediaMetadata?.artist ?: "Song Artist").toString())
                    playerViewModel.setImgUri(player.currentMediaItem?.mediaMetadata?.artworkData)
                    if (player.duration>0){
                        playerViewModel.setSongDurationValue(player.duration.toFloat())
                    }else{
                        playerViewModel.setSongDurationValue(0f)
                    }
                    updatePlayerProgress(player)
                }else if (playbackState==ExoPlayer.STATE_ENDED){
                    viewModel.setLastPosition(player.currentMediaItemIndex)
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                playerViewModel.setIsPlayingValue(isPlaying)
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                super.onRepeatModeChanged(repeatMode)
                when(repeatMode){
                    ExoPlayer.REPEAT_MODE_ALL->playerViewModel.setRepeatMode(RepeatMode.REPEAT_ALL)
                    ExoPlayer.REPEAT_MODE_OFF->playerViewModel.setRepeatMode(RepeatMode.REPEAT_OFF)
                    ExoPlayer.REPEAT_MODE_ONE->playerViewModel.setRepeatMode(RepeatMode.REPEAT_ONE)
                }
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                super.onShuffleModeEnabledChanged(shuffleModeEnabled)
                playerViewModel.setShuffleMode(shuffleModeEnabled)
            }
        })
    }
    private fun updatePlayerProgress(player:MediaController){
        lifecycleScope.launch {
            delay(1000)
            if (player.isPlaying){
                playerViewModel.setCurrentPositionValue(player.currentPosition.toFloat())
                playerViewModel.setCurrentPositionStringValue(formatTime(player.currentPosition))
            }
            updatePlayerProgress(player)
        }
    }

    private fun setMediaItems(list:List<Audio>):MutableList<MediaItem>{
        val mediaItems= mutableListOf<MediaItem>()
        for (song in list){
            val uri= Uri.parse(song.path)
            val item= MediaItem.Builder()
                .setUri(uri)
                .setMediaMetadata(getMediaMetadata(song))
                .build()
            mediaItems.add(item)
        }
        return mediaItems
    }

    private fun getMediaMetadata(song:Audio): MediaMetadata {
        return MediaMetadata.Builder()
            .setTitle(song.name)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .setArtworkUri(Uri.parse(song.path))
            .setArtworkData(getAlbumArt(song.path))
            .build()
    }
    private fun getSongs(){
        val list = arrayListOf<Audio>()
        val recents= arrayListOf<Audio>()
        val mediaStoreuri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATA
        )
        val sort = "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        val cursor = contentResolver.query(mediaStoreuri, projection, null, null, sort)
        if (cursor != null) {
            while (cursor.moveToNext()) {
                var name = cursor.getString(1)
                val artist = cursor.getString(2)
                val album = cursor.getString(3)
                val duration = cursor.getLong(4)
                val path = cursor.getString(6)
                val size = cursor.getLong(5)
                val id = cursor.getInt(0)
                if (size > 1048576 && !name.endsWith(".amr")) {
                    name = name.replace(".mp3", "").replace(".wav", "")
                    val audio = Audio(id, name, artist, album, path, duration,null)
                    list.add(audio)
                }
            }
            cursor.close()
        }
        if (list.size<=10){
            viewModel.setRecentSongs(list)
        }else{
            var i=0
            while (i<10){
                recents.add(list[i])
                i++
            }
            viewModel.setRecentSongs(recents)
        }
        list.sortBy { it.name.lowercase() }
        viewModel.setAllSongs(list)
        viewModel.setAlbums(list)
    }
    private fun requestPermission(){
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
            val notGrantedPermissions=permissions.filterNot { permission->
                ContextCompat.checkSelfPermission(this,permission) ==PackageManager.PERMISSION_GRANTED
            }
            if (notGrantedPermissions.isNotEmpty()){
                val showRational=notGrantedPermissions.any { shouldShowRequestPermissionRationale(it)}
                if (showRational){
                    AlertDialog.Builder(this)
                        .setTitle("Muziki Permissions")
                        .setMessage("In order to ensure a smooth experience while using this application,please "+
                        "grant access to storage and notifications.")
                        .setNegativeButton("Cancel"){dialog,_->
                            Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                        .setPositiveButton("OK"){_,_->
                            audioAndNotificationLauncher.launch(notGrantedPermissions.toTypedArray())
                        }
                        .show()
                }else{
                    audioAndNotificationLauncher.launch(notGrantedPermissions.toTypedArray())
                }
            }else {
                getSongs()
            }
        }else{
            if (ContextCompat.checkSelfPermission(this,write_storage)==PackageManager.PERMISSION_GRANTED){
                getSongs()
            }else{
                if (shouldShowRequestPermissionRationale(write_storage)){
                    AlertDialog.Builder(this)
                        .setTitle("Muziki Permission")
                        .setMessage("In order to ensure a smooth experience while using this application,please "+
                                "grant access to storage")
                        .setNegativeButton("Cancel"){dialog,_->
                            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                        .setPositiveButton("OK"){_,_->
                            writeStorageLauncher.launch(write_storage)
                        }
                        .show()
                }else{
                    writeStorageLauncher.launch(write_storage)
                }
            }
        }
    }
    private val audioAndNotificationLauncher=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
        val audioPermission=it[read_storage] ?: false
        if (audioPermission){
            getSongs()
        }else{
            requestPermission()
        }
    }
    private val writeStorageLauncher=registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted->
        if (isGranted){
            getSongs()
        }else{
            requestPermission()
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Compose2Theme {

    }
}