package com.zagirlek.finance.impl.transaction.remote

import com.zagirlek.finance.impl.category.remote.CategoryDto
import com.zagirlek.finance.api.money.CurrencyCode
import com.zagirlek.finance.api.transaction.TransactionType
import java.math.BigDecimal
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionMapperTest {

    @Test
    fun `maps response to unified transaction`() {
        val transaction = responseDto().toDomain()

        assertEquals("17", transaction.id.value)
        assertEquals("4", transaction.accountId.value)
        assertEquals(8, transaction.category.id.value)
        assertEquals(TransactionType.Expense, transaction.category.type)
        assertEquals(BigDecimal("125.50"), transaction.money.amount)
        assertEquals(CurrencyCode.RUB, transaction.money.currency)
        assertEquals(Instant.parse("2026-07-24T12:15:00Z"), transaction.occurredAt)
        assertEquals(Instant.parse("2026-07-24T12:16:00Z"), transaction.updatedAt)
    }

    private fun responseDto() = TransactionResponseDto(
        id = 17,
        account = AccountBriefDto(
            id = 4,
            name = "Основной",
            emoji = "💳",
            balance = "1000.00",
            currency = "RUB",
        ),
        category = CategoryDto(
            id = 8,
            name = "Продукты",
            emoji = "🛒",
            isIncome = false,
        ),
        amount = "125.50",
        transactionDate = "2026-07-24T12:15:00Z",
        comment = "Магазин",
        createdAt = "2026-07-24T12:15:30Z",
        updatedAt = "2026-07-24T12:16:00Z",
    )
}
