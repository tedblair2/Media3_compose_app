package com.github.tedblair2.muziki3.features.album.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.github.tedblair2.muziki3.R
import com.github.tedblair2.muziki3.core.local.model.Audio
import com.github.tedblair2.muziki3.features.album.viewmodel.AlbumsScreenEvents
import com.github.tedblair2.muziki3.features.album.viewmodel.AlbumsViewmodel
import com.github.tedblair2.muziki3.features.details.ui.DetailScreenType
import com.github.tedblair2.muziki3.features.home.ui.NoMediaScreen
import com.github.tedblair2.muziki3.helpers.ShimmerEffect

@Composable
fun AlbumsScreen(
    modifier: Modifier = Modifier,
    navigateToDetailsScreen:(screenType:DetailScreenType,albumName:String,playlistId:Int)->Unit
) {

    val viewModel= hiltViewModel<AlbumsViewmodel>()
    val albumScreenState by viewModel.albumsScreenState.collectAsStateWithLifecycle()

    AlbumsScreenContent(
        modifier = modifier,
        albumScreenState = albumScreenState,
        onEvent = viewModel::onEvent,
        navigateToDetailsScreen = navigateToDetailsScreen
    )
}

@Composable
fun AlbumsScreenContent(
    modifier: Modifier = Modifier,
    albumScreenState: AlbumScreenState,
    onEvent: (AlbumsScreenEvents) -> Unit = {},
    navigateToDetailsScreen:(screenType:DetailScreenType,albumName:String,playlistId:Int)->Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize()
    ) {
        if (albumScreenState.isLoading){
            items(10){
                ShimmerEffect {brush->
                    LoadingAlbum(shimmerBrush = brush)
                }
            }
        }else if (!albumScreenState.permissionGranted || albumScreenState.albumList.isEmpty()){
            item {
                NoMediaScreen()
            }
        }else {
            items(items = albumScreenState.albumList){audio->
                MusicAlbum(
                    audio = audio,
                    modifier = Modifier
                        .clickable {
                            navigateToDetailsScreen(DetailScreenType.ALBUM,audio.album,0)
                        }
                    )
            }
        }
    }
}

@Composable
fun LoadingAlbum(
    modifier: Modifier = Modifier,
    shimmerBrush:Brush
) {
    Box(modifier = modifier
        .fillMaxWidth()
        .height(180.dp)
        .padding(4.dp)
        .clip(RoundedCornerShape(6.dp))
    ){
        Spacer(modifier = Modifier
            .fillMaxSize()
            .background(shimmerBrush)
        )
    }
}

@Composable
fun MusicAlbum(
    modifier: Modifier = Modifier,
    audio: Audio
) {
    val context= LocalContext.current

    Box(modifier = modifier
        .fillMaxWidth()
        .height(180.dp)
        .padding(4.dp)
        .clip(RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.BottomCenter){

        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
            contentAlignment = Alignment.Center){
            val request= ImageRequest.Builder(context)
                .data(audio.picture)
                .placeholder(R.drawable.p32)
                .error(R.drawable.p32)
                .fallback(R.drawable.p32)
                .memoryCacheKey(audio.id.toString())
                .diskCacheKey(audio.id.toString())
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()

            AsyncImage(
                model = request,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
        Box(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.6f)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent , Color.DarkGray)
                )
            ))

        Text(text = audio.album,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}