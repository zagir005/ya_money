package com.zagirlek.finance.impl.sync

import com.zagirlek.finance.api.error.FinanceNetworkException
import kotlinx.coroutines.delay

internal suspend fun <Result> retryServerFailures(
    block: suspend () -> Result,
): Result = retryServerFailures(
    retryDelay = { delayMillis -> delay(delayMillis) },
    block = block,
)

internal suspend fun <Result> retryServerFailures(
    retryDelay: suspend (Long) -> Unit,
    block: suspend () -> Result,
): Result {
    var completedAttempts = 0

    while (true) {
        try {
            return block()
        } catch (error: FinanceNetworkException.ServerFailure) {
            completedAttempts += 1
            if (completedAttempts >= ServerRetryMaxAttempts) {
                throw error
            }
            retryDelay(ServerRetryDelayMillis)
        }
    }
}

internal const val ServerRetryMaxAttempts = 3
internal const val ServerRetryDelayMillis = 2_000L
