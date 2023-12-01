package com.example.compose2.di

import androidx.room.Room
import com.example.compose2.pref.DataPrefService
import com.example.compose2.pref.DataPrefServiceImpl
import com.example.compose2.repository.MuzikiRepository
import com.example.compose2.room.MuzikiDatabase
import com.example.compose2.viewmodel.MuzikiViewModel
import com.example.compose2.viewmodel.PlayerViewModel
import com.example.compose2.viewmodel.PlaylistViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule= module {
    single {
        Room.databaseBuilder(
            androidApplication().applicationContext,
            MuzikiDatabase::class.java,"muziki_db_v3")
            .build()
    }
    single<DataPrefService> {
        DataPrefServiceImpl(androidContext())
    }
    factory {
        MuzikiRepository(get())
    }
    viewModel { MuzikiViewModel(get(),get(),get()) }

    viewModel { PlaylistViewModel(get()) }

    viewModel { PlayerViewModel(get()) }
}