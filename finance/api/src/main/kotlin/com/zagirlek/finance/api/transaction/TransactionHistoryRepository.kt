package com.zagirlek.finance.api.transaction

import com.zagirlek.finance.api.account.AccountId
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

@Deprecated(
    message = "Use Category from finance.api.category with the unified Transaction model.",
)
data class TransactionCategory(
    val id: Int,
    val name: String,
    val emoji: String,
    val type: TransactionType,
)

@Deprecated(
    message = "Use Transaction and TransactionsRepository.",
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

@Deprecated(
    message = "Use TransactionsRepository.",
)
interface TransactionHistoryRepository {
    suspend fun getHistory(
        period: TransactionPeriod = TransactionPeriod.currentMonthToDate(),
    ): List<TransactionHistoryEntry>
}
