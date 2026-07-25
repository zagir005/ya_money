package com.zagirlek.finance.impl.transaction.remote

import com.zagirlek.finance.impl.category.remote.CategoryDto
import kotlinx.serialization.Serializable

/** Точная transport-модель элемента ответа `GET /transactions/account/{id}/period`. */
@Serializable
data class TransactionResponseDto(
    val id: Int,
    val account: AccountBriefDto,
    val category: CategoryDto,
    val amount: String,
    val transactionDate: String,
    val comment: String? = null,
    val createdAt: String,
    val updatedAt: String,
)
