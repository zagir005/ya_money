package com.zagirlek.finance.impl.local.transaction

import com.zagirlek.finance.api.transaction.Transaction
import com.zagirlek.finance.api.transaction.TransactionId
import com.zagirlek.finance.api.transaction.TransactionPeriod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.ZoneId

internal class TransactionsLocalDataSource(
    private val transactionDao: TransactionDao,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) {
    fun observeTransactions(period: TransactionPeriod): Flow<List<Transaction>> {
        val (startInclusive, endExclusive) = period.toEpochMillisRange(zoneId)

        return transactionDao.observeByPeriod(
            startInclusiveMillis = startInclusive,
            endExclusiveMillis = endExclusive,
        ).map { transactions -> transactions.map(TransactionWithCategory::toDomain) }
    }

    fun observeTransaction(transactionId: TransactionId): Flow<Transaction?> =
        transactionDao.observeByClientId(transactionId.value).map { it?.toDomain() }

    suspend fun getEntity(transactionId: TransactionId): TransactionEntity? =
        transactionDao.getByClientId(transactionId.value)

    suspend fun getEntity(remoteId: Long): TransactionEntity? =
        transactionDao.getByRemoteId(remoteId)

    suspend fun upsert(transaction: TransactionEntity) = transactionDao.upsert(transaction)

    suspend fun upsertAll(transactions: List<TransactionEntity>) =
        transactionDao.upsertAll(transactions)
}

internal fun TransactionPeriod.toEpochMillisRange(zoneId: ZoneId): Pair<Long, Long> =
    startDate.atStartOfDay(zoneId).toInstant().toEpochMilli() to
        endDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
