package com.zagirlek.finance.impl.sync

import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.sync.FinanceSyncStatus
import com.zagirlek.finance.api.sync.FinanceSyncStatusRepository
import com.zagirlek.finance.api.transaction.TransactionId
import com.zagirlek.finance.impl.local.FinanceLocalTransactionRunner
import com.zagirlek.finance.impl.local.PendingEntityType
import com.zagirlek.finance.impl.local.PendingOperationStatus
import com.zagirlek.finance.impl.local.PendingOperationType
import com.zagirlek.finance.impl.local.SyncStatus
import com.zagirlek.finance.impl.local.account.AccountsLocalDataSource
import com.zagirlek.finance.impl.local.sync.PendingOperationEntity
import com.zagirlek.finance.impl.local.sync.SyncLocalDataSource
import com.zagirlek.finance.impl.local.transaction.TransactionsLocalDataSource
import java.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomFinanceSyncStatusRepository(
    private val accountsLocalDataSource: AccountsLocalDataSource,
    private val transactionsLocalDataSource: TransactionsLocalDataSource,
    private val syncLocalDataSource: SyncLocalDataSource,
    private val transactionRunner: FinanceLocalTransactionRunner,
    private val clock: Clock,
    private val onSyncRequested: () -> Unit,
) : FinanceSyncStatusRepository {
    override fun observeStatus(): Flow<FinanceSyncStatus> =
        syncLocalDataSource.observeOperations().map(List<PendingOperationEntity>::toDomainStatus)

    override suspend fun retryFailedOperations() {
        transactionRunner.run {
            syncLocalDataSource.operations(PendingOperationStatus.Failed)
                .forEach { operation ->
                    syncLocalDataSource.upsert(
                        operation.copy(
                            status = PendingOperationStatus.Pending,
                            attemptCount = 0,
                            lastError = null,
                            nextAttemptAtMillis = null,
                        ),
                    )
                    operation.markEntityPending()
                }
        }
        onSyncRequested()
    }

    private suspend fun PendingOperationEntity.markEntityPending() {
        val syncStatus = when (operationType) {
            PendingOperationType.Create -> SyncStatus.PendingCreate
            PendingOperationType.Update -> SyncStatus.PendingUpdate
        }
        when (entityType) {
            PendingEntityType.Account -> accountsLocalDataSource.updateSyncStatus(
                accountId = AccountId(entityClientId),
                syncStatus = syncStatus,
                updatedAtLocalMillis = clock.millis(),
            )
            PendingEntityType.Transaction -> transactionsLocalDataSource.updateSyncStatus(
                transactionId = TransactionId(entityClientId),
                syncStatus = syncStatus,
                updatedAtLocalMillis = clock.millis(),
            )
        }
    }
}

internal fun List<PendingOperationEntity>.toDomainStatus(): FinanceSyncStatus =
    FinanceSyncStatus(
        pendingCount = count { operation ->
            operation.status == PendingOperationStatus.Pending
        },
        failedCount = count { operation ->
            operation.status == PendingOperationStatus.Failed
        },
        unknownResultCount = count { operation ->
            operation.status == PendingOperationStatus.UnknownResult
        },
    )
