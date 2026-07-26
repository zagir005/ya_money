package com.zagirlek.finance.impl.transaction.remote

import kotlinx.serialization.Serializable

@Serializable
internal data class TransactionRequestDto(
    val accountId: Long,
    val categoryId: Int,
    val amount: String,
    val transactionDate: String,
    val comment: String?,
)

@Serializable
internal data class CreatedTransactionDto(
    val id: Int,
    val accountId: Int,
    val categoryId: Int,
    val amount: String,
    val transactionDate: String,
    val comment: String? = null,
    val createdAt: String,
    val updatedAt: String,
)

internal data class TransactionWriteResult(
    val remoteId: Long,
    val createdAt: String,
    val updatedAt: String,
)
