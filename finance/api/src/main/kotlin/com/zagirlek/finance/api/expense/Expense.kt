package com.zagirlek.finance.api.expense

import com.zagirlek.finance.api.account.AccountId
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

@JvmInline
@Deprecated("Use TransactionId.")
value class ExpenseId(val value: String)

@JvmInline
@Deprecated("Use CategoryId.")
value class ExpenseTypeId(val value: String)

@Deprecated("Use Category.")
data class ExpenseType(
    val id: ExpenseTypeId,
    val name: String,
    val emoji: String
)

@Deprecated("Use the unified Transaction model.")
data class Expense(
    val id: ExpenseId,
    val accountId: AccountId,
    val type: ExpenseType,
    val amount: BigDecimal,
    val occurredAt: Instant,
    val occurredOn: LocalDate,
    val createdAt: Instant,
    val description: String?,
)
