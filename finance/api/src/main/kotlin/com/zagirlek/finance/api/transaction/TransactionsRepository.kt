package com.zagirlek.finance.api.transaction

import kotlinx.coroutines.flow.Flow

interface TransactionsRepository {
    fun observeTransactions(
        period: TransactionPeriod = TransactionPeriod.currentMonthToDate(),
    ): Flow<List<Transaction>>

    fun observeTransaction(id: TransactionId): Flow<Transaction?>

    suspend fun createTransaction(command: CreateTransaction): TransactionId

    suspend fun updateTransaction(command: UpdateTransaction)

    suspend fun refreshTransactions(
        period: TransactionPeriod = TransactionPeriod.currentMonthToDate(),
    )
}
