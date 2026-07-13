package com.zagirlek.ui.mvi

fun interface MviReducer<State : Any, Mutation : Any> {
    fun reduce(
        state: State,
        mutation: Mutation,
    ): State
}
