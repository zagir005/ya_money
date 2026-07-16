package com.zagirlek.finance.api.account

interface AccountsRepository {
    suspend fun getAccounts(): List<Account>
}
