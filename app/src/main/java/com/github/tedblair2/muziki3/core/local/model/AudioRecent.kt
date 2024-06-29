package com.github.tedblair2.muziki3.core.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recents")
data class AudioRecent(
    @PrimaryKey(autoGenerate = true)
    val audioId:Int=0,
    val id:Int,
    val name:String,
    val artist:String,
    val album:String,
    val path:String,
    val duration: Long,
    val dateAdded:Long?,
)
