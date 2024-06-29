package com.github.tedblair2.muziki3.features.createplaylist.ui

import android.os.Bundle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.github.tedblair2.muziki3.features.createplaylist.viewmodel.CreatePlaylistEvents
import com.github.tedblair2.muziki3.features.createplaylist.viewmodel.CreatePlaylistViewModel
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

enum class Age{
    NEW,EXISTS
}

enum class Parent{
    FAVORITE,PLAYLIST
}

@Serializable
data class CreatePlaylist(val age: Age,val parent: Parent,val playlistId:Int=0,val title:String="")

fun NavGraphBuilder.createPlaylistScreen(
    onNavigateUp:()->Unit
){
    composable<CreatePlaylist>(
        typeMap = mapOf(
            typeOf<Age>() to AgeNavType(),
            typeOf<Parent>() to ParentNavType()
        )
    ){
        val args=it.toRoute<CreatePlaylist>()
        val viewModel= hiltViewModel<CreatePlaylistViewModel>()
        val screenState by viewModel.screenState.collectAsStateWithLifecycle()

        LaunchedEffect(key1 = Unit) {
            viewModel.onEvent(CreatePlaylistEvents.OnNavigateToScreen(
                age = args.age,
                parent = args.parent,
                playlistId = args.playlistId,
                title = args.title
            ))
        }

        CreatePlaylistScreen(
            createPlaylistScreenState = screenState,
            onEvent = viewModel::onEvent,
            onNavigateUp = onNavigateUp
        )
    }
}

fun NavController.navigateToCreatePlaylist(
    age: Age,
    parent: Parent,
    playlistId:Int=0,
    title: String="",
    options:NavOptionsBuilder.()->Unit={}
){
    navigate(CreatePlaylist(age,parent,playlistId,title)){
        options()
    }
}

class AgeNavType:NavType<Age>(isNullableAllowed = false){

    override fun get(bundle: Bundle, key: String): Age? {
        return bundle.getString(key)?.let { Age.valueOf(it) }
    }

    override fun parseValue(value: String): Age {
        return Age.valueOf(value)
    }

    override fun put(bundle: Bundle, key: String, value: Age) {
        bundle.putString(key,value.name)
    }

    override val name: String
        get() = "Age"

    override fun serializeAsValue(value: Age): String {
        return value.name
    }
}

class ParentNavType:NavType<Parent>(isNullableAllowed = false){
    override fun get(bundle: Bundle, key: String): Parent? {
        return bundle.getString(key)?.let { Parent.valueOf(it) }
    }

    override fun parseValue(value: String): Parent {
        return Parent.valueOf(value)
    }

    override fun put(bundle: Bundle, key: String, value: Parent) {
        bundle.putString(key,value.name)
    }

    override val name: String
        get() = "Parent"

    override fun serializeAsValue(value: Parent): String {
        return value.name
    }

}