package com.zagirlek.ui.mvi

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface State
interface Mutation
interface Intent
interface Effect

interface MviStore<I : Intent, S : State, E : Effect> {
    val state: StateFlow<S>
    val effects: Flow<E>

    fun accept(intent: I)
}

fun interface MviReducer<S : State, M : Mutation> {
    fun reduce(
        state: S,
        mutation: M,
    ): S
}