package com.github.tedblair2.muziki3.di

import android.content.ComponentName
import android.content.Context
import androidx.annotation.OptIn
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.room.Room
import com.github.tedblair2.muziki3.core.local.room.AppDatabase
import com.github.tedblair2.muziki3.data.service.MediaPlayerService
import com.google.common.util.concurrent.ListenableFuture
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Module {

    @OptIn(UnstableApi::class)
    @Singleton
    @Provides
    fun provideSessionToken(@ApplicationContext context: Context):SessionToken{
        return SessionToken(context, ComponentName(context, MediaPlayerService::class.java))
    }

    @Singleton
    @Provides
    fun provideMediaController(
        @ApplicationContext context: Context,
        token: SessionToken
    ):ListenableFuture<MediaController>{
        return MediaController.Builder(context,token).buildAsync()
    }

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context):AppDatabase{
        return Room
            .databaseBuilder(
                context,
                AppDatabase::class.java,
                "muziki3_db_v6.db")
            .build()
    }

    @Provides
    fun provideFavoriteDao(appDatabase: AppDatabase) = appDatabase.favoriteDao()

    @Provides
    fun providePlaylistDao(appDatabase: AppDatabase)=appDatabase.playListDao()

    @Provides
    fun provideRecentsDao(appDatabase: AppDatabase)=appDatabase.recentsDao()

    @Singleton
    @Provides
    fun providePreferencesDataStore(@ApplicationContext context: Context):DataStore<Preferences>{
        return PreferenceDataStoreFactory.create(
            produceFile = {
                File(context.cacheDir.path,"Muziki3.preferences_pb")
            }
        )
    }
}