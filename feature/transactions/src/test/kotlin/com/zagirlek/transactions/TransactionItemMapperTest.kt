package com.zagirlek.transactions

import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.category.Category
import com.zagirlek.finance.api.category.CategoryId
import com.zagirlek.finance.api.money.CurrencyCode
import com.zagirlek.finance.api.money.Money
import com.zagirlek.finance.api.transaction.Transaction
import com.zagirlek.finance.api.transaction.TransactionId
import com.zagirlek.finance.api.transaction.TransactionType
import com.zagirlek.ui.formatter.MoneyFormatter
import java.math.BigDecimal
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionItemMapperTest {
    @Test
    fun `list title comes from backend category even when transaction has comment`() {
        val transaction = transaction(
            id = "transaction",
            occurredAt = "2026-07-25T10:00:00Z",
            createdAt = "2026-07-25T10:00:01Z",
            comment = "Зарплата за месяц",
        )

        val result = transaction.toItemUi(
            moneyFormatter = object : MoneyFormatter {
                override fun format(money: Money): String = "1 250 ₽"
            },
        )

        assertEquals("Продукты", result.title)
        assertEquals("🛒", result.lead)
        assertEquals("1 250 ₽", result.trail)
    }

    @Test
    fun `transactions are sorted from newest occurred date to oldest`() {
        val oldest = transaction(
            id = "oldest",
            occurredAt = "2026-07-23T10:00:00Z",
            createdAt = "2026-07-25T12:00:00Z",
        )
        val newest = transaction(
            id = "newest",
            occurredAt = "2026-07-25T10:00:00Z",
            createdAt = "2026-07-25T10:00:00Z",
        )
        val middle = transaction(
            id = "middle",
            occurredAt = "2026-07-24T10:00:00Z",
            createdAt = "2026-07-25T11:00:00Z",
        )

        val result = listOf(oldest, newest, middle).sortedNewestFirst()

        assertEquals(listOf("newest", "middle", "oldest"), result.map { it.id.value })
    }

    @Test
    fun `creation date breaks ties for equal transaction date`() {
        val createdEarlier = transaction(
            id = "created-earlier",
            occurredAt = "2026-07-25T10:00:00Z",
            createdAt = "2026-07-25T10:01:00Z",
        )
        val createdLater = transaction(
            id = "created-later",
            occurredAt = "2026-07-25T10:00:00Z",
            createdAt = "2026-07-25T10:02:00Z",
        )

        val result = listOf(createdEarlier, createdLater).sortedNewestFirst()

        assertEquals(listOf("created-later", "created-earlier"), result.map { it.id.value })
    }

    private fun transaction(
        id: String,
        occurredAt: String,
        createdAt: String,
        comment: String? = null,
    ) = Transaction(
        id = TransactionId(id),
        accountId = AccountId("account"),
        category = Category(
            id = CategoryId(1),
            name = "Продукты",
            emoji = "🛒",
            type = TransactionType.Expense,
        ),
        money = Money(
            amount = BigDecimal("1250.00"),
            currency = CurrencyCode.RUB,
        ),
        occurredAt = Instant.parse(occurredAt),
        comment = comment,
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(createdAt),
    )
}
