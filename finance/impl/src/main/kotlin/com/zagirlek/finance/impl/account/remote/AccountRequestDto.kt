package com.zagirlek.finance.impl.account.remote

import kotlinx.serialization.Serializable

@Serializable
internal data class AccountCreateRequestDto(
    val name: String,
    val emoji: String,
    val balance: String,
    val currency: String,
)

@Serializable
internal data class AccountUpdateRequestDto(
    val name: String,
    val emoji: String,
    val balance: String,
    val currency: String,
)
