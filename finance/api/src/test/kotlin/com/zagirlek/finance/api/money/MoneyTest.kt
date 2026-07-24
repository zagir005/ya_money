package com.zagirlek.finance.api.money

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MoneyTest {

    @Test
    fun `adds amounts with the same currency`() {
        val result = Money(BigDecimal("10.25"), CurrencyCode.RUB) +
            Money(BigDecimal("2.75"), CurrencyCode.RUB)

        assertEquals(BigDecimal("13.00"), result.amount)
        assertEquals(CurrencyCode.RUB, result.currency)
    }

    @Test
    fun `rejects arithmetic with different currencies`() {
        assertThrows(IllegalArgumentException::class.java) {
            Money(BigDecimal.TEN, CurrencyCode.RUB) +
                Money(BigDecimal.ONE, CurrencyCode.USD)
        }
    }

    @Test
    fun `normalizes parsed currency code`() {
        assertEquals(CurrencyCode.RUB, CurrencyCode.parse(" rub "))
    }

    @Test
    fun `rejects malformed currency code`() {
        assertThrows(IllegalArgumentException::class.java) {
            CurrencyCode("RUBLE")
        }
    }
}
