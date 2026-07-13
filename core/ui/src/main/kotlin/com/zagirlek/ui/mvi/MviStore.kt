package com.zagirlek.ui.mvi

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MviStore<Intent : Any, State : Any, Effect : Any> {
    val state: StateFlow<State>
    val effects: Flow<Effect>

    fun accept(intent: Intent)
}
