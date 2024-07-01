package com.github.tedblair2.muziki3.data.service

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.TaskStackBuilder
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.github.tedblair2.muziki3.MainActivity
import com.github.tedblair2.muziki3.R
import com.github.tedblair2.muziki3.core.local.AppCoroutineContext
import com.github.tedblair2.muziki3.core.local.RoomRepository
import com.github.tedblair2.muziki3.core.local.datastore.DataPrefService
import com.github.tedblair2.muziki3.core.local.model.Audio
import com.github.tedblair2.muziki3.core.local.model.AudioRecent
import com.github.tedblair2.muziki3.helpers.Util
import com.github.tedblair2.muziki3.helpers.loadBitmapFromByteArray
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class MediaPlayerService:MediaSessionService(),MediaSession.Callback {

    private var mediaSession: MediaSession? = null
    private lateinit var player:ExoPlayer

    @Inject
    lateinit var dataPrefService: DataPrefService

    @Inject
    lateinit var roomRepository: RoomRepository

    @Inject
    lateinit var appCoroutineContext: AppCoroutineContext

    private val serviceScope=CoroutineScope(SupervisorJob()+Dispatchers.IO)

    private var defaultBitmap: Bitmap? =null

    private val _currentAudioState= MutableStateFlow(CurrentAudioState())
    private val currentAudioState=_currentAudioState.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        defaultBitmap= BitmapFactory.decodeResource(resources,R.drawable.p33)
        val audioAttributes= AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
        player=ExoPlayer.Builder(baseContext).setAudioAttributes(audioAttributes,true).build()
        mediaSession=MediaSession.Builder(baseContext,player)
            .setSessionActivity(notificationIntent())
            .setCallback(this)
            .setCustomLayout(
                ImmutableList.of(
                    CommandButton.Builder()
                        .setDisplayName(currentAudioState.value.title)
                        .setIconResId(currentAudioState.value.icon)
                        .setSessionCommand(SessionCommand(Util.FAV_ACTION,Bundle()))
                        .build(),
                    CommandButton.Builder()
                        .setDisplayName("Stop")
                        .setIconResId(R.drawable.baseline_stop_24)
                        .setSessionCommand(SessionCommand(Util.STOP_ACTION, Bundle()))
                        .build()
            ))
            .build()

        setMediaNotificationProvider(object : MediaNotification.Provider{
            override fun createNotification(
                mediaSession: MediaSession ,
                customLayout: ImmutableList<CommandButton> ,
                actionFactory: MediaNotification.ActionFactory ,
                onNotificationChangedCallback: MediaNotification.Provider.Callback
            ): MediaNotification {
                val songName=mediaSession.player.currentMediaItem!!.mediaMetadata.title.toString()
                val songArtist=mediaSession.player.currentMediaItem!!.mediaMetadata.artist.toString()
                val songAlbum=mediaSession.player.currentMediaItem!!.mediaMetadata.albumTitle.toString()
                val art=mediaSession.player.currentMediaItem!!.mediaMetadata.artworkData
                val bitmap=if(art != null){
                    loadBitmapFromByteArray(art,100,100,this@MediaPlayerService)
                }else{
                    defaultBitmap
                }

                serviceScope.launch {
                    val isFavorite=roomRepository.audioInFavorites(songName,songArtist,songAlbum)
                    println("isFavorite $isFavorite")
                    _currentAudioState.update {
                        it.copy(
                            isInFavorite = isFavorite,
                            name = songName,
                            artist = songArtist,
                            album = songAlbum
                        )
                    }
                }

                val playPauseIcon=if (mediaSession.player.isPlaying){
                    IconCompat.createWithResource(this@MediaPlayerService,R.drawable.baseline_pause_circle_filled_24)
                }else{
                    IconCompat.createWithResource(this@MediaPlayerService,R.drawable.baseline_play_circle_filled_24)
                }

                val playPauseTitle=if (mediaSession.player.isPlaying) "Play" else "Pause"
                val prevAction=actionFactory.createMediaAction(
                    mediaSession,
                    IconCompat.createWithResource(this@MediaPlayerService,R.drawable.baseline_skip_previous_24),
                    "Prev",Player.COMMAND_SEEK_TO_PREVIOUS)
                val playPauseAction=actionFactory.createMediaAction(
                    mediaSession,playPauseIcon,playPauseTitle,Player.COMMAND_PLAY_PAUSE)
                val nextAction=actionFactory.createMediaAction(
                    mediaSession,
                    IconCompat.createWithResource(this@MediaPlayerService,R.drawable.baseline_skip_next_24),
                    "Next",Player.COMMAND_SEEK_TO_NEXT)
                val stopAction=actionFactory.createCustomAction(mediaSession,
                    IconCompat.createWithResource(this@MediaPlayerService,R.drawable.baseline_stop_24),
                    "Stop",Util.STOP_ACTION,Bundle())
                val favAction=actionFactory.createCustomAction(
                    mediaSession,
                    IconCompat.createWithResource(this@MediaPlayerService,currentAudioState.value.icon),
                    currentAudioState.value.title,
                    Util.FAV_ACTION,
                    Bundle()
                )


                val notificationBuilder=NotificationCompat.Builder(this@MediaPlayerService,Util.CHANNEL_ID)
                    .setSmallIcon(R.drawable.p33)
                    .setLargeIcon(bitmap)
                    .setContentTitle(songName)
                    .setContentText(songArtist)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(mediaSession.sessionActivity)
                    .addAction(favAction)
                    .addAction(prevAction)
                    .addAction(playPauseAction)
                    .addAction(nextAction)
                    .addAction(stopAction)
                    .setStyle(
                        MediaStyleNotificationHelper.MediaStyle(mediaSession)
                        .setShowActionsInCompactView(1))

                val notification=notificationBuilder.build()

                return MediaNotification(Util.NOTIFICATION_ID,notification)
            }

            override fun handleCustomCommand(
                session: MediaSession ,
                action: String ,
                extras: Bundle
            ): Boolean {
                return when(action){
                    Util.FAV_ACTION->{
                        addAudioToFavorite()
                        true
                    }
                    Util.STOP_ACTION->{
                        stopPlayer()
                        true
                    }
                    else -> false
                }
            }
        })
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onConnect(
        session: MediaSession ,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val connectionResult=super.onConnect(session, controller)
        val sessionCommands=connectionResult.availableSessionCommands
            .buildUpon()
            .add(SessionCommand(Util.FAV_ACTION,Bundle()))
            .add(SessionCommand(Util.STOP_ACTION, Bundle()))
            .build()
        return MediaSession.ConnectionResult.accept(sessionCommands,connectionResult.availablePlayerCommands)
    }

    override fun onCustomCommand(
        session: MediaSession ,
        controller: MediaSession.ControllerInfo ,
        customCommand: SessionCommand ,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        return when(customCommand.customAction){
            Util.FAV_ACTION->{
                addAudioToFavorite()
                Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
            Util.STOP_ACTION->{
                stopPlayer()
                Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
            else-> super.onCustomCommand(session, controller, customCommand, args)
        }
    }

    override fun onPostConnect(session: MediaSession , controller: MediaSession.ControllerInfo) {
        val playPauseBtn=CommandButton.Builder()
            .setDisplayName(if (session.player.isPlaying) "Play" else "Pause")
            .setIconResId(if (session.player.isPlaying) R.drawable.baseline_pause_circle_filled_24 else R.drawable.baseline_play_circle_filled_24)
            .setPlayerCommand(Player.COMMAND_PLAY_PAUSE)
            .build()
        val nextBtn=CommandButton.Builder()
            .setDisplayName("Next")
            .setIconResId(R.drawable.baseline_skip_next_24)
            .setPlayerCommand(Player.COMMAND_SEEK_TO_NEXT)
            .build()
        val prevBtn=CommandButton.Builder()
            .setDisplayName("Prev")
            .setIconResId(R.drawable.baseline_skip_previous_24)
            .setPlayerCommand(Player.COMMAND_SEEK_TO_PREVIOUS)
            .build()
        val favBtn=CommandButton.Builder()
            .setDisplayName(currentAudioState.value.title)
            .setIconResId(if (currentAudioState.value.isInFavorite) R.drawable.baseline_favorite_24 else R.drawable.baseline_favorite_border_24)
            .setSessionCommand(SessionCommand(Util.FAV_ACTION,Bundle()))
            .build()
        val stopBtn=CommandButton.Builder()
            .setDisplayName("Stop")
            .setIconResId(R.drawable.baseline_stop_24)
            .setSessionCommand(SessionCommand(Util.STOP_ACTION, Bundle()))
            .build()
        session.setCustomLayout(controller, listOf(favBtn,prevBtn,playPauseBtn,nextBtn,stopBtn))
        super.onPostConnect(session , controller)
    }

    override fun onPlaybackResumption(
        mediaSession: MediaSession ,
        controller: MediaSession.ControllerInfo
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        val settable=SettableFuture.create<MediaSession.MediaItemsWithStartPosition>()
        serviceScope.launch {
            val audioList=roomRepository.getAudioRecentList().first()
            val lastPosition=dataPrefService.getLastPosition().first()
            val mediaItemsWithStartPosition=MediaSession.MediaItemsWithStartPosition(
                setMediaItems(audioList.map { it.toAudio() }),
                lastPosition.toInt(),
                0
            )
            settable.set(mediaItemsWithStartPosition)
        }
        return settable
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession=null
        }
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun addAudioToFavorite(){
        serviceScope.launch {
            val name=currentAudioState.value.name
            val artist=currentAudioState.value.artist
            val album=currentAudioState.value.album
            roomRepository.addToFavoriteFromNotification(name, artist, album)
        }
    }

    private fun notificationIntent(): PendingIntent {
        val intent=Intent(Intent.ACTION_VIEW,"myapp://player_screen".toUri(),
            this, MainActivity::class.java)
        return TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(0,FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    private fun stopPlayer(){
        val notificationManagerCompat=NotificationManagerCompat.from(baseContext)
        mediaSession?.run {
            if (player.playbackState != Player.STATE_IDLE) {
                player.seekTo(0)
                player.playWhenReady = false
                player.stop()
            }
        }
        notificationManagerCompat.cancel(Util.NOTIFICATION_ID)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun setMediaItems(songs:List<Audio>):List<MediaItem>{
        return songs.map {
            MediaItem.Builder()
                .setMediaMetadata(getMediaMetadata(it))
                .setUri(Uri.parse(it.path))
                .build()
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

    private fun getMediaMetadata(song: Audio): MediaMetadata {
        return MediaMetadata.Builder()
            .setTitle(song.name)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .setArtworkUri(Uri.parse(song.path))
            .setArtworkData(song.artWork, MediaMetadata.PICTURE_TYPE_FILE_ICON)
            .build()
    }
}