package com.zagirlek.finance.impl.transaction

import com.zagirlek.finance.api.transaction.CreateTransaction
import com.zagirlek.finance.api.transaction.Transaction
import com.zagirlek.finance.api.transaction.TransactionId
import com.zagirlek.finance.api.transaction.TransactionPeriod
import com.zagirlek.finance.api.transaction.TransactionsRepository
import com.zagirlek.finance.api.transaction.UpdateTransaction
import com.zagirlek.finance.impl.local.transaction.TransactionsLocalDataSource
import com.zagirlek.finance.impl.FinanceSyncCoordinator
import kotlinx.coroutines.flow.Flow

internal class OfflineFirstTransactionsRepository(
    private val localDataSource: TransactionsLocalDataSource,
    private val syncCoordinator: FinanceSyncCoordinator,
    private val commandHandler: TransactionsCommandHandler,
    private val onSyncRequested: () -> Unit,
) : TransactionsRepository {
    override fun observeTransactions(period: TransactionPeriod): Flow<List<Transaction>> =
        localDataSource.observeTransactions(period)

    override fun observeTransaction(id: TransactionId): Flow<Transaction?> =
        localDataSource.observeTransaction(id)

    override suspend fun createTransaction(command: CreateTransaction): TransactionId {
        val transactionId = commandHandler.create(command)
        onSyncRequested()
        return transactionId
    }

    override suspend fun updateTransaction(command: UpdateTransaction) {
        commandHandler.update(command)
        onSyncRequested()
    }

    override suspend fun refreshTransactions(period: TransactionPeriod) {
        syncCoordinator.sync(period)
    }
}
