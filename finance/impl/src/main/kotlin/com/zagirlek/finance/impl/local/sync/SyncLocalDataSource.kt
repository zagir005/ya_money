package com.zagirlek.finance.impl.local.sync

import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.transaction.TransactionPeriod
import com.zagirlek.finance.impl.local.PendingEntityType
import com.zagirlek.finance.impl.local.PendingOperationStatus
import java.time.Instant
import java.time.LocalDate

internal class SyncLocalDataSource(
    private val pendingOperationDao: PendingOperationDao,
    private val syncWindowDao: SyncWindowDao,
) {
    suspend fun readyOperations(now: Instant): List<PendingOperationEntity> =
        pendingOperationDao.getReady(now.toEpochMilli())

    suspend fun operations(status: PendingOperationStatus): List<PendingOperationEntity> =
        pendingOperationDao.getByStatus(status)

    suspend fun operation(id: String): PendingOperationEntity? =
        pendingOperationDao.getById(id)

    suspend fun latestOperation(
        entityType: PendingEntityType,
        entityClientId: String,
    ): PendingOperationEntity? = pendingOperationDao.getLatestForEntity(
        entityType = entityType,
        entityClientId = entityClientId,
    )

    suspend fun upsert(operation: PendingOperationEntity) =
        pendingOperationDao.upsert(operation)

    suspend fun remove(operationId: String) =
        pendingOperationDao.delete(operationId)

    suspend fun recordFailure(
        operationId: String,
        status: PendingOperationStatus,
        lastError: String?,
        nextAttemptAt: Instant?,
    ) = pendingOperationDao.recordFailure(
        operationId = operationId,
        status = status,
        lastError = lastError,
        nextAttemptAtMillis = nextAttemptAt?.toEpochMilli(),
    )

    suspend fun lastSyncedAt(
        accountId: AccountId,
        period: TransactionPeriod,
    ): Instant? =
        syncWindowDao.get(
            accountClientId = accountId.value,
            startEpochDay = period.startDate.toEpochDay(),
            endEpochDay = period.endDate.toEpochDay(),
        )?.lastSyncedAtMillis?.let(Instant::ofEpochMilli)

    suspend fun markSynced(
        accountId: AccountId,
        period: TransactionPeriod,
        syncedAt: Instant,
    ) {
        syncWindowDao.upsert(
            SyncWindowEntity(
                accountClientId = accountId.value,
                startEpochDay = period.startDate.toEpochDay(),
                endEpochDay = period.endDate.toEpochDay(),
                lastSyncedAtMillis = syncedAt.toEpochMilli(),
            ),
        )
    }

    suspend fun registeredPeriods(): List<TransactionPeriod> =
        syncWindowDao.getAll()
            .map { window ->
                TransactionPeriod(
                    startDate = LocalDate.ofEpochDay(window.startEpochDay),
                    endDate = LocalDate.ofEpochDay(window.endEpochDay),
                )
            }
            .distinct()
}
