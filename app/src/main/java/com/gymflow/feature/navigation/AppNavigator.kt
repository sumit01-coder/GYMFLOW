package com.gymflow.feature.navigation

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AppNavigator {
    private val _navEvents = MutableSharedFlow<String>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val navEvents = _navEvents.asSharedFlow()

    fun navigateTo(route: String) {
        _navEvents.tryEmit(route)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun clear() {
        _navEvents.resetReplayCache()
    }
}

