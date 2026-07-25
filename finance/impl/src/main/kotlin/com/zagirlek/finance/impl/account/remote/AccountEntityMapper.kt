package com.zagirlek.finance.impl.account.remote

import com.zagirlek.finance.api.error.FinanceNetworkException
import com.zagirlek.finance.api.money.CurrencyCode
import com.zagirlek.finance.impl.local.SyncStatus
import com.zagirlek.finance.impl.local.account.AccountEntity
import java.math.BigDecimal
import java.time.Instant

internal fun AccountDto.mergeIntoLocal(
    existing: AccountEntity?,
    syncedAtMillis: Long,
): AccountEntity = try {
    val remoteEntity = AccountEntity(
        clientId = existing?.clientId ?: id.toString(),
        remoteId = id.toLong(),
        name = name,
        emoji = emoji,
        balance = BigDecimal(balance).toPlainString(),
        currency = CurrencyCode.parse(currency).value,
        createdAtMillis = Instant.parse(createdAt).toEpochMilli(),
        updatedAtMillis = Instant.parse(updatedAt).toEpochMilli(),
        updatedAtLocalMillis = syncedAtMillis,
        syncStatus = SyncStatus.Synced,
    )

    if (existing == null || existing.syncStatus == SyncStatus.Synced) {
        remoteEntity
    } else {
        existing.copy(remoteId = remoteEntity.remoteId)
    }
} catch (error: RuntimeException) {
    throw FinanceNetworkException.InvalidResponse(error)
}
