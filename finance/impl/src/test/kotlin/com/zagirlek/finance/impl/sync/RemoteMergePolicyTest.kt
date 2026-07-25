package com.zagirlek.finance.impl.sync

import com.zagirlek.finance.api.error.FinanceNetworkException
import com.zagirlek.finance.impl.account.remote.AccountDto
import com.zagirlek.finance.impl.account.remote.mergeIntoLocal
import com.zagirlek.finance.impl.category.remote.CategoryDto
import com.zagirlek.finance.impl.local.SyncStatus
import com.zagirlek.finance.impl.local.account.AccountEntity
import com.zagirlek.finance.impl.local.transaction.TransactionEntity
import com.zagirlek.finance.impl.transaction.remote.AccountBriefDto
import com.zagirlek.finance.impl.transaction.remote.TransactionResponseDto
import com.zagirlek.finance.impl.transaction.remote.mergeIntoLocal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RemoteMergePolicyTest {
    @Test
    fun `new remote account receives deterministic client id`() {
        val result = remoteAccount().mergeIntoLocal(
            existing = null,
            syncedAtMillis = 500L,
        )

        assertEquals("10", result.clientId)
        assertEquals(10L, result.remoteId)
        assertEquals("1000.00", result.balance)
        assertEquals(SyncStatus.Synced, result.syncStatus)
    }

    @Test
    fun `remote account does not overwrite pending local fields`() {
        val pending = localAccount(
            name = "Локальное имя",
            syncStatus = SyncStatus.PendingUpdate,
        )

        val result = remoteAccount().mergeIntoLocal(
            existing = pending,
            syncedAtMillis = 500L,
        )

        assertEquals(pending, result)
    }

    @Test
    fun `remote account replaces synced snapshot`() {
        val result = remoteAccount().mergeIntoLocal(
            existing = localAccount(
                name = "Старое имя",
                syncStatus = SyncStatus.Synced,
            ),
            syncedAtMillis = 500L,
        )

        assertEquals("Серверный счёт", result.name)
        assertEquals(500L, result.updatedAtLocalMillis)
    }

    @Test
    fun `remote transaction keeps stable client account reference`() {
        val result = remoteTransaction().mergeIntoLocal(
            accountClientId = "local-account-id",
            existing = null,
            syncedAtMillis = 700L,
        )

        assertEquals("20", result.clientId)
        assertEquals(20L, result.remoteId)
        assertEquals("local-account-id", result.accountClientId)
        assertEquals("42.50", result.amount)
        assertNull(result.comment)
    }

    @Test
    fun `remote transaction does not overwrite pending local edit`() {
        val pending = TransactionEntity(
            clientId = "local-transaction",
            remoteId = 20L,
            accountClientId = "local-account",
            categoryId = 2,
            amount = "99.99",
            currency = "RUB",
            transactionDateMillis = 100L,
            comment = "Локальное изменение",
            createdAtMillis = 50L,
            updatedAtMillis = 110L,
            updatedAtLocalMillis = 120L,
            syncStatus = SyncStatus.PendingUpdate,
        )

        val result = remoteTransaction().mergeIntoLocal(
            accountClientId = "server-account",
            existing = pending,
            syncedAtMillis = 700L,
        )

        assertEquals(pending, result)
    }

    @Test(expected = FinanceNetworkException.InvalidResponse::class)
    fun `invalid remote money becomes typed network error`() {
        remoteAccount().copy(balance = "not-a-number").mergeIntoLocal(
            existing = null,
            syncedAtMillis = 500L,
        )
    }

    private fun remoteAccount() = AccountDto(
        id = 10,
        userId = 1,
        name = "Серверный счёт",
        emoji = "💳",
        balance = "1000.00",
        currency = "RUB",
        createdAt = "2026-07-01T08:00:00Z",
        updatedAt = "2026-07-13T10:00:00Z",
    )

    private fun localAccount(
        name: String,
        syncStatus: SyncStatus,
    ) = AccountEntity(
        clientId = "10",
        remoteId = 10L,
        name = name,
        emoji = "💵",
        balance = "900.00",
        currency = "RUB",
        createdAtMillis = 1L,
        updatedAtMillis = 2L,
        updatedAtLocalMillis = 3L,
        syncStatus = syncStatus,
    )

    private fun remoteTransaction() = TransactionResponseDto(
        id = 20,
        account = AccountBriefDto(
            id = 10,
            name = "Серверный счёт",
            emoji = "💳",
            balance = "1000.00",
            currency = "RUB",
        ),
        category = CategoryDto(
            id = 1,
            name = "Кафе",
            emoji = "☕",
            isIncome = false,
        ),
        amount = "42.50",
        transactionDate = "2026-07-13T10:10:00Z",
        comment = null,
        createdAt = "2026-07-13T10:10:01Z",
        updatedAt = "2026-07-13T10:10:02Z",
    )
}
