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

    suspend fun retryFailedOperations()
}
