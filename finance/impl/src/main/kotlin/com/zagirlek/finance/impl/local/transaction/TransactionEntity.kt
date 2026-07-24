package com.zagirlek.finance.impl.local.transaction

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zagirlek.finance.impl.local.SyncStatus
import com.zagirlek.finance.impl.local.account.AccountEntity
import com.zagirlek.finance.impl.local.category.CategoryEntity

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["client_id"],
            childColumns = ["account_client_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["remote_id"], unique = true),
        Index(value = ["account_client_id"]),
        Index(value = ["category_id"]),
        Index(value = ["transaction_date_millis"]),
    ],
)
internal data class TransactionEntity(
    @PrimaryKey
    @ColumnInfo(name = "client_id")
    val clientId: String,
    @ColumnInfo(name = "remote_id")
    val remoteId: Long?,
    @ColumnInfo(name = "account_client_id")
    val accountClientId: String,
    @ColumnInfo(name = "category_id")
    val categoryId: Int,
    val amount: String,
    val currency: String,
    @ColumnInfo(name = "transaction_date_millis")
    val transactionDateMillis: Long,
    val comment: String?,
    @ColumnInfo(name = "created_at_millis")
    val createdAtMillis: Long,
    @ColumnInfo(name = "updated_at_millis")
    val updatedAtMillis: Long,
    @ColumnInfo(name = "updated_at_local_millis")
    val updatedAtLocalMillis: Long,
    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus,
)
