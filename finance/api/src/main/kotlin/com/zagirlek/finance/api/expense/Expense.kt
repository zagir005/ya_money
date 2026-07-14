package com.zagirlek.finance.api.expense

import com.zagirlek.finance.api.account.AccountId
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

@JvmInline
value class ExpenseId(val value: String)

@JvmInline
value class ExpenseTypeId(val value: String)

data class ExpenseType(
    val id: ExpenseTypeId,
    val name: String,
    val emoji: String
)

data class Expense(
    val id: ExpenseId,
    val accountId: AccountId,
    val type: ExpenseType,
    val amount: BigDecimal,
    val occurredOn: LocalDate,
    val createdAt: Instant,
    val description: String?,
)
