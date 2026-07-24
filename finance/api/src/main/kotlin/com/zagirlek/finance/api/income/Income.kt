package com.zagirlek.finance.api.income

import com.zagirlek.finance.api.account.AccountId
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

@JvmInline
@Deprecated("Use TransactionId.")
value class IncomeId(val value: String)

@JvmInline
@Deprecated("Use CategoryId.")
value class IncomeTypeId(val value: String)

@Deprecated("Use Category.")
data class IncomeType(
    val id: IncomeTypeId,
    val name: String,
    val emoji: String,
)

@Deprecated("Use the unified Transaction model.")
data class Income(
    val id: IncomeId,
    val accountId: AccountId,
    val type: IncomeType,
    val amount: BigDecimal,
    val occurredAt: Instant,
    val occurredOn: LocalDate,
    val createdAt: Instant,
    val description: String?,
)
