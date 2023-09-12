package com.example.compose2.routes

sealed class TabScreens(val title:String){
    object Songs:TabScreens("SONGS")
    object Albums:TabScreens("ALBUMS")
    object Playlists:TabScreens("PLAYLISTS")
}
