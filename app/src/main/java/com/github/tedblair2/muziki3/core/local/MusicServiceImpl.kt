package com.github.tedblair2.muziki3.core.local

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.github.tedblair2.muziki3.core.local.model.Audio
import com.github.tedblair2.muziki3.helpers.loadBitmapFromByteArray
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MusicServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appCoroutineContext: AppCoroutineContext
) : MusicService {

    private val contentResolver = context.contentResolver

    override fun getSongs(permissionGranted: Boolean): Flow<List<Audio>> = callbackFlow {
        if (permissionGranted){
            val mediaStoreUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            val observer=AudioObserver(contentResolver, coroutineScope = this ,onChange = {
                trySend(fetchSongs(mediaStoreUri))
            })

            observer.register(mediaStoreUri)

            trySend(fetchSongs(mediaStoreUri))

            awaitClose {
                observer.unregister()
            }
        }else{
            trySend(emptyList())
            awaitClose {  }
        }
    }

    private suspend fun fetchSongs(mediaStoreUri:Uri):List<Audio>{
        val songs= arrayListOf<Audio>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATA
        )
        val sort = "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        val cursor = contentResolver.query(mediaStoreUri, projection, null, null, sort)

        cursor?.use {
            while (it.moveToNext()) {
                var name = it.getString(1)
                val artist = it.getString(2)
                val album = it.getString(3)
                val duration = it.getLong(4)
                val path = it.getString(6)
                val size = it.getLong(5)
                val id = it.getInt(0)
                if (size > 1048576 && !name.endsWith(".amr") && !album.contains("WhatsApp")) {
                    val image=getSongImage(path)
                    val bitmap=generateBitmap(image)
                    name = name.replace(".mp3", "")
                        .replace(".wav", "")
                        .replace(".aac","")
                        .replace(".m4a","")
                    val audio = Audio(id, name, artist, album, path, duration,null,image,bitmap)
                    songs.add(audio)
                }
            }
        }
        return songs
    }

    private fun generateBitmap(art:ByteArray?):Bitmap{
        return loadBitmapFromByteArray(art,100,100,context)
    }

    private suspend fun getSongImage(uri:String):ByteArray?= withContext(appCoroutineContext.io){
        val retriever=MediaMetadataRetriever()
        try {
            retriever.setDataSource(uri)
            retriever.embeddedPicture
        }catch (e:Exception){
            e.printStackTrace()
            null
        }finally {
            retriever.release()
        }
    }
}