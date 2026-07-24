package com.zagirlek.finance.impl.account

import com.zagirlek.finance.api.account.Account
import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.account.AccountsRepository
import com.zagirlek.finance.api.money.CurrencyCode
import com.zagirlek.finance.api.money.Money
import java.math.BigDecimal
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeAccountsRepository : AccountsRepository {
    override fun observeAccounts(): Flow<List<Account>> = flowOf(accounts)

    override suspend fun refreshAccounts() = Unit

    override suspend fun getAccounts(): List<Account> = accounts

    private companion object {
        val accounts = listOf(
            Account(
                id = AccountId("account-main"),
                name = "Основной счёт",
                money = Money(
                    amount = BigDecimal("145000.00"),
                    currency = CurrencyCode.RUB,
                ),
                emoji = "💳",
                createdAt = Instant.parse("2026-07-01T08:00:00Z"),
                updatedAt = Instant.parse("2026-07-13T10:00:00Z"),
            ),
            Account(
                id = AccountId("account-cash"),
                name = "Наличные",
                money = Money(
                    amount = BigDecimal("12350.00"),
                    currency = CurrencyCode.RUB,
                ),
                emoji = "💵",
                createdAt = Instant.parse("2026-07-01T08:00:00Z"),
                updatedAt = Instant.parse("2026-07-13T10:00:00Z"),
            ),
        )
    }
}
