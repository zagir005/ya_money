package com.zagirlek.finance.impl.local.sync

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.zagirlek.finance.impl.local.PendingEntityType
import com.zagirlek.finance.impl.local.PendingOperationStatus
import kotlinx.coroutines.flow.Flow

@Dao
internal interface PendingOperationDao {
    @Query(
        """
        SELECT * FROM pending_operations
        WHERE status = :status
          AND (next_attempt_at_millis IS NULL OR next_attempt_at_millis <= :nowMillis)
        ORDER BY created_at_millis, id
        """,
    )
    suspend fun getReady(
        nowMillis: Long,
        status: PendingOperationStatus = PendingOperationStatus.Pending,
    ): List<PendingOperationEntity>

    @Query(
        """
        SELECT * FROM pending_operations
        WHERE entity_client_id = :entityClientId
        ORDER BY created_at_millis, id
        """,
    )
    fun observeByEntity(entityClientId: String): Flow<List<PendingOperationEntity>>

    @Query("SELECT * FROM pending_operations WHERE id = :operationId")
    suspend fun getById(operationId: String): PendingOperationEntity?

    @Query(
        """
        SELECT * FROM pending_operations
        WHERE entity_type = :entityType
          AND entity_client_id = :entityClientId
        ORDER BY created_at_millis DESC
        LIMIT 1
        """,
    )
    suspend fun getLatestForEntity(
        entityType: PendingEntityType,
        entityClientId: String,
    ): PendingOperationEntity?

    @Upsert
    suspend fun upsert(operation: PendingOperationEntity)

    @Query("DELETE FROM pending_operations WHERE id = :operationId")
    suspend fun delete(operationId: String)

    @Query(
        """
        UPDATE pending_operations
        SET status = :status,
            attempt_count = attempt_count + 1,
            last_error = :lastError,
            next_attempt_at_millis = :nextAttemptAtMillis
        WHERE id = :operationId
        """,
    )
    suspend fun recordFailure(
        operationId: String,
        status: PendingOperationStatus,
        lastError: String?,
        nextAttemptAtMillis: Long?,
    )
}
