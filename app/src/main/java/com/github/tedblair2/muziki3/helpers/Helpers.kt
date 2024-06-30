package com.github.tedblair2.muziki3.helpers

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.ComponentActivity
import com.github.tedblair2.muziki3.R
import java.util.concurrent.TimeUnit

fun formatTime(duration:Long):String{
    val time= TimeUnit.MILLISECONDS.toSeconds(duration)
    val hours= time/3600
    val minutes = time % 3600 / 60
    val seconds = time % 60
    if (hours<1){
        return "${minutes.pad()}:${seconds.pad()}"
    }
    return "${hours.pad()}:${minutes.pad()}:${seconds.pad()}"
}

fun Long.pad(): String {
    return this.toString().padStart(2, '0')
}

fun songCountString(count: Int): String {
    return if (count==1){
        "$count song"
    }else{
        "$count songs"
    }
}

fun loadBitmapFromByteArray(data: ByteArray?,maxWidth:Int,maxHeight: Int,context: Context): Bitmap {
    return if (data != null) {
        decodeSampledBitmapFromByteArray(data,maxWidth, maxHeight)
    }else BitmapFactory.decodeResource(context.resources, R.drawable.p32)
}

fun decodeSampledBitmapFromByteArray(data: ByteArray,maxWidth:Int,maxHeight: Int): Bitmap {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeByteArray(data, 0, data.size, options)

    // Calculate the inSampleSize to reduce memory consumption
    options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)

    // Decode the bitmap with the calculated sample size
    options.inJustDecodeBounds = false
    return BitmapFactory.decodeByteArray(data, 0, data.size, options)
}
fun calculateInSampleSize(options: BitmapFactory.Options , maxWidth: Int , maxHeight: Int): Int {
    var inSampleSize = 1
    val width = options.outWidth
    val height = options.outHeight

    if (width > maxWidth || height > maxHeight) {
        val halfWidth = width / 2
        val halfHeight = height / 2

        while ((halfWidth / inSampleSize) >= maxWidth && (halfHeight / inSampleSize) >= maxHeight) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}

fun Context.getActivity(): ComponentActivity?{
    var currentContext=this
    while (currentContext is ContextWrapper){
        if (currentContext is ComponentActivity){
            return currentContext
        }
        currentContext=currentContext.baseContext
    }
    return null
}