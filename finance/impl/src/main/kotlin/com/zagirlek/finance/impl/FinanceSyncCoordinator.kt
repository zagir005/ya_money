package com.zagirlek.finance.impl

import com.zagirlek.finance.api.transaction.TransactionPeriod
import com.zagirlek.finance.impl.account.AccountsReadSynchronizer
import com.zagirlek.finance.impl.category.CategoriesReadSynchronizer
import com.zagirlek.finance.impl.local.PendingOperationStatus
import com.zagirlek.finance.impl.local.sync.SyncLocalDataSource
import com.zagirlek.finance.impl.sync.OutboxDelivery
import com.zagirlek.finance.impl.sync.SyncDebugLog
import com.zagirlek.finance.impl.transaction.TransactionsReadSynchronizer
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FinanceSyncCoordinator internal constructor(
    private val outboxDelivery: OutboxDelivery,
    private val accountsSynchronizer: AccountsReadSynchronizer,
    private val categoriesSynchronizer: CategoriesReadSynchronizer,
    private val transactionsSynchronizer: TransactionsReadSynchronizer,
    private val syncLocalDataSource: SyncLocalDataSource,
) {
    private val syncMutex = Mutex()

    suspend fun sync(period: TransactionPeriod? = null): FinanceSyncResult =
        syncMutex.withLock {
            val pendingCount = syncLocalDataSource.operations(PendingOperationStatus.Pending).size
            val failedCount = syncLocalDataSource.operations(PendingOperationStatus.Failed).size
            val unknownCount = syncLocalDataSource.operations(PendingOperationStatus.UnknownResult).size
            SyncDebugLog.debug(
                "Sync started: requestedPeriod=$period, pending=$pendingCount, " +
                    "failed=$failedCount, unknown=$unknownCount",
            )
            outboxDelivery.reconcileUnknownResults()
            val deliveryResult = outboxDelivery.deliver()
            outboxDelivery.reconcileUnknownResults()

            categoriesSynchronizer.refresh()
            accountsSynchronizer.refresh()

            val periods = buildSet {
                addAll(syncLocalDataSource.registeredPeriods())
                period?.let(::add)
            }
            periods.forEach { registeredPeriod ->
                transactionsSynchronizer.refresh(registeredPeriod)
            }

            SyncDebugLog.debug(
                "Sync finished: refreshedPeriods=${periods.size}, needsRetry=${deliveryResult.needsRetry}",
            )
            FinanceSyncResult(needsRetry = deliveryResult.needsRetry)
        }
}

data class FinanceSyncResult(
    val needsRetry: Boolean,
)
