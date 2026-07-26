package com.zagirlek.finance.impl.account

import com.zagirlek.finance.api.account.Account
import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.account.AccountsRepository
import com.zagirlek.finance.api.account.CreateAccount
import com.zagirlek.finance.api.account.UpdateAccount
import com.zagirlek.finance.api.money.CurrencyCode
import com.zagirlek.finance.api.money.Money
import java.math.BigDecimal
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import java.time.Clock
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAccountsRepository(
    private val clock: Clock = Clock.systemUTC(),
) : AccountsRepository {
    private val mutableAccounts = MutableStateFlow(initialAccounts)

    override fun observeAccounts(): Flow<List<Account>> = mutableAccounts

    override suspend fun refreshAccounts() = Unit

    override suspend fun createAccount(command: CreateAccount): AccountId {
        val accountId = AccountId(UUID.randomUUID().toString())
        val now = clock.instant()
        mutableAccounts.value += Account(
            id = accountId,
            name = command.name.trim(),
            money = command.initialBalance,
            emoji = command.emoji.trim(),
            createdAt = now,
            updatedAt = now,
        )
        return accountId
    }

    override suspend fun updateAccount(command: UpdateAccount) {
        mutableAccounts.value = mutableAccounts.value.map { account ->
            if (account.id == command.accountId) {
                account.copy(
                    name = command.name.trim(),
                    emoji = command.emoji.trim(),
                    money = command.balance,
                    updatedAt = clock.instant(),
                )
            } else {
                account
            }
        }
    }

    override suspend fun getAccounts(): List<Account> = mutableAccounts.value

    private companion object {
        val initialAccounts = listOf(
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
