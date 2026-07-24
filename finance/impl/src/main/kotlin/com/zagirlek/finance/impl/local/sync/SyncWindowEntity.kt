package com.zagirlek.finance.impl.local.sync

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.zagirlek.finance.impl.local.account.AccountEntity

@Entity(
    tableName = "sync_windows",
    primaryKeys = ["account_client_id", "start_epoch_day", "end_epoch_day"],
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["client_id"],
            childColumns = ["account_client_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["account_client_id"])],
)
internal data class SyncWindowEntity(
    @ColumnInfo(name = "account_client_id")
    val accountClientId: String,
    @ColumnInfo(name = "start_epoch_day")
    val startEpochDay: Long,
    @ColumnInfo(name = "end_epoch_day")
    val endEpochDay: Long,
    @ColumnInfo(name = "last_synced_at_millis")
    val lastSyncedAtMillis: Long,
)
