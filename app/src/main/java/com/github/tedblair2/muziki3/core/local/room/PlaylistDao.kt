package com.github.tedblair2.muziki3.core.local.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.github.tedblair2.muziki3.core.local.model.PlaylistLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Upsert
    suspend fun addPlaylist(playlist:PlaylistLocal)

    @Update
    suspend fun updatePlaylist(playlist: PlaylistLocal)

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistLocal)

    @Query("Select * from playlists order by id ASC")
    fun getPlaylist():Flow<List<PlaylistLocal>>

    @Query("Select * from playlists order by id ASC")
    suspend fun getAllPlaylist():List<PlaylistLocal>

    @Query("Select * from playlists where id=:id")
    fun getPlaylistById(id:Int):Flow<PlaylistLocal>

    @Query("Select * from playlists where id=:id")
    suspend fun getPlaylistByIdSync(id:Int):PlaylistLocal?
}