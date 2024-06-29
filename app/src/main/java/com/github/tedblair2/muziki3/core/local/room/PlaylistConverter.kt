package com.github.tedblair2.muziki3.core.local.room

import androidx.room.TypeConverter
import com.github.tedblair2.muziki3.core.local.model.PlaylistAudio
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PlaylistConverter {

    @TypeConverter
    fun listToString(list:List<PlaylistAudio>):String=Json.encodeToString(list)

    @TypeConverter
    fun stringToList(string:String):List<PlaylistAudio> = Json.decodeFromString(string)
}