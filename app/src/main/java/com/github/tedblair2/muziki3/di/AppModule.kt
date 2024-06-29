package com.github.tedblair2.muziki3.di

import com.github.tedblair2.muziki3.core.local.AppCoroutineContext
import com.github.tedblair2.muziki3.core.local.AppCoroutineContextImpl
import com.github.tedblair2.muziki3.core.local.MusicService
import com.github.tedblair2.muziki3.core.local.MusicServiceImpl
import com.github.tedblair2.muziki3.core.local.datastore.DataPrefService
import com.github.tedblair2.muziki3.core.local.datastore.DataPrefServiceImpl
import com.github.tedblair2.muziki3.data.redux.AppStore
import com.github.tedblair2.muziki3.data.redux.Store
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Singleton
    @Binds
    abstract fun bindCoroutineContext(impl: AppCoroutineContextImpl): AppCoroutineContext

    @Singleton
    @Binds
    abstract fun bindMusicService(impl: MusicServiceImpl): MusicService

    @Singleton
    @Binds
    abstract fun bindStore(store: AppStore): Store

    @Singleton
    @Binds
    abstract fun bindDataPref(impl: DataPrefServiceImpl):DataPrefService
}