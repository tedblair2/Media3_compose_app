package com.github.tedblair2.muziki3.features.player.ui

data class PlayerScreenState(
    val songName:String="" ,
    val songArtist:String="" ,
    val isPlaying:Boolean=false ,
    val shuffle:Boolean=false ,
    val currentPosition:Float=0f ,
    val songDuration:Float=0f ,
    val currentPositionString:String="00:00" ,
    val songImage:ByteArray?=null ,
    val repeatMode:RepeatMode=RepeatMode.REPEAT_OFF,
    val isMiniPlayerVisible:Boolean=false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayerScreenState

        if (songName != other.songName) return false
        if (songArtist != other.songArtist) return false
        if (isPlaying != other.isPlaying) return false
        if (shuffle != other.shuffle) return false
        if (currentPosition != other.currentPosition) return false
        if (songDuration != other.songDuration) return false
        if (currentPositionString != other.currentPositionString) return false
        if (songImage != null) {
            if (other.songImage == null) return false
            if (!songImage.contentEquals(other.songImage)) return false
        } else if (other.songImage != null) return false
        if (repeatMode != other.repeatMode) return false
        if (isMiniPlayerVisible != other.isMiniPlayerVisible) return false

        return true
    }

    override fun hashCode(): Int {
        var result=songName.hashCode()
        result=31 * result + songArtist.hashCode()
        result=31 * result + isPlaying.hashCode()
        result=31 * result + shuffle.hashCode()
        result=31 * result + currentPosition.hashCode()
        result=31 * result + songDuration.hashCode()
        result=31 * result + currentPositionString.hashCode()
        result=31 * result + (songImage?.contentHashCode() ?: 0)
        result=31 * result + repeatMode.hashCode()
        result=31 * result + isMiniPlayerVisible.hashCode()
        return result
    }
}

enum class RepeatMode {
    REPEAT_OFF,REPEAT_ALL,REPEAT_ONE
}
