package com.github.tedblair2.muziki3.core.local

import com.github.tedblair2.muziki3.core.local.AppCoroutineContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class AppCoroutineContextImpl @Inject constructor() : AppCoroutineContext {
    override val main: CoroutineContext
        get() = Dispatchers.Main
    override val io: CoroutineContext
        get() = Dispatchers.IO
    override val default: CoroutineContext
        get() = Dispatchers.Default
}