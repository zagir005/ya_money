package com.zagirlek.finance.api.transaction

import com.zagirlek.finance.api.account.AccountId
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

@JvmInline
value class TransactionId(val value: String)

enum class TransactionType {
    Expense,
    Income,
}

data class TransactionCategory(
    val id: Int,
    val name: String,
    val emoji: String,
    val type: TransactionType,
)

data class TransactionHistoryEntry(
    val id: TransactionId,
    val accountId: AccountId,
    val category: TransactionCategory,
    val amount: BigDecimal,
    val occurredAt: Instant,
    val occurredOn: LocalDate,
    val createdAt: Instant,
    val description: String?,
)

/**
 * A combined, account-independent transaction history for period-based
 * consumers such as analytics.
 */
interface TransactionHistoryRepository {
    suspend fun getHistory(
        period: TransactionPeriod = TransactionPeriod.currentMonthToDate(),
    ): List<TransactionHistoryEntry>
}
