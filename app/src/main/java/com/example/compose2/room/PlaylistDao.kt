package com.example.compose2.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.compose2.model.Playlist

@Dao
interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addPlaylist(playlist: Playlist)

    @Update
    fun updatePlaylist(playlist: Playlist)

    @Delete
    fun deletePlaylist(playlist: Playlist)

    @Query("SELECT * FROM playlists ORDER BY id ASC")
    fun getPlaylist(): LiveData<List<Playlist>>

    @Query("SELECT * FROM playlists ORDER BY id ASC")
    fun getAllPlaylists():List<Playlist>

    @Query("SELECT * FROM playlists WHERE id = :id")
    fun getPlaylistById(id: Int): LiveData<Playlist>
}