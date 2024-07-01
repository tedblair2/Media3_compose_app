package com.github.tedblair2.muziki3.data.service

import com.github.tedblair2.muziki3.R

data class CurrentAudioState(
    val isInFavorite:Boolean=false,
    val name:String="",
    val artist:String="",
    val album:String=""
){
    val icon:Int
        get() = if(isInFavorite) R.drawable.baseline_favorite_24 else R.drawable.baseline_favorite_border_24
    val title:String
        get() = if(isInFavorite) "addToFavorite" else "removeFromFavorite"
}
