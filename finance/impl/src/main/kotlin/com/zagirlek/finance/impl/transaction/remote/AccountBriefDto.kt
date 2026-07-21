package com.zagirlek.finance.impl.transaction.remote

import kotlinx.serialization.Serializable

@Serializable
data class AccountBriefDto(
    val id: Int,
    val name: String,
    val emoji: String,
    val balance: String,
    val currency: String,
)
