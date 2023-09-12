package com.example.compose2.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.compose2.model.Audio

@Dao
interface FavoritesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addAudio(audio: Audio)

    @Query("DELETE FROM favorites WHERE id =:id")
    fun deleteAudio(id:Int)

    @Query("SELECT * FROM favorites WHERE id =:id")
    fun getAudio(id:Int):Audio?

    @Query("SELECT * FROM favorites ORDER BY dateAdded ASC")
    fun getFavorites(): LiveData<List<Audio>>
}