package com.zagirlek.finance.impl.transaction

import com.zagirlek.finance.impl.local.transaction.TransactionEntity
import kotlinx.serialization.Serializable

@Serializable
internal data class PendingTransactionPayload(
    val clientId: String,
    val remoteId: Long?,
    val accountClientId: String,
    val categoryId: Int,
    val amount: String,
    val currency: String,
    val transactionDateMillis: Long,
    val comment: String?,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)

internal fun TransactionEntity.toPendingPayload(): PendingTransactionPayload =
    PendingTransactionPayload(
        clientId = clientId,
        remoteId = remoteId,
        accountClientId = accountClientId,
        categoryId = categoryId,
        amount = amount,
        currency = currency,
        transactionDateMillis = transactionDateMillis,
        comment = comment,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
    )
