package com.zagirlek.ya_money.navigation

import com.zagirlek.finance.api.sync.FinanceSyncStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppStatusAlertTest {
    @Test
    fun `offline state has priority over outbox issues`() {
        val alert = selectAppStatusAlert(
            isOnline = false,
            syncStatus = FinanceSyncStatus(
                pendingCount = 1,
                failedCount = 1,
                unknownResultCount = 1,
            ),
        )

        assertEquals(AppStatusAlertKind.Offline, alert?.kind)
    }

    @Test
    fun `unknown result has priority over failed and pending operations`() {
        val alert = selectAppStatusAlert(
            isOnline = true,
            syncStatus = FinanceSyncStatus(
                pendingCount = 1,
                failedCount = 1,
                unknownResultCount = 2,
            ),
        )

        assertEquals(
            AppStatusAlert(
                kind = AppStatusAlertKind.UnknownResult,
                count = 2,
            ),
            alert,
        )
    }

    @Test
    fun `failed operation has priority over pending operation`() {
        val alert = selectAppStatusAlert(
            isOnline = true,
            syncStatus = FinanceSyncStatus(
                pendingCount = 1,
                failedCount = 3,
            ),
        )

        assertEquals(
            AppStatusAlert(
                kind = AppStatusAlertKind.Failed,
                count = 3,
            ),
            alert,
        )
    }

    @Test
    fun `no alert is selected for synced online state`() {
        assertNull(
            selectAppStatusAlert(
                isOnline = true,
                syncStatus = FinanceSyncStatus(),
            ),
        )
    }
}
