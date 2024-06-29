package com.github.tedblair2.muziki3.core.local

import kotlin.coroutines.CoroutineContext

interface AppCoroutineContext {
    val main:CoroutineContext
    val io:CoroutineContext
    val default:CoroutineContext
}