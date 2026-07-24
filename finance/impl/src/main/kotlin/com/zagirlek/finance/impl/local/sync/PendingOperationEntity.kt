package com.zagirlek.finance.impl.local.sync

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zagirlek.finance.impl.local.PendingEntityType
import com.zagirlek.finance.impl.local.PendingOperationStatus
import com.zagirlek.finance.impl.local.PendingOperationType

@Entity(
    tableName = "pending_operations",
    indices = [
        Index(value = ["entity_client_id"]),
        Index(value = ["depends_on_operation_id"]),
        Index(value = ["status", "next_attempt_at_millis"]),
    ],
)
internal data class PendingOperationEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "entity_type")
    val entityType: PendingEntityType,
    @ColumnInfo(name = "operation_type")
    val operationType: PendingOperationType,
    @ColumnInfo(name = "entity_client_id")
    val entityClientId: String,
    @ColumnInfo(name = "payload_json")
    val payloadJson: String,
    @ColumnInfo(name = "depends_on_operation_id")
    val dependsOnOperationId: String?,
    val status: PendingOperationStatus,
    @ColumnInfo(name = "attempt_count")
    val attemptCount: Int,
    @ColumnInfo(name = "last_error")
    val lastError: String?,
    @ColumnInfo(name = "created_at_millis")
    val createdAtMillis: Long,
    @ColumnInfo(name = "next_attempt_at_millis")
    val nextAttemptAtMillis: Long?,
)
