package com.github.tedblair2.muziki3.features.player.viewmodel

import com.github.tedblair2.muziki3.core.local.model.Audio
import com.github.tedblair2.muziki3.data.redux.Action
import com.github.tedblair2.muziki3.features.player.ui.RepeatMode

sealed interface PlayerEvents:Action {
    data class OnAudioClick(val songs:List<Audio>,val position:Int):PlayerEvents
    data object OnPrevious:PlayerEvents
    data object OnNext:PlayerEvents
    data object OnPlayPause:PlayerEvents
    data object OnShuffleClick:PlayerEvents
    data object OnRepeatClick:PlayerEvents
    data class OnPositionChange(val position:Long):PlayerEvents
    data class StartStopProgress(val update:Boolean):PlayerEvents
    data class OnDelete(val audio: Audio):PlayerEvents
    data class SetSongName(val name:String):PlayerEvents
    data class SetSongArtist(val artist:String):PlayerEvents
    data class SetIsPlaying(val isPlaying:Boolean):PlayerEvents
    data class SetShuffle(val isShuffle:Boolean):PlayerEvents
    data class SetCurrentPosition(val position:Float):PlayerEvents
    data class SetSongDuration(val duration:Float):PlayerEvents
    data class SetRepeatMode(val repeatMode: RepeatMode):PlayerEvents
    data class SetSongImage(val image:ByteArray?):PlayerEvents {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SetSongImage

            if (image != null) {
                if (other.image == null) return false
                if (!image.contentEquals(other.image)) return false
            } else if (other.image != null) return false

            return true
        }

        override fun hashCode(): Int {
            return image?.contentHashCode() ?: 0
        }
    }
    data class MiniPlayerVisibility(val visibility:Boolean):PlayerEvents
}