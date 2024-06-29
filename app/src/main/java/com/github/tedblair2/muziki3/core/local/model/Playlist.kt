package com.github.tedblair2.muziki3.core.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

data class Playlist(
    val name: String,
    val songs:List<Audio>,
    val id:Int=0
)

@Serializable
data class PlaylistAudio(
    val id:Int,
    val name:String,
    val artist:String,
    val album:String,
    val path:String,
    val duration: Long,
    val dateAdded:Long?,
)

@Entity(tableName = "playlists")
data class PlaylistLocal(
    val name: String,
    val songs: List<PlaylistAudio>,
    @PrimaryKey(autoGenerate = true)
    val id: Int=0
)
