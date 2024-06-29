package com.github.tedblair2.muziki3.core.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.tedblair2.muziki3.core.local.model.Audio
import com.github.tedblair2.muziki3.core.local.model.AudioRecent
import com.github.tedblair2.muziki3.core.local.model.PlaylistLocal

@Database(entities = [Audio::class,PlaylistLocal::class,AudioRecent::class], version = 1, exportSchema = false)
@TypeConverters(PlaylistConverter::class)
abstract class AppDatabase:RoomDatabase() {

    abstract fun favoriteDao():FavoritesDao
    abstract fun playListDao():PlaylistDao
    abstract fun recentsDao():RecentsDao
}