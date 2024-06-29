package com.github.tedblair2.muziki3.core.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.github.tedblair2.muziki3.core.local.model.Audio
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {

    @Upsert
    suspend fun addAudio(audio: Audio)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAudioList(songs:List<Audio>)

    @Query("Delete from favorites where id = :id")
    suspend fun deleteAudio(id:Int)

    @Query("Select * from favorites order by dateAdded desc")
    fun getFavorites():Flow<List<Audio>>

    @Query("Select * from favorites where id = :id")
    suspend fun getAudio(id:Int):Audio?

}