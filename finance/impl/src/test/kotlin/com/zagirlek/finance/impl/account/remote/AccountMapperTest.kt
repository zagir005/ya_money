package com.zagirlek.finance.impl.account.remote

import com.zagirlek.finance.api.error.FinanceNetworkException
import java.math.BigDecimal
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class AccountMapperTest {

    @Test
    fun `maps API account values to domain types`() {
        val account = accountDto().toDomain()

        assertEquals("42", account.id.value)
        assertEquals(BigDecimal("1234.56"), account.balance)
        assertEquals("RUB", account.currency)
        assertEquals(Instant.parse("2026-07-21T10:15:30Z"), account.createdAt)
        assertEquals(Instant.parse("2026-07-21T11:15:30Z"), account.updatedAt)
    }

    @Test
    fun `maps malformed balance to domain response error`() {
        assertThrows(FinanceNetworkException.InvalidResponse::class.java) {
            accountDto(balance = "not-a-number").toDomain()
        }
    }

    private fun accountDto(balance: String = "1234.56") = AccountDto(
        id = 42,
        userId = 7,
        name = "Основной счёт",
        emoji = "💳",
        balance = balance,
        currency = "RUB",
        createdAt = "2026-07-21T10:15:30Z",
        updatedAt = "2026-07-21T11:15:30Z",
    )
}
