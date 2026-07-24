package com.zagirlek.finance.impl.local.transaction

import com.zagirlek.finance.api.account.AccountId
import com.zagirlek.finance.api.money.CurrencyCode
import com.zagirlek.finance.api.money.Money
import com.zagirlek.finance.api.transaction.Transaction
import com.zagirlek.finance.api.transaction.TransactionId
import com.zagirlek.finance.impl.local.SyncStatus
import com.zagirlek.finance.impl.local.category.toDomain
import java.math.BigDecimal
import java.time.Instant

internal fun TransactionWithCategory.toDomain(): Transaction = Transaction(
    id = TransactionId(transaction.clientId),
    accountId = AccountId(transaction.accountClientId),
    category = category.toDomain(),
    money = Money(
        amount = BigDecimal(transaction.amount),
        currency = CurrencyCode.parse(transaction.currency),
    ),
    occurredAt = Instant.ofEpochMilli(transaction.transactionDateMillis),
    comment = transaction.comment,
    createdAt = Instant.ofEpochMilli(transaction.createdAtMillis),
    updatedAt = Instant.ofEpochMilli(transaction.updatedAtMillis),
)

internal fun Transaction.toEntity(
    remoteId: Long?,
    syncStatus: SyncStatus,
    updatedAtLocalMillis: Long,
): TransactionEntity = TransactionEntity(
    clientId = id.value,
    remoteId = remoteId,
    accountClientId = accountId.value,
    categoryId = category.id.value,
    amount = money.amount.toPlainString(),
    currency = money.currency.value,
    transactionDateMillis = occurredAt.toEpochMilli(),
    comment = comment,
    createdAtMillis = createdAt.toEpochMilli(),
    updatedAtMillis = updatedAt.toEpochMilli(),
    updatedAtLocalMillis = updatedAtLocalMillis,
    syncStatus = syncStatus,
)
