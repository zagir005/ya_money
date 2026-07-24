package com.zagirlek.finance.impl.local

import androidx.room.TypeConverter

enum class SyncStatus {
    Synced,
    PendingCreate,
    PendingUpdate,
    Failed,
    UnknownResult,
}

enum class PendingEntityType {
    Account,
    Transaction,
}

enum class PendingOperationType {
    Create,
    Update,
}

enum class PendingOperationStatus {
    Pending,
    Failed,
    UnknownResult,
}

internal class FinanceTypeConverters {
    @TypeConverter
    fun syncStatusToString(value: SyncStatus): String = value.name

    @TypeConverter
    fun stringToSyncStatus(value: String): SyncStatus = enumValueOf(value)

    @TypeConverter
    fun pendingEntityTypeToString(value: PendingEntityType): String = value.name

    @TypeConverter
    fun stringToPendingEntityType(value: String): PendingEntityType = enumValueOf(value)

    @TypeConverter
    fun pendingOperationTypeToString(value: PendingOperationType): String = value.name

    @TypeConverter
    fun stringToPendingOperationType(value: String): PendingOperationType = enumValueOf(value)

    @TypeConverter
    fun pendingOperationStatusToString(value: PendingOperationStatus): String = value.name

    @TypeConverter
    fun stringToPendingOperationStatus(value: String): PendingOperationStatus = enumValueOf(value)
}
