package com.github.tedblair2.muziki3.core.local.model

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.Ignore
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
    val dateAdded:Long?,
    val artWork:ByteArray?,
) {
    @Ignore var picture:Bitmap?=null

    constructor(
        id: Int,
        name: String,
        artist: String,
        album: String,
        path: String,
        duration: Long,
        dateAdded: Long?,
        artWork: ByteArray?,
        picture: Bitmap?
    ) : this(id, name, artist, album, path, duration, dateAdded, artWork) {
        this.picture = picture
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Audio

        if (id != other.id) return false
        if (name != other.name) return false
        if (artist != other.artist) return false
        if (album != other.album) return false
        if (path != other.path) return false
        if (duration != other.duration) return false
        if (dateAdded != other.dateAdded) return false
        if (artWork != null) {
            if (other.artWork == null) return false
            if (!artWork.contentEquals(other.artWork)) return false
        } else if (other.artWork != null) return false
        if (picture != other.picture) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + artist.hashCode()
        result = 31 * result + album.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + duration.hashCode()
        result = 31 * result + (dateAdded?.hashCode() ?: 0)
        result = 31 * result + (artWork?.contentHashCode() ?: 0)
        result = 31 * result + (picture?.hashCode() ?: 0)
        return result
    }
}
