package com.example.compose2.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.compose2.model.AudioRecent
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentPlaylistDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addSongs(audioList:List<AudioRecent>)

    @Query("SELECT * FROM recent_table ORDER BY audioId ASC")
    fun getSongs():Flow<List<AudioRecent>>

    @Query("DELETE FROM recent_table")
    fun deleteSongs()
}