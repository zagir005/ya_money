package com.zagirlek.finance.impl.transaction.remote

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionRequestFormattingTest {

    @Test
    fun `adds fraction digits to whole amount`() {
        assertEquals("556.00", "556".toTransactionRequestAmount())
    }

    @Test
    fun `preserves amount with two fraction digits`() {
        assertEquals("556.40", "556.40".toTransactionRequestAmount())
    }

    @Test
    fun `adds missing second fraction digit`() {
        assertEquals("556.40", "556.4".toTransactionRequestAmount())
    }

    @Test
    fun `keeps three fraction digits in transaction date`() {
        assertEquals(
            "2026-07-27T18:51:00.000Z",
            Instant.parse("2026-07-27T18:51:00Z").toTransactionRequestDate(),
        )
    }
}
