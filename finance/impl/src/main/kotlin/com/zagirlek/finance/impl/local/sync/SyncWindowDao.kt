package com.zagirlek.finance.impl.local.sync

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
internal interface SyncWindowDao {
    @Query("SELECT * FROM sync_windows ORDER BY start_epoch_day, end_epoch_day")
    suspend fun getAll(): List<SyncWindowEntity>

    @Query(
        """
        SELECT * FROM sync_windows
        WHERE account_client_id = :accountClientId
          AND start_epoch_day = :startEpochDay
          AND end_epoch_day = :endEpochDay
        """,
    )
    suspend fun get(
        accountClientId: String,
        startEpochDay: Long,
        endEpochDay: Long,
    ): SyncWindowEntity?

    @Upsert
    suspend fun upsert(syncWindow: SyncWindowEntity)
}
