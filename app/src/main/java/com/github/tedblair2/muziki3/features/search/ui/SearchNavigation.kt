package com.github.tedblair2.muziki3.features.search.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object Search

fun NavGraphBuilder.searchScreen(
    onNavigateUp:()->Unit
){
    composable<Search>{
        SearchScreen(
            onNavigateUp = onNavigateUp
        )
    }
}

fun NavController.navigateToSearch(
    options:NavOptionsBuilder.()->Unit={}
){
    navigate(Search){
        options()
    }
}