package com.zagirlek.finance.impl.sync

import com.zagirlek.finance.api.sync.FinanceSyncStatus
import com.zagirlek.finance.impl.local.PendingEntityType
import com.zagirlek.finance.impl.local.PendingOperationStatus
import com.zagirlek.finance.impl.local.PendingOperationType
import com.zagirlek.finance.impl.local.sync.PendingOperationEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class FinanceSyncStatusMapperTest {
    @Test
    fun `outbox operations are aggregated by status`() {
        val status = listOf(
            operation("pending-1", PendingOperationStatus.Pending),
            operation("pending-2", PendingOperationStatus.Pending),
            operation("failed", PendingOperationStatus.Failed),
            operation("unknown", PendingOperationStatus.UnknownResult),
        ).toDomainStatus()

        assertEquals(
            FinanceSyncStatus(
                pendingCount = 2,
                failedCount = 1,
                unknownResultCount = 1,
            ),
            status,
        )
    }

    private fun operation(
        id: String,
        status: PendingOperationStatus,
    ): PendingOperationEntity = PendingOperationEntity(
        id = id,
        entityType = PendingEntityType.Transaction,
        operationType = PendingOperationType.Create,
        entityClientId = id,
        payloadJson = "{}",
        dependsOnOperationId = null,
        status = status,
        attemptCount = 0,
        lastError = null,
        createdAtMillis = 0,
        nextAttemptAtMillis = null,
    )
}
