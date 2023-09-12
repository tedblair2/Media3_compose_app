package com.example.compose2.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_table")
data class AudioRecent(
    @PrimaryKey(autoGenerate = true)
    val audioId:Int=0,
    val id:Int,
    val name:String,
    val artist:String,
    val album:String,
    val path:String,
    val duration: Long,
    val dateAdded:Long?
){
    fun audioRecentToAudio():Audio{
        return Audio(id,name, artist, album, path, duration, dateAdded)
    }
}
