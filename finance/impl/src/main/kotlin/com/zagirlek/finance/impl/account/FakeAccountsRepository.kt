package com.zagirlek.finance.impl.account

import com.zagirlek.finance.api.account.Account
import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.account.AccountsRepository
import java.math.BigDecimal

class FakeAccountsRepository : AccountsRepository {
    override fun getAccounts(): List<Account> = accounts

    private companion object {
        val accounts = listOf(
            Account(
                id = AccountId("account-main"),
                name = "Основной счёт",
                balance = BigDecimal("145000.00"),
                emoji = "💳",
            ),
            Account(
                id = AccountId("account-cash"),
                name = "Наличные",
                balance = BigDecimal("12350.00"),
                emoji = "💵",
            ),
        )
    }
}
