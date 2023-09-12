package com.example.compose2.services

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.TaskStackBuilder
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.*
import com.example.compose2.MainActivity
import com.example.compose2.R
import com.example.compose2.Util
import com.example.compose2.dataStore
import com.example.compose2.model.Audio
import com.example.compose2.musicUi.getAlbumArt
import com.example.compose2.repository.MuzikiRepository
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

@UnstableApi class MediaPlayerService:MediaSessionService(),MediaSession.Callback {
    private var mediaSession:MediaSession?=null
    private lateinit var player: ExoPlayer
    private val muzikiRepository by inject<MuzikiRepository>()
    private val lastPosition= intPreferencesKey("lastPosition")

    override fun onCreate() {
        super.onCreate()
        val audioAttributes= AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
        player=ExoPlayer.Builder(baseContext).setAudioAttributes(audioAttributes,true).build()
        mediaSession=MediaSession.Builder(baseContext,player)
            .setSessionActivity(notificationIntent())
            .setCallback(this)
            .setCustomLayout(ImmutableList.of(
                CommandButton.Builder()
                    .setDisplayName("Stop")
                    .setIconResId(R.drawable.baseline_stop_24)
                    .setSessionCommand(SessionCommand(Util.stop_action, Bundle()))
                    .build()
            ))
            .build()

        setMediaNotificationProvider(object :MediaNotification.Provider{
            override fun createNotification(
                mediaSession: MediaSession,
                customLayout: ImmutableList<CommandButton>,
                actionFactory: MediaNotification.ActionFactory,
                onNotificationChangedCallback: MediaNotification.Provider.Callback
            ): MediaNotification {
                val art=mediaSession.player.currentMediaItem!!.mediaMetadata.artworkData
                val bitmap=if(art != null){
                    val options = BitmapFactory.Options()
                    options.inSampleSize = 2
                    BitmapFactory.decodeByteArray(art,0,art.size,options)
                }else{
                    BitmapFactory.decodeResource(resources,R.drawable.p32)
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
                    "Stop",Util.stop_action,Bundle())
                val notificationBuilder=NotificationCompat.Builder(this@MediaPlayerService,Util.CHANNEL_ID)
                    .setSmallIcon(R.drawable.p32)
                    .setLargeIcon(bitmap)
                    .setContentTitle(mediaSession.player.currentMediaItem!!.mediaMetadata.title)
                    .setContentText(mediaSession.player.currentMediaItem!!.mediaMetadata.artist)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(mediaSession.sessionActivity)
                    .addAction(prevAction)
                    .addAction(playPauseAction)
                    .addAction(nextAction)
                    .addAction(stopAction)
                    .setStyle(MediaStyleNotificationHelper.MediaStyle(mediaSession)
                        .setShowActionsInCompactView(1))
                    .build()
                return MediaNotification(Util.NOTIFICATION_ID,notificationBuilder)
            }

            override fun handleCustomCommand(
                session: MediaSession,
                action: String,
                extras: Bundle
            ): Boolean {
                if (action==Util.stop_action){
                    stopPlayer()
                    return true
                }
                return false
            }
        })
    }

    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val connectionResult=super.onConnect(session, controller)
        val sessionCommands=connectionResult.availableSessionCommands
            .buildUpon()
            .add(SessionCommand(Util.stop_action, Bundle()))
            .build()
        return MediaSession.ConnectionResult.accept(sessionCommands,connectionResult.availablePlayerCommands)
    }

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        if (customCommand.customAction==Util.stop_action){
            stopPlayer()
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
        return super.onCustomCommand(session, controller, customCommand, args)
    }

    override fun onPostConnect(session: MediaSession, controller: MediaSession.ControllerInfo) {
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
        val stopBtn=CommandButton.Builder()
            .setDisplayName("Stop")
            .setIconResId(R.drawable.baseline_stop_24)
            .setSessionCommand(SessionCommand(Util.stop_action, Bundle()))
            .build()
        session.setCustomLayout(controller, listOf(prevBtn,playPauseBtn,nextBtn,stopBtn))
        super.onPostConnect(session, controller)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onPlaybackResumption(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        val settable=SettableFuture.create<MediaSession.MediaItemsWithStartPosition>()
        CoroutineScope(Dispatchers.Default).launch {
            val lastPosition=getLastPosition().first()
            val list=muzikiRepository.getRecentList().first()
            val mediaItemsWithStartPosition=MediaSession.MediaItemsWithStartPosition(
                setMediaItems(list.map { it.audioRecentToAudio() }),lastPosition,0
            )
            settable.set(mediaItemsWithStartPosition)
        }
        return settable
    }
    private fun getLastPosition(): Flow<Int> {
        return dataStore.data.map { pref ->
            pref[lastPosition] ?: 0
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession=null
        }
        super.onDestroy()
    }

    private fun notificationIntent():PendingIntent{
        val intent=Intent(Intent.ACTION_VIEW,"myapp://player_route".toUri(),
            this,MainActivity::class.java)
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

    private fun getMediaMetadata(song: Audio): MediaMetadata {
        return MediaMetadata.Builder()
            .setTitle(song.name)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .setArtworkUri(Uri.parse(song.path))
            .setArtworkData(getAlbumArt(song.path), MediaMetadata.PICTURE_TYPE_FILE_ICON)
            .build()
    }
}