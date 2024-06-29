package com.github.tedblair2.muziki3.features.details.ui

import android.os.Bundle
import androidx.navigation.NavType

enum class DetailScreenType {
    ALBUM,PLAYLIST,FAVORITE,RECENT
}

class DetailScreenNavType:NavType<DetailScreenType>(isNullableAllowed = false){

    override fun get(bundle: Bundle , key: String): DetailScreenType? {
        return bundle.getString(key)?.let { DetailScreenType.valueOf(it) }
    }

    override fun parseValue(value: String): DetailScreenType {
        return DetailScreenType.valueOf(value)
    }

    override fun put(bundle: Bundle , key: String , value: DetailScreenType) {
        bundle.putString(key,value.name)
    }

    override val name: String
        get() = "DetailScreenType"

    override fun serializeAsValue(value: DetailScreenType): String {
        return value.name
    }
}