package com.example.compose2.routes

const val ARG_1="type"
const val ARG_2="name"
const val ARG_3="age"
const val ARG_4="playlist"
sealed class Screens(val route:String){
    data object Home:Screens("home_route")
    data object Player:Screens("player_route")
    data object Details:Screens("detail_route/{$ARG_1}/{$ARG_2}"){
        fun passParams(type:String,name:String):String{
            return "detail_route/$type/$name"
        }
    }
    data object CreatePlaylist:Screens("create_playlist/{$ARG_3}/{$ARG_4}"){
        fun passParams(age:String,playlist:String):String{
            return "create_playlist/$age/$playlist"
        }
    }
    data object SearchSong:Screens("search_song")
}
