package com.zagirlek.finance.api.account

import com.zagirlek.finance.api.money.CurrencyCode
import com.zagirlek.finance.api.money.Money
import java.math.BigDecimal
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
) {
    @Deprecated(
        message = "Use money.amount.",
        replaceWith = ReplaceWith("money.amount"),
    )
    val balance: BigDecimal
        get() = money.amount

    @Deprecated(
        message = "Use money.currency.",
        replaceWith = ReplaceWith("money.currency"),
    )
    val currency: String
        get() = money.currency.value
}

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
    val currency: CurrencyCode,
) {
    init {
        require(name.isNotBlank()) { "Account name must not be blank." }
        require(emoji.isNotBlank()) { "Account emoji must not be blank." }
    }
}

data class AdjustAccountBalance(
    val accountId: AccountId,
    val newBalance: Money,
    val occurredAt: Instant,
)
