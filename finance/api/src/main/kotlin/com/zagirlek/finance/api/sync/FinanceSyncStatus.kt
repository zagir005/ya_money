package com.zagirlek.finance.api.sync

import kotlinx.coroutines.flow.Flow

data class FinanceSyncStatus(
    val pendingCount: Int = 0,
    val failedCount: Int = 0,
    val unknownResultCount: Int = 0,
) {
    init {
        require(pendingCount >= 0)
        require(failedCount >= 0)
        require(unknownResultCount >= 0)
    }

    val totalCount: Int
        get() = pendingCount + failedCount + unknownResultCount
}

interface FinanceSyncStatusRepository {
    fun observeStatus(): Flow<FinanceSyncStatus>

    /**
     * Returns failed operations to the queue and requests a sync pass.
     * Unknown POST results are only reconciled with remote data and are never
     * blindly converted back to pending creates.
     */
    suspend fun retryFailedOperations()
}
