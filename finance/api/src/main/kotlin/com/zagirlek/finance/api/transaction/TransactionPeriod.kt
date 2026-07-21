package com.zagirlek.finance.api.transaction

import java.time.Clock
import java.time.LocalDate

data class TransactionPeriod(
    val startDate: LocalDate,
    val endDate: LocalDate,
) {
    init {
        require(!endDate.isBefore(startDate)) { "Transaction period end must not precede start." }
    }

    companion object {
        fun currentMonthToDate(clock: Clock = Clock.systemDefaultZone()): TransactionPeriod {
            val today = LocalDate.now(clock)
            return TransactionPeriod(
                startDate = today.withDayOfMonth(1),
                endDate = today,
            )
        }
    }
}
