package com.github.tedblair2.muziki3.core.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import androidx.room.withTransaction
import com.github.tedblair2.muziki3.R
import com.github.tedblair2.muziki3.core.local.model.Audio
import com.github.tedblair2.muziki3.core.local.model.AudioRecent
import com.github.tedblair2.muziki3.core.local.model.Playlist
import com.github.tedblair2.muziki3.core.local.model.PlaylistAudio
import com.github.tedblair2.muziki3.core.local.model.PlaylistLocal
import com.github.tedblair2.muziki3.core.local.room.AppDatabase
import com.github.tedblair2.muziki3.core.local.room.FavoritesDao
import com.github.tedblair2.muziki3.core.local.room.PlaylistDao
import com.github.tedblair2.muziki3.core.local.room.RecentsDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RoomRepository @Inject constructor(
    private val favoritesDao: FavoritesDao,
    @ApplicationContext private val context: Context,
    private val playlistDao: PlaylistDao,
    private val appCoroutineContext: AppCoroutineContext,
    private val recentsDao: RecentsDao,
    private val appDatabase: AppDatabase
) {

    fun getFavorites():Flow<List<Audio>>{
        return favoritesDao
            .getFavorites()
            .map {
                it.map {audio->
                    audio.apply {
                        picture=generateBitmap(artWork)
                    }
                }
            }
    }

    suspend fun addAudioToFavorites(audio: Audio){
        val existingAudio=favoritesDao.getAudio(audio.id)
        val newAudio=audio.copy(dateAdded = System.currentTimeMillis())
        if (existingAudio==null){
            favoritesDao.addAudio(newAudio)
        }
    }

    suspend fun addToFavoriteFromNotification(name:String,artist:String,album:String){
        if (audioInFavorites(name, artist, album)){
            favoritesDao.deleteAudioByName(name, artist, album)
        }else{
            recentsDao.getSongs().collect {
                val audioRecent=it.find { audio->
                    audio.name==name && audio.artist==artist && audio.album==album
                }
                if (audioRecent != null){
                    favoritesDao.addAudio(audioRecent.toAudio().copy(dateAdded = System.currentTimeMillis()))
                }
            }
        }
    }

    suspend fun audioInFavorites(name:String,artist:String,album:String):Boolean{
        return favoritesDao.getAudioByName(name, artist, album) != null
    }

    suspend fun addAudioListToFavorites(songs:List<Audio>){
        favoritesDao.addAudioList(
            songs = songs.map { it.copy(dateAdded = System.currentTimeMillis()) }
        )
    }

    suspend fun deleteAudioFromFavorites(audio: Audio){
        favoritesDao.deleteAudio(audio.id)
    }

    fun getPlaylists():Flow<List<Playlist>>{
        return playlistDao.getPlaylist()
            .map {
                it.map {local->
                    Playlist(local.name,local.songs.toAudio(),local.id)
                }
            }
    }

    suspend fun addPlaylist(playlist: Playlist){
        val playlistLocal=PlaylistLocal(playlist.name,playlist.songs.toPlayListAudio(),playlist.id)
        playlistDao.addPlaylist(playlistLocal)
    }

    suspend fun addAudioToPlaylist(audio: Audio,playlist: Playlist){
        if (!playlist.songs.contains(audio)){
            val list=playlist.songs.toMutableList()
            list.add(audio)
            val newList=list.toPlayListAudio()
            playlistDao.updatePlaylist(PlaylistLocal(playlist.name,newList,playlist.id))
        }
    }

    suspend fun deleteAudioFromPlaylist(audio: Audio,playlist: Playlist){
        if (playlist.songs.contains(audio)){
            val list=playlist.songs.toMutableList()
            list.remove(audio)
            val newPlaylist=PlaylistLocal(playlist.name,list.toPlayListAudio(),playlist.id)
            playlistDao.updatePlaylist(newPlaylist)
        }
    }

    suspend fun addAudioListToPlaylist(songs: List<Audio>, playlistId: Int){
        val playlist=playlistDao.getPlaylistByIdSync(playlistId)
        val currentList=playlist?.songs?.toMutableList() ?: mutableListOf()
        songs.toPlayListAudio().forEach {
            if (!currentList.contains(it)){
                currentList.add(it)
            }
        }
        val newPlaylist=PlaylistLocal(playlist?.name ?: "",currentList,playlist?.id ?: 0)
        playlistDao.updatePlaylist(newPlaylist)
    }

    suspend fun deletePlaylist(playlist: Playlist){
        val playlistLocal=PlaylistLocal(playlist.name,playlist.songs.toPlayListAudio(),playlist.id)
        playlistDao.deletePlaylist(playlistLocal)
    }

    fun getPlaylistById(id:Int):Flow<Playlist>{
        return playlistDao.getPlaylistById(id)
            .map {
                Playlist(it.name,it.songs.toAudio(),it.id)
            }
    }

    suspend fun addAudioRecentListToDb(songs: List<Audio>){
        appDatabase.withTransaction {
            recentsDao.deleteSongs()
            recentsDao.insertAudioList(songs.map { it.toAudioRecent() })
        }
    }

    fun getAudioRecentList():Flow<List<AudioRecent>> = recentsDao.getSongs()

    suspend fun deleteAudioFromFavoritesAndPlaylist(audio: Audio){
        appDatabase.withTransaction {
            deleteAudioFromFavorites(audio)
            deleteAudioFromRecents(audio)
            val playlists=playlistDao.getAllPlaylist()
            playlists.forEach {
                val songs=it.songs.toMutableList()
                songs.forEach { playlistAudio->
                    if (playlistAudio.id==audio.id){
                        songs.remove(playlistAudio)
                    }
                }
                val newPlaylist=PlaylistLocal(it.name,songs,it.id)
                playlistDao.updatePlaylist(newPlaylist)
            }
        }
    }

    private suspend fun deleteAudioFromRecents(audio: Audio){
        val existingAudio=recentsDao.getExistingAudio(audio.id)
        if (existingAudio != null){
            recentsDao.deleteAudio(audio.id)
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

    private fun Audio.toAudioRecent():AudioRecent{
        return AudioRecent(
            id = id,
            name = name,
            album = album,
            artist = artist,
            path = path,
            dateAdded = dateAdded,
            duration = duration
        )
    }

    private fun List<Audio>.toPlayListAudio():List<PlaylistAudio>{
        return this.map {
            PlaylistAudio(
                id = it.id,
                name = it.name,
                artist = it.artist,
                album = it.album,
                path = it.path,
                duration = it.duration,
                dateAdded = null
            )
        }
    }

    private suspend fun List<PlaylistAudio>.toAudio():List<Audio>{
        return this.map {playlistAudio->
            val artWork=getSongImage(playlistAudio.path)
            val bitmap=generateBitmap(artWork)
            Audio(
                id = playlistAudio.id,
                name = playlistAudio.name,
                artist = playlistAudio.artist,
                album = playlistAudio.album,
                path = playlistAudio.path,
                duration = playlistAudio.duration,
                dateAdded = null,
                artWork = artWork,
            ).apply {
                picture=bitmap
            }
        }
    }


    private fun generateBitmap(art:ByteArray?): Bitmap {
        return if (art != null){
            val options = BitmapFactory.Options()
            options.inSampleSize = 4
            BitmapFactory.decodeByteArray(art,0,art.size,options)
        }else{
            BitmapFactory.decodeResource(context.resources, R.drawable.p33)
        }
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
}