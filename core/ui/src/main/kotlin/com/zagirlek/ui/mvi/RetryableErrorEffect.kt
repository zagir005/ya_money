package com.zagirlek.ui.mvi

interface RetryableErrorEffect : Effect {
    val message: String
}
