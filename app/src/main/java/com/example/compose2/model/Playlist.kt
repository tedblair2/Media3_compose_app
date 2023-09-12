package com.example.compose2.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class Playlist(
    val name:String,
    val songs:List<Audio>,
    @PrimaryKey(autoGenerate = true)
    val id:Int=0,
)
