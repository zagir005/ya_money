package com.zagirlek.finance.impl.local.account

import com.zagirlek.finance.api.account.Account
import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.money.CurrencyCode
import com.zagirlek.finance.api.money.Money
import com.zagirlek.finance.impl.local.SyncStatus
import java.math.BigDecimal
import java.time.Instant

internal fun AccountEntity.toDomain(): Account = Account(
    id = AccountId(clientId),
    name = name,
    money = Money(
        amount = BigDecimal(balance),
        currency = CurrencyCode.parse(currency),
    ),
    emoji = emoji,
    createdAt = Instant.ofEpochMilli(createdAtMillis),
    updatedAt = Instant.ofEpochMilli(updatedAtMillis),
)

internal fun Account.toEntity(
    remoteId: Long?,
    syncStatus: SyncStatus,
    updatedAtLocalMillis: Long,
): AccountEntity = AccountEntity(
    clientId = id.value,
    remoteId = remoteId,
    name = name,
    emoji = emoji,
    balance = money.amount.toPlainString(),
    currency = money.currency.value,
    createdAtMillis = createdAt.toEpochMilli(),
    updatedAtMillis = updatedAt.toEpochMilli(),
    updatedAtLocalMillis = updatedAtLocalMillis,
    syncStatus = syncStatus,
)
