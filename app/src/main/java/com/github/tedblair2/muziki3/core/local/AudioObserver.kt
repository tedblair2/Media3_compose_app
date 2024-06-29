package com.github.tedblair2.muziki3.core.local

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class AudioObserver(
    private val contentResolver: ContentResolver ,
    private val coroutineScope: CoroutineScope ,
    private val onChange:suspend ()->Unit
): ContentObserver(Handler(Looper.getMainLooper())) {

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        coroutineScope.launch {
            onChange()
        }
    }

    fun register(uri:Uri){
        contentResolver.registerContentObserver(uri,true,this)
    }

    fun unregister(){
        contentResolver.unregisterContentObserver(this)
    }
}