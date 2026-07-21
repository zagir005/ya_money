package com.zagirlek.finance.api.account

import java.math.BigDecimal
import java.time.Instant

@JvmInline
value class AccountId(val value: String)

data class Account(
    val id: AccountId,
    val name: String,
    val balance: BigDecimal,
    val emoji: String,
    val currency: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
