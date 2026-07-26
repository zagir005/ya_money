package com.zagirlek.finance.impl.account

import com.zagirlek.finance.impl.local.account.AccountEntity
import kotlinx.serialization.Serializable

@Serializable
internal data class PendingAccountPayload(
    val clientId: String,
    val remoteId: Long?,
    val name: String,
    val emoji: String,
    val balance: String,
    val currency: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)

internal fun AccountEntity.toPendingPayload(): PendingAccountPayload =
    PendingAccountPayload(
        clientId = clientId,
        remoteId = remoteId,
        name = name,
        emoji = emoji,
        balance = balance,
        currency = currency,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
    )
