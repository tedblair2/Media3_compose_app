package com.github.tedblair2.muziki3.core.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.tedblair2.muziki3.core.local.model.AudioRecent
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAudioList(songs:List<AudioRecent>)

    @Query("SELECT * FROM recents ORDER BY audioId ASC")
    fun getSongs(): Flow<List<AudioRecent>>

    @Query("select * from recents where id=:id")
    suspend fun getExistingAudio(id:Int):AudioRecent?

    @Query("delete from recents where id=:id")
    suspend fun deleteAudio(id:Int)

    @Query("DELETE FROM recents")
    suspend fun deleteSongs()
}