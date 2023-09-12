package com.example.compose2.room

import androidx.room.TypeConverter
import com.example.compose2.model.Audio
import com.google.gson.Gson

class PlaylistConverter {
    @TypeConverter
    fun listToJson(list:List<Audio>):String= Gson().toJson(list)

    @TypeConverter
    fun jsonToList(name:String)= Gson().fromJson(name,Array<Audio>::class.java).toList()
}