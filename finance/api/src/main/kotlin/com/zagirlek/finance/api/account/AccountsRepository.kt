package com.zagirlek.finance.api.account

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface AccountsRepository {
    fun observeAccounts(): Flow<List<Account>>

    fun observeAccount(id: AccountId): Flow<Account?> = observeAccounts().map { accounts ->
        accounts.firstOrNull { account -> account.id == id }
    }

    suspend fun refreshAccounts()

    suspend fun createAccount(command: CreateAccount): AccountId

    suspend fun updateAccount(command: UpdateAccount)

    @Deprecated(
        message = "Screen components must observe Room-backed data through observeAccounts().",
    )
    suspend fun getAccounts(): List<Account>
}
