package com.zagirlek.finance.impl.transaction.remote

import com.zagirlek.finance.api.error.FinanceNetworkException
import com.zagirlek.finance.api.money.CurrencyCode
import com.zagirlek.finance.impl.local.SyncStatus
import com.zagirlek.finance.impl.local.transaction.TransactionEntity
import java.math.BigDecimal
import java.time.Instant

internal fun TransactionResponseDto.mergeIntoLocal(
    accountClientId: String,
    existing: TransactionEntity?,
    syncedAtMillis: Long,
): TransactionEntity = try {
    val remoteEntity = TransactionEntity(
        clientId = existing?.clientId ?: id.toString(),
        remoteId = id.toLong(),
        accountClientId = accountClientId,
        categoryId = category.id,
        amount = BigDecimal(amount).toPlainString(),
        currency = CurrencyCode.parse(account.currency).value,
        transactionDateMillis = Instant.parse(transactionDate).toEpochMilli(),
        comment = comment,
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
