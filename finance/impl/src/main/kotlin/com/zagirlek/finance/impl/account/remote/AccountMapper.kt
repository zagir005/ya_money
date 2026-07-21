package com.zagirlek.finance.impl.account.remote

import com.zagirlek.finance.api.account.Account
import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.error.FinanceNetworkException
import java.math.BigDecimal
import java.time.Instant

fun AccountDto.toDomain(): Account = try {
    Account(
        id = AccountId(id.toString()),
        name = name,
        balance = BigDecimal(balance),
        emoji = emoji,
        currency = currency,
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt),
    )
} catch (error: RuntimeException) {
    throw FinanceNetworkException.InvalidResponse(error)
}
