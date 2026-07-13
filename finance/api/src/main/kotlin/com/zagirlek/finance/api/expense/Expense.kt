package com.zagirlek.finance.api.expense

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

@JvmInline
value class ExpenseId(val value: String)

@JvmInline
value class AccountId(val value: String)

@JvmInline
value class ExpenseTypeId(val value: String)

/** A category of expense. Emoji is a domain value received from the backend. */
data class ExpenseType(
    val id: ExpenseTypeId,
    val name: String,
    val emoji: String,
)

/**
 * Financial operation in the expense flow.
 *
 * Display strings such as list subtitles and currency labels deliberately do not
 * belong here: the presentation layer creates them for a concrete screen.
 */
data class Expense(
    val id: ExpenseId,
    val accountId: AccountId,
    val type: ExpenseType,
    val amount: BigDecimal,
    val occurredOn: LocalDate,
    val createdAt: Instant,
    val description: String?,
)
