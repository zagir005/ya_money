package com.zagirlek.finance.impl.local

import com.zagirlek.finance.api.transaction.TransactionPeriod
import com.zagirlek.finance.impl.local.transaction.toEpochMillisRange
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionPeriodRangeTest {
    @Test
    fun `end date is inclusive and converted to next day exclusive`() {
        val zoneId = ZoneId.of("Europe/Moscow")
        val period = TransactionPeriod(
            startDate = LocalDate.of(2026, 7, 1),
            endDate = LocalDate.of(2026, 7, 13),
        )

        val (startInclusive, endExclusive) = period.toEpochMillisRange(zoneId)

        assertEquals(Instant.parse("2026-06-30T21:00:00Z").toEpochMilli(), startInclusive)
        assertEquals(Instant.parse("2026-07-13T21:00:00Z").toEpochMilli(), endExclusive)
    }

    @Test
    fun `range respects daylight saving transition`() {
        val zoneId = ZoneId.of("Europe/Berlin")
        val period = TransactionPeriod(
            startDate = LocalDate.of(2026, 3, 29),
            endDate = LocalDate.of(2026, 3, 29),
        )

        val (startInclusive, endExclusive) = period.toEpochMillisRange(zoneId)

        assertEquals(23L * 60L * 60L * 1_000L, endExclusive - startInclusive)
    }
}
