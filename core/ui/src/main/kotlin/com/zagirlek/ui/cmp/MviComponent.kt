package com.zagirlek.ui.cmp

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.zagirlek.ui.mvi.Intent
import com.zagirlek.ui.mvi.Mutation
import com.zagirlek.ui.mvi.MviReducer
import com.zagirlek.ui.mvi.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

abstract class MviComponent<S : State, M : Mutation, I : Intent, R : MviReducer<S, M>>(
    private val reducer: R,
    componentContext: ComponentContext,
) : ComponentContext by componentContext {

    protected val componentScope = coroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    protected val ioScope = coroutineScope(SupervisorJob() + Dispatchers.IO)

    protected fun M.reduce(state: MutableStateFlow<S>) {
        state.update { currentState ->
            reducer.reduce(state = currentState, mutation = this@reduce)
        }
    }

    abstract fun accept(intent: I)
}
