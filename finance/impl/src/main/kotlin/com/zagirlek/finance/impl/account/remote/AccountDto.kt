package com.zagirlek.finance.impl.account.remote

import kotlinx.serialization.Serializable

@Serializable
data class AccountDto(
    val id: Int,
    val userId: Int,
    val name: String,
    val emoji: String,
    val balance: String,
    val currency: String,
    val createdAt: String,
    val updatedAt: String,
)
