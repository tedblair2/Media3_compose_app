package com.example.compose2.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.compose2.model.Audio
import com.example.compose2.model.AudioRecent
import com.example.compose2.model.Playlist

@Database(entities = [Audio::class,Playlist::class,AudioRecent::class], version = 1, exportSchema = false)
@TypeConverters(PlaylistConverter::class)
abstract class MuzikiDatabase:RoomDatabase() {

    abstract fun getFavoriteDao():FavoritesDao
    abstract fun getPlaylistDao():PlaylistDao
    abstract fun getRecentPlaylistDao():RecentPlaylistDao
}