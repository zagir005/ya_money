package com.zagirlek.finance.impl.local.account

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zagirlek.finance.impl.local.SyncStatus

@Entity(
    tableName = "accounts",
    indices = [Index(value = ["remote_id"], unique = true)],
)
internal data class AccountEntity(
    @PrimaryKey
    @ColumnInfo(name = "client_id")
    val clientId: String,
    @ColumnInfo(name = "remote_id")
    val remoteId: Long?,
    val name: String,
    val emoji: String,
    val balance: String,
    val currency: String,
    @ColumnInfo(name = "created_at_millis")
    val createdAtMillis: Long,
    @ColumnInfo(name = "updated_at_millis")
    val updatedAtMillis: Long,
    @ColumnInfo(name = "updated_at_local_millis")
    val updatedAtLocalMillis: Long,
    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus,
)
