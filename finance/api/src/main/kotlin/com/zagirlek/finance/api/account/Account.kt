package com.zagirlek.finance.api.account

import java.math.BigDecimal

@JvmInline
value class AccountId(val value: String)

data class Account(
    val id: AccountId,
    val name: String,
    val balance: BigDecimal,
    val emoji: String,
)
