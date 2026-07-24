package com.zagirlek.finance.impl.account.remote

import com.zagirlek.finance.api.account.Account
import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.error.FinanceNetworkException
import com.zagirlek.finance.api.money.CurrencyCode
import com.zagirlek.finance.api.money.Money
import java.math.BigDecimal
import java.time.Instant

fun AccountDto.toDomain(): Account = try {
    Account(
        id = AccountId(id.toString()),
        name = name,
        money = Money(
            amount = BigDecimal(balance),
            currency = CurrencyCode.parse(currency),
        ),
        emoji = emoji,
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt),
    )
} catch (error: RuntimeException) {
    throw FinanceNetworkException.InvalidResponse(error)
}
