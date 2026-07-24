package com.zagirlek.finance.impl.local

import com.zagirlek.finance.api.account.Account
import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.category.Category
import com.zagirlek.finance.api.category.CategoryId
import com.zagirlek.finance.api.money.CurrencyCode
import com.zagirlek.finance.api.money.Money
import com.zagirlek.finance.api.transaction.Transaction
import com.zagirlek.finance.api.transaction.TransactionId
import com.zagirlek.finance.api.transaction.TransactionType
import com.zagirlek.finance.impl.local.account.toDomain
import com.zagirlek.finance.impl.local.account.toEntity
import com.zagirlek.finance.impl.local.category.toEntity
import com.zagirlek.finance.impl.local.transaction.TransactionWithCategory
import com.zagirlek.finance.impl.local.transaction.toDomain
import com.zagirlek.finance.impl.local.transaction.toEntity
import java.math.BigDecimal
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalMapperTest {
    @Test
    fun `account round trip preserves decimal scale and timestamps`() {
        val account = Account(
            id = AccountId("local-account"),
            name = "Основной",
            money = Money(BigDecimal("145000.0010"), CurrencyCode.RUB),
            emoji = "💳",
            createdAt = Instant.parse("2026-07-01T08:00:00.123Z"),
            updatedAt = Instant.parse("2026-07-13T10:00:00.456Z"),
        )

        val entity = account.toEntity(
            remoteId = null,
            syncStatus = SyncStatus.PendingCreate,
            updatedAtLocalMillis = 42L,
        )

        assertEquals("145000.0010", entity.balance)
        assertEquals(account, entity.toDomain())
    }

    @Test
    fun `transaction round trip keeps category money and optional comment`() {
        val category = Category(
            id = CategoryId(7),
            name = "Кафе",
            emoji = "☕",
            type = TransactionType.Expense,
        )
        val transaction = Transaction(
            id = TransactionId("local-transaction"),
            accountId = AccountId("local-account"),
            category = category,
            money = Money(BigDecimal("420.50"), CurrencyCode.RUB),
            occurredAt = Instant.parse("2026-07-13T10:10:00Z"),
            comment = null,
            createdAt = Instant.parse("2026-07-13T10:10:01Z"),
            updatedAt = Instant.parse("2026-07-13T10:10:02Z"),
        )

        val entity = transaction.toEntity(
            remoteId = 10L,
            syncStatus = SyncStatus.Synced,
            updatedAtLocalMillis = 11L,
        )
        val restored = TransactionWithCategory(
            transaction = entity,
            category = category.toEntity(),
        ).toDomain()

        assertEquals(transaction, restored)
        assertEquals("420.50", entity.amount)
    }
}
