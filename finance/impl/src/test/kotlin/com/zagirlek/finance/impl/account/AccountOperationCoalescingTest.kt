package com.zagirlek.finance.impl.account

import com.zagirlek.finance.impl.local.PendingOperationType
import com.zagirlek.finance.impl.local.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class AccountOperationCoalescingTest {
    @Test
    fun `editing a locally created account keeps create operation`() {
        assertEquals(
            PendingOperationType.Create,
            resolveAccountOperationType(
                syncStatus = SyncStatus.PendingCreate,
                existingOperationType = PendingOperationType.Create,
            ),
        )
    }

    @Test
    fun `existing create operation wins over stale entity status`() {
        assertEquals(
            PendingOperationType.Create,
            resolveAccountOperationType(
                syncStatus = SyncStatus.PendingUpdate,
                existingOperationType = PendingOperationType.Create,
            ),
        )
    }

    @Test
    fun `editing a remote account creates update operation`() {
        assertEquals(
            PendingOperationType.Update,
            resolveAccountOperationType(
                syncStatus = SyncStatus.Synced,
                existingOperationType = null,
            ),
        )
    }
}
