package com.zagirlek.finance.impl.local.transaction

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.zagirlek.finance.impl.local.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
internal interface TransactionDao {
    @Transaction
    @Query(
        """
        SELECT * FROM transactions
        WHERE transaction_date_millis >= :startInclusiveMillis
          AND transaction_date_millis < :endExclusiveMillis
        ORDER BY transaction_date_millis DESC, created_at_millis DESC, client_id DESC
        """,
    )
    fun observeByPeriod(
        startInclusiveMillis: Long,
        endExclusiveMillis: Long,
    ): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE client_id = :clientId")
    fun observeByClientId(clientId: String): Flow<TransactionWithCategory?>

    @Query("SELECT * FROM transactions WHERE client_id = :clientId")
    suspend fun getByClientId(clientId: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE remote_id = :remoteId")
    suspend fun getByRemoteId(remoteId: Long): TransactionEntity?

    @Query(
        """
        SELECT COUNT(*) FROM transactions
        WHERE account_client_id = :accountClientId
          AND sync_status != :syncedStatus
        """,
    )
    suspend fun countPendingForAccount(
        accountClientId: String,
        syncedStatus: SyncStatus = SyncStatus.Synced,
    ): Int

    @Query(
        """
        SELECT * FROM transactions
        WHERE account_client_id = :accountClientId
          AND sync_status IN (:pendingCreateStatus, :pendingUpdateStatus)
        ORDER BY created_at_millis, client_id
        """,
    )
    suspend fun getPendingForAccount(
        accountClientId: String,
        pendingCreateStatus: SyncStatus = SyncStatus.PendingCreate,
        pendingUpdateStatus: SyncStatus = SyncStatus.PendingUpdate,
    ): List<TransactionEntity>

    @Upsert
    suspend fun upsert(transaction: TransactionEntity)

    @Upsert
    suspend fun upsertAll(transactions: List<TransactionEntity>)
}
