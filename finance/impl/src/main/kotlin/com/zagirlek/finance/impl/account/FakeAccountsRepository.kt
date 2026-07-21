package com.zagirlek.finance.impl.account

import com.zagirlek.finance.api.account.Account
import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.account.AccountsRepository
import java.math.BigDecimal
import java.time.Instant

class FakeAccountsRepository : AccountsRepository {
    override suspend fun getAccounts(): List<Account> = accounts

    private companion object {
        val accounts = listOf(
            Account(
                id = AccountId("account-main"),
                name = "Основной счёт",
                balance = BigDecimal("145000.00"),
                emoji = "💳",
                currency = "RUB",
                createdAt = Instant.parse("2026-07-01T08:00:00Z"),
                updatedAt = Instant.parse("2026-07-13T10:00:00Z"),
            ),
            Account(
                id = AccountId("account-cash"),
                name = "Наличные",
                balance = BigDecimal("12350.00"),
                emoji = "💵",
                currency = "RUB",
                createdAt = Instant.parse("2026-07-01T08:00:00Z"),
                updatedAt = Instant.parse("2026-07-13T10:00:00Z"),
            ),
        )
    }
}
