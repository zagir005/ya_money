package com.zagirlek.finance.impl.sync

import android.util.Log

internal object SyncDebugLog {
    const val tag = "YaMoneySync"

    fun debug(message: String) {
        Log.d(tag, message)
    }

    fun warning(message: String, error: Throwable? = null) {
        if (error == null) {
            Log.w(tag, message)
        } else {
            Log.w(tag, message, error)
        }
    }
}
