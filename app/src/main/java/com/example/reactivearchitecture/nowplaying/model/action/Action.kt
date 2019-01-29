package com.example.reactivearchitecture.nowplaying.model.action

sealed class Action

/**
 * Internal representation of [com.example.reactivearchitecture.nowplaying.model.event.FilterEvent].
 */
data class FilterAction(val isFilterOn: Boolean) : Action()

/**
 * Internal representation of [com.example.reactivearchitecture.nowplaying.model.event.RestoreEvent].
 */
data class RestoreAction(val pageNumberToRestore: Int) : Action()

/**
 * Internal representation of [com.example.reactivearchitecture.nowplaying.model.event.ScrollEvent].
 */
data class ScrollAction(val pageNumber: Int) : Action()
