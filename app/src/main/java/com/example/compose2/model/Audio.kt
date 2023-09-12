package com.example.compose2.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class Audio(
    @PrimaryKey(autoGenerate = false)
    val id:Int,
    val name:String,
    val artist:String,
    val album:String,
    val path:String,
    val duration: Long,
    val dateAdded:Long?
){
    fun audioToAudioRecent():AudioRecent{
        return AudioRecent(id = id, name = name, artist = artist, album = album, path = path,
            duration = duration, dateAdded = dateAdded)
    }
}
