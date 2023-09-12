package com.example.compose2.musicUi

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.compose2.R
import com.example.compose2.model.Audio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MusicAlbums(albumList:List<Audio> = emptyList(),onAlbumClick: (String) -> Unit) {
    val height= with(LocalDensity.current){180.dp.toPx()}.toInt()
    val configuration= LocalConfiguration.current
    val imgWidth=configuration.screenWidthDp/2
    val imgWidthPx= with(LocalDensity.current){imgWidth.dp.toPx()}.toInt()
    LazyVerticalGrid(columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize()){
        items(items =albumList, key = {it.id} ){audio->
            var albumImage by remember(audio.path){
                mutableStateOf<Bitmap?>(null)
            }
            LaunchedEffect(key1 = audio.path){
                val bitmap= withContext(Dispatchers.IO){
                    loadBitmap(audio.path,imgWidthPx,height)
                }
                albumImage=bitmap
            }
            MusicAlbum(albumName =audio.album,
                albumImage = albumImage,
                onAlbumClick = {onAlbumClick(it)}, audioPath = audio.path)
        }
    }
}

@Composable
fun MusicAlbum(albumName:String,audioPath:String, albumImage: Bitmap?=null,onAlbumClick:(String)->Unit) {
    val context= LocalContext.current
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
        .padding(4.dp)
        .clip(RoundedCornerShape(6.dp))
        .clickable {onAlbumClick(albumName)},
        contentAlignment = Alignment.BottomCenter) {
        Box(modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center ) {
            if (albumImage != null){
                val request= ImageRequest.Builder(context)
                    .data(albumImage)
                    .placeholder(R.drawable.baseline_album_24)
                    .error(R.drawable.baseline_album_24)
                    .fallback(R.drawable.baseline_album_24)
                    .memoryCacheKey(albumName)
                    .diskCacheKey(albumName)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
                AsyncImage(model = request,
                    contentDescription =null,
                    imageLoader = context.imageLoader,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize())
            }else{
                Image(painter = painterResource(id = R.drawable.baseline_album_24),
                    contentDescription =null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .background(color = MaterialTheme.colorScheme.background),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground) )
            }
        }
        Box(modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.DarkGray)
                )
            ))
        Text(text = albumName,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AlbumPreview() {
    MusicAlbums(onAlbumClick = {})
}