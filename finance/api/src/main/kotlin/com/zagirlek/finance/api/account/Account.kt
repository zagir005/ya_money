package com.zagirlek.finance.api.account

import com.zagirlek.finance.api.money.Money
import java.time.Instant

@JvmInline
value class AccountId(val value: String) {
    init {
        require(value.isNotBlank()) { "Account ID must not be blank." }
    }
}

data class Account(
    val id: AccountId,
    val name: String,
    val money: Money,
    val emoji: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class CreateAccount(
    val name: String,
    val emoji: String,
    val initialBalance: Money,
) {
    init {
        require(name.isNotBlank()) { "Account name must not be blank." }
        require(emoji.isNotBlank()) { "Account emoji must not be blank." }
    }
}

data class UpdateAccount(
    val accountId: AccountId,
    val name: String,
    val emoji: String,
    val balance: Money,
) {
    init {
        require(name.isNotBlank()) { "Account name must not be blank." }
        require(emoji.isNotBlank()) { "Account emoji must not be blank." }
    }
}
