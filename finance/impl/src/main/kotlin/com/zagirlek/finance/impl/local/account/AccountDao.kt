package com.zagirlek.finance.impl.local.account

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.zagirlek.finance.impl.local.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
internal interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY created_at_millis, client_id")
    fun observeAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE client_id = :clientId")
    fun observeByClientId(clientId: String): Flow<AccountEntity?>

    @Query("SELECT * FROM accounts WHERE client_id = :clientId")
    suspend fun getByClientId(clientId: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE remote_id = :remoteId")
    suspend fun getByRemoteId(remoteId: Long): AccountEntity?

    @Query("SELECT * FROM accounts WHERE remote_id IS NOT NULL ORDER BY remote_id")
    suspend fun getAllWithRemoteId(): List<AccountEntity>

    @Upsert
    suspend fun upsert(account: AccountEntity)

    @Upsert
    suspend fun upsertAll(accounts: List<AccountEntity>)

    @Query(
        """
        UPDATE accounts
        SET sync_status = :syncStatus,
            updated_at_local_millis = :updatedAtLocalMillis
        WHERE client_id = :clientId
        """,
    )
    suspend fun updateSyncStatus(
        clientId: String,
        syncStatus: SyncStatus,
        updatedAtLocalMillis: Long,
    )
}
