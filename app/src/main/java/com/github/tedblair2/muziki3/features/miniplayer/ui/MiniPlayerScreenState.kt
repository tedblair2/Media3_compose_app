package com.github.tedblair2.muziki3.features.miniplayer.ui

import android.graphics.Bitmap

data class MiniPlayerScreenState(
    val songName:String="" ,
    val songArtist:String="" ,
    val isPlaying:Boolean=false ,
    val songImage:Bitmap?=null
)
