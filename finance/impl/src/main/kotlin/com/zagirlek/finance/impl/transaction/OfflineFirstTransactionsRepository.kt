package com.zagirlek.finance.impl.transaction

import com.zagirlek.finance.api.transaction.CreateTransaction
import com.zagirlek.finance.api.transaction.Transaction
import com.zagirlek.finance.api.transaction.TransactionId
import com.zagirlek.finance.api.transaction.TransactionPeriod
import com.zagirlek.finance.api.transaction.TransactionsRepository
import com.zagirlek.finance.api.transaction.UpdateTransaction
import com.zagirlek.finance.impl.local.transaction.TransactionsLocalDataSource
import kotlinx.coroutines.flow.Flow

internal class OfflineFirstTransactionsRepository(
    private val localDataSource: TransactionsLocalDataSource,
    private val readSynchronizer: TransactionsReadSynchronizer,
    private val commandHandler: TransactionsCommandHandler,
) : TransactionsRepository {
    override fun observeTransactions(period: TransactionPeriod): Flow<List<Transaction>> =
        localDataSource.observeTransactions(period)

    override fun observeTransaction(id: TransactionId): Flow<Transaction?> =
        localDataSource.observeTransaction(id)

    override suspend fun createTransaction(command: CreateTransaction): TransactionId =
        commandHandler.create(command)

    override suspend fun updateTransaction(command: UpdateTransaction) =
        commandHandler.update(command)

    override suspend fun refreshTransactions(period: TransactionPeriod) =
        readSynchronizer.refresh(period)
}
