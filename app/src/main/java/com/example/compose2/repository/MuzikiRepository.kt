package com.example.compose2.repository

import androidx.lifecycle.LiveData
import androidx.room.withTransaction
import com.example.compose2.model.Audio
import com.example.compose2.model.AudioRecent
import com.example.compose2.model.Playlist
import com.example.compose2.room.MuzikiDatabase
import kotlinx.coroutines.flow.Flow

class MuzikiRepository(private val muzikiDatabase: MuzikiDatabase){

    private val favoritesDao=muzikiDatabase.getFavoriteDao()
    private val playlistDao=muzikiDatabase.getPlaylistDao()
    private val recentPlaylistDao=muzikiDatabase.getRecentPlaylistDao()

    fun getAlbums(list:ArrayList<Audio>):ArrayList<Audio>{
        val albumList= arrayListOf<Audio>()
        val uniqueAlbums= mutableSetOf<String>()
        for(audio in list){
            if (uniqueAlbums.add(audio.album)){
                albumList.add(audio)
            }
        }
        return  albumList
    }

    fun addFavorite(audio: Audio){
        val existingAudio=favoritesDao.getAudio(audio.id)
        val newAudio=Audio(audio.id,audio.name,audio.artist,audio.album,audio.path,audio.duration,System.currentTimeMillis())
        if (existingAudio==null){
            favoritesDao.addAudio(newAudio)
        }
    }
    fun getFavorites():LiveData<List<Audio>> = favoritesDao.getFavorites()

    fun deleteFavorite(audio: Audio){
        favoritesDao.deleteAudio(audio.id)
    }
    fun getPlaylists():LiveData<List<Playlist>> = playlistDao.getPlaylist()

    fun addPlaylist(playlist: Playlist){
        playlistDao.addPlaylist(playlist)
    }

    fun addAudioToPlaylist(audio: Audio,playlist: Playlist){
        if (!playlist.songs.contains(audio)){
            val list=playlist.songs.toMutableList()
            list.add(audio)
            val newPlaylist=Playlist(playlist.name,list,playlist.id)
            playlistDao.updatePlaylist(newPlaylist)
        }
    }

    fun removeAudioFromPlaylist(audio: Audio,playlist: Playlist){
        if (playlist.songs.contains(audio)){
            val list=playlist.songs.toMutableList()
            list.remove(audio)
            val newPlaylist=Playlist(playlist.name,list,playlist.id)
            playlistDao.updatePlaylist(newPlaylist)
        }
    }

    fun addAudioListToPlaylist(audioList:List<Audio>,playlist: Playlist){
        val currentList=playlist.songs.toMutableList()
        for (audio in audioList){
            if (!currentList.contains(audio)){
                currentList.add(audio)
            }
        }
        val newPlaylist=Playlist(playlist.name,currentList,playlist.id)
        playlistDao.updatePlaylist(newPlaylist)
    }

    suspend fun deleteAudioFromPlaylistsAndFavorite(audio: Audio){
        val list=playlistDao.getAllPlaylists()
        muzikiDatabase.withTransaction {
            deleteFavorite(audio)
            for (playlist in list){
                removeAudioFromPlaylist(audio, playlist)
            }
        }
    }

    fun getPlaylistById(id:Int):LiveData<Playlist>{
        return playlistDao.getPlaylistById(id)
    }

    fun deletePlaylist(playlist: Playlist){
        playlistDao.deletePlaylist(playlist)
    }

    suspend fun addAudioListToDb(list: List<Audio>){
        muzikiDatabase.withTransaction {
            recentPlaylistDao.deleteSongs()
            recentPlaylistDao.addSongs(list.map { it.audioToAudioRecent() })
        }
    }
    fun getRecentList():Flow<List<AudioRecent>> = recentPlaylistDao.getSongs()
}